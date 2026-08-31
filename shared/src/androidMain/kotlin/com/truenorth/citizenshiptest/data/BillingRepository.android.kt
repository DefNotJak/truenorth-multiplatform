package com.truenorth.citizenshiptest.data

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.consumePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * BillingClient supports exactly one listener, set at build time - unlike
 * Firestore's addSnapshotListener, it can't support one callbackFlow per
 * collector. Purchase updates are instead funneled into a single shared flow
 * that every collector (just AppNavHost, today) observes.
 */
actual class BillingRepository(private val context: Context) {
    private val _purchaseUpdates = MutableSharedFlow<PurchaseUpdate>(extraBufferCapacity = 4)
    actual val purchaseUpdates: SharedFlow<PurchaseUpdate> = _purchaseUpdates.asSharedFlow()

    // launchBillingFlow needs the original ProductDetails object, not just an ID -
    // cached from the last queryProductPrices() call.
    private var productDetailsCache: Map<String, ProductDetails> = emptyMap()

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener { billingResult, purchases ->
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    val purchased = purchases
                        ?.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                        ?.mapNotNull { it.toCompletedPurchaseOrNull() }
                        ?: emptyList()
                    if (purchased.isNotEmpty()) {
                        _purchaseUpdates.tryEmit(PurchaseUpdate.Success(purchased))
                    }
                }
                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    _purchaseUpdates.tryEmit(PurchaseUpdate.UserCancelled)
                }
                else -> {
                    _purchaseUpdates.tryEmit(PurchaseUpdate.Error(billingResult.debugMessage))
                }
            }
        }
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    actual suspend fun connect() {
        if (billingClient.isReady) return
        suspendCancellableCoroutine { continuation ->
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (continuation.isActive) continuation.resume(Unit)
                }

                override fun onBillingServiceDisconnected() {
                    // No-op here - the next connect() call (e.g. from the onResume
                    // reconciliation pass) will retry startConnection.
                }
            })
        }
    }

    actual suspend fun queryProductPrices(): Map<String, ProductPrice> {
        val products = BillingProducts.DURATIONS_MILLIS.keys.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder().setProductList(products).build()
        val result = billingClient.queryProductDetails(params)
        val detailsList = result.productDetailsList ?: emptyList()
        productDetailsCache = detailsList.associateBy { it.productId }
        return detailsList.mapNotNull { details ->
            val offer = details.oneTimePurchaseOfferDetails ?: return@mapNotNull null
            details.productId to ProductPrice(
                productId = details.productId,
                formattedPrice = offer.formattedPrice,
                priceMicros = offer.priceAmountMicros,
                currencyCode = offer.priceCurrencyCode
            )
        }.toMap()
    }

    actual fun launchPurchase(productId: String) {
        val activity = context as? Activity ?: return
        val details = productDetailsCache[productId] ?: return
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .build()
        )
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    actual suspend fun queryExistingPurchases(): List<CompletedPurchase> {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        val result = billingClient.queryPurchasesAsync(params)
        return result.purchasesList
            .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
            .mapNotNull { it.toCompletedPurchaseOrNull() }
    }

    /**
     * consumeAsync implicitly acknowledges, so no separate acknowledgePurchase
     * call is needed.
     */
    actual suspend fun consumePurchase(purchase: CompletedPurchase) {
        val params = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient.consumePurchase(params)
    }
}

private fun Purchase.toCompletedPurchaseOrNull(): CompletedPurchase? {
    val productId = products.firstOrNull() ?: return null
    return CompletedPurchase(productId = productId, purchaseToken = purchaseToken)
}

@Composable
actual fun rememberBillingRepository(): BillingRepository {
    val context = LocalContext.current
    return remember { BillingRepository(context) }
}

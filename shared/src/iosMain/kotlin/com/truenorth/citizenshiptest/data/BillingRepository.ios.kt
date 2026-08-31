package com.truenorth.citizenshiptest.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSError
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle
import platform.StoreKit.SKErrorCode
import platform.StoreKit.SKErrorDomain
import platform.StoreKit.SKPayment
import platform.StoreKit.SKPaymentQueue
import platform.StoreKit.SKPaymentTransaction
import platform.StoreKit.SKPaymentTransactionObserverProtocol
import platform.StoreKit.SKPaymentTransactionState
import platform.StoreKit.SKProduct
import platform.StoreKit.SKProductsRequest
import platform.StoreKit.SKProductsRequestDelegateProtocol
import platform.StoreKit.SKProductsResponse
import platform.StoreKit.SKRequest
import platform.darwin.NSObject
import kotlin.coroutines.resume

/**
 * StoreKit 2 (Product/Transaction, async/await) is Swift-only - it has no
 * Objective-C-compatible surface, so it isn't reachable from Kotlin/Native's
 * cinterop without a separate Swift bridge module (needs a Mac to build, which
 * this project doesn't have yet). StoreKit 1 (SKPaymentQueue/SKProduct/
 * SKPaymentTransaction) is the older API, but it's fully Objective-C-based,
 * still fully supported by Apple, and is what most cross-platform tooling
 * (Flutter, RevenueCat, etc.) uses under the hood for exactly this reason.
 */
@OptIn(ExperimentalForeignApi::class)
actual class BillingRepository {
    private val _purchaseUpdates = MutableSharedFlow<PurchaseUpdate>(extraBufferCapacity = 4)
    actual val purchaseUpdates: SharedFlow<PurchaseUpdate> = _purchaseUpdates.asSharedFlow()

    private val productCache = mutableMapOf<String, SKProduct>()
    private val transactionCache = mutableMapOf<String, SKPaymentTransaction>()
    private var observerAdded = false

    private val observer = object : NSObject(), SKPaymentTransactionObserverProtocol {
        override fun paymentQueue(queue: SKPaymentQueue, updatedTransactions: List<*>) {
            val newlyPurchased = mutableListOf<CompletedPurchase>()
            @Suppress("UNCHECKED_CAST")
            for (transaction in updatedTransactions as List<SKPaymentTransaction>) {
                when (transaction.transactionState) {
                    SKPaymentTransactionState.SKPaymentTransactionStatePurchased -> {
                        val transactionId = transaction.transactionIdentifier ?: continue
                        transactionCache[transactionId] = transaction
                        newlyPurchased += CompletedPurchase(
                            productId = transaction.payment.productIdentifier,
                            purchaseToken = transactionId
                        )
                    }
                    SKPaymentTransactionState.SKPaymentTransactionStateFailed -> {
                        val error = transaction.error
                        val cancelled = error != null &&
                            error.domain == SKErrorDomain &&
                            error.code == SKErrorCode.SKErrorPaymentCancelled.value
                        // Failed transactions are done - finish immediately, unlike
                        // purchased ones (which wait for grantPass to succeed first).
                        queue.finishTransaction(transaction)
                        if (cancelled) {
                            _purchaseUpdates.tryEmit(PurchaseUpdate.UserCancelled)
                        } else {
                            _purchaseUpdates.tryEmit(
                                PurchaseUpdate.Error(error?.localizedDescription ?: "Purchase failed")
                            )
                        }
                    }
                    else -> Unit // purchasing / deferred / restored - no action needed here
                }
            }
            if (newlyPurchased.isNotEmpty()) {
                _purchaseUpdates.tryEmit(PurchaseUpdate.Success(newlyPurchased))
            }
        }
    }

    actual suspend fun connect() {
        if (observerAdded) return
        SKPaymentQueue.defaultQueue().addTransactionObserver(observer)
        observerAdded = true
        // No separate startConnection step like Play Billing - registering the
        // observer is enough, and StoreKit automatically redelivers any
        // unfinished transactions to it right after this call.
    }

    actual suspend fun queryProductPrices(): Map<String, ProductPrice> {
        val products = suspendCancellableCoroutine<List<SKProduct>> { continuation ->
            val request = SKProductsRequest(productIdentifiers = BillingProducts.DURATIONS_MILLIS.keys)
            val delegate = object : NSObject(), SKProductsRequestDelegateProtocol {
                override fun productsRequest(request: SKProductsRequest, didReceiveResponse: SKProductsResponse) {
                    @Suppress("UNCHECKED_CAST")
                    val received = didReceiveResponse.products as List<SKProduct>
                    if (continuation.isActive) continuation.resume(received)
                }

                override fun request(request: SKRequest, didFailWithError: NSError) {
                    if (continuation.isActive) continuation.resume(emptyList())
                }
            }
            // Held via productRequestDelegate so it isn't deallocated before the
            // async callback fires.
            productRequestDelegate = delegate
            request.delegate = delegate
            request.start()
        }
        productCache.clear()
        products.forEach { productCache[it.productIdentifier] = it }
        return products.associate { product ->
            val (formattedPrice, currencyCode) = formatPrice(product)
            product.productIdentifier to ProductPrice(
                productId = product.productIdentifier,
                formattedPrice = formattedPrice,
                priceMicros = (product.price.doubleValue * 1_000_000).toLong(),
                currencyCode = currencyCode
            )
        }
    }

    // Kept alive between queryProductPrices() calls only for as long as a request
    // is in flight - reassigned per call, not per-product.
    private var productRequestDelegate: SKProductsRequestDelegateProtocol? = null

    actual fun launchPurchase(productId: String) {
        val product = productCache[productId] ?: return
        SKPaymentQueue.defaultQueue().addPayment(SKPayment.paymentWithProduct(product))
    }

    actual suspend fun queryExistingPurchases(): List<CompletedPurchase> {
        // StoreKit 1 has no pull-based "query purchases" API like Play's
        // queryPurchasesAsync - the queue automatically redelivers any unfinished
        // transactions to the observer once connect() registers it (at cold start
        // and again on every relaunch), so reconciliation happens via
        // purchaseUpdates instead of a separate explicit call here.
        return emptyList()
    }

    actual suspend fun consumePurchase(purchase: CompletedPurchase) {
        val transaction = transactionCache.remove(purchase.purchaseToken) ?: return
        SKPaymentQueue.defaultQueue().finishTransaction(transaction)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun formatPrice(product: SKProduct): Pair<String, String> {
    val formatter = NSNumberFormatter()
    formatter.numberStyle = NSNumberFormatterCurrencyStyle
    formatter.locale = product.priceLocale
    val formattedPrice = formatter.stringFromNumber(product.price) ?: product.price.stringValue
    return formattedPrice to formatter.currencyCode
}

@Composable
actual fun rememberBillingRepository(): BillingRepository =
    remember { BillingRepository() }

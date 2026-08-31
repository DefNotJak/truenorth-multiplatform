package com.truenorth.citizenshiptest.data

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.SharedFlow

/**
 * One-time (non-renewing) passes, not subscriptions - matches the app's "no
 * auto-renewal" marketing copy. Access is time-limited, so purchases must be
 * consumed/finished after granting entitlement (see [UsageRepository.grantPass]),
 * not left permanently owned, or the store would refuse to sell the same pass
 * again once it expires.
 */
object BillingProducts {
    const val ONE_MONTH = "truenorth_pass_1_month"
    const val THREE_MONTHS = "truenorth_pass_3_month"
    val DURATIONS_MILLIS = mapOf(
        ONE_MONTH to 30L * 24 * 60 * 60 * 1000,
        THREE_MONTHS to 90L * 24 * 60 * 60 * 1000
    )
}

/** Store-agnostic view of a purchasable product's real, localized price. */
data class ProductPrice(
    val productId: String,
    val formattedPrice: String,
    val priceMicros: Long,
    val currencyCode: String
)

/** Store-agnostic view of a completed purchase, pending grant + consume. */
data class CompletedPurchase(val productId: String, val purchaseToken: String)

sealed class PurchaseUpdate {
    data class Success(val purchases: List<CompletedPurchase>) : PurchaseUpdate()
    data object UserCancelled : PurchaseUpdate()
    data class Error(val message: String) : PurchaseUpdate()
}

expect class BillingRepository {
    val purchaseUpdates: SharedFlow<PurchaseUpdate>

    /** Idempotent - safe to call again (e.g. on resume) if the connection dropped. */
    suspend fun connect()

    suspend fun queryProductPrices(): Map<String, ProductPrice>

    fun launchPurchase(productId: String)

    /** Reconciles anything the store considers purchased that this app hasn't granted+consumed yet. */
    suspend fun queryExistingPurchases(): List<CompletedPurchase>

    /**
     * Only call after entitlement has been durably granted (see
     * UsageRepository.grantPass) - consuming first and failing the grant after
     * would make the purchase unrecoverable, since the store forgets consumed/
     * finished purchases.
     */
    suspend fun consumePurchase(purchase: CompletedPurchase)
}

@Composable
expect fun rememberBillingRepository(): BillingRepository

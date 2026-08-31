package com.truenorth.citizenshiptest.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// One free practice test per day - unlimited requires a paid pass.
private const val DAILY_FREE_TESTS_LIMIT = 1

private fun currentDateString(): String =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()

private fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()

data class UsageState(
    val freeTestsUsedToday: Int,
    val freeTestDate: String?,
    val subscriptionExpiresAtMillis: Long?,
    val testDateMillis: Long? = null,
    val onboardingCompleted: Boolean = false,
    val privacyConsentGiven: Boolean = false,
    val isLoaded: Boolean = false
) {
    companion object {
        const val DAILY_LIMIT = DAILY_FREE_TESTS_LIMIT
    }

    val hasActiveSubscription: Boolean
        get() = subscriptionExpiresAtMillis != null && subscriptionExpiresAtMillis > nowMillis()

    val effectiveUsedToday: Int
        get() = if (freeTestDate == currentDateString()) freeTestsUsedToday else 0

    val freeTestsRemainingToday: Int
        get() = (DAILY_FREE_TESTS_LIMIT - effectiveUsedToday).coerceAtLeast(0)

    val hasFreeTestsRemaining: Boolean
        get() = hasActiveSubscription || effectiveUsedToday < DAILY_FREE_TESTS_LIMIT
}

class UsageRepository(uid: String) {
    private val docRef = Firebase.firestore.collection("users").document(uid)

    val usageState: Flow<UsageState> = docRef.snapshots.map { snapshot ->
        UsageState(
            freeTestsUsedToday = snapshot.get<Long?>("freeTestsUsedToday")?.toInt() ?: 0,
            freeTestDate = snapshot.get<String?>("freeTestDate"),
            subscriptionExpiresAtMillis = snapshot.get<Long?>("subscriptionExpiresAtMillis"),
            testDateMillis = snapshot.get<Long?>("testDateMillis"),
            onboardingCompleted = snapshot.get<Boolean?>("onboardingCompleted") ?: false,
            privacyConsentGiven = snapshot.get<Boolean?>("privacyConsentGiven") ?: false,
            isLoaded = true
        )
    }

    suspend fun setTestDate(millis: Long?, markOnboardingCompleted: Boolean = false) {
        val updates = mutableMapOf<String, Any?>("testDateMillis" to millis)
        if (markOnboardingCompleted) updates["onboardingCompleted"] = true
        docRef.set(updates, merge = true)
    }

    suspend fun markOnboardingCompleted() {
        docRef.set(mapOf("onboardingCompleted" to true), merge = true)
    }

    suspend fun markPrivacyConsentGiven() {
        docRef.set(mapOf("privacyConsentGiven" to true), merge = true)
    }

    suspend fun incrementFreeTestsUsed() {
        val today = currentDateString()
        Firebase.firestore.runTransaction {
            val snapshot = get(docRef)
            val storedDate = snapshot.get<String?>("freeTestDate")
            val storedCount = snapshot.get<Long?>("freeTestsUsedToday")?.toInt() ?: 0
            val newCount = if (storedDate == today) storedCount + 1 else 1
            set(
                docRef,
                mapOf("freeTestsUsedToday" to newCount, "freeTestDate" to today),
                merge = true
            )
        }
    }

    /**
     * Must be a transaction, not a plain read-then-write - a purchase token can
     * resurface via BillingRepository.queryExistingPurchases() after a process
     * death between granting and consuming, and two purchases resolving close
     * together could otherwise race and lose one grant. processedPurchaseTokens
     * makes replay of the same token a no-op instead of double-stacking time.
     */
    suspend fun grantPass(purchaseToken: String, durationMillis: Long) {
        Firebase.firestore.runTransaction {
            val snapshot = get(docRef)
            val processedTokens = snapshot.get<List<String>?>("processedPurchaseTokens") ?: emptyList()
            if (purchaseToken !in processedTokens) {
                val now = nowMillis()
                val currentExpiry = snapshot.get<Long?>("subscriptionExpiresAtMillis")
                val newExpiry = (maxOf(now, currentExpiry ?: now) + durationMillis)
                    // Sanity clamp so a future stacking bug can't compound silently.
                    .coerceAtMost(now + 400L * 24 * 60 * 60 * 1000)
                set(
                    docRef,
                    mapOf(
                        "subscriptionExpiresAtMillis" to newExpiry,
                        "processedPurchaseTokens" to (processedTokens + purchaseToken)
                    ),
                    merge = true
                )
            }
        }
    }

    suspend fun deleteUserData() {
        docRef.delete()
    }
}

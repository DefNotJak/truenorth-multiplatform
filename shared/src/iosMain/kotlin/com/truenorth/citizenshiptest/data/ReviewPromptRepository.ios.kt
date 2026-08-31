package com.truenorth.citizenshiptest.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSUserDefaults

private const val HAS_REQUESTED_REVIEW_KEY = "has_requested_review"

actual class ReviewPromptRepository {
    private val defaults = NSUserDefaults.standardUserDefaults

    actual suspend fun hasRequestedReview(): Boolean = defaults.boolForKey(HAS_REQUESTED_REVIEW_KEY)

    actual suspend fun markReviewRequested() {
        defaults.setBool(true, forKey = HAS_REQUESTED_REVIEW_KEY)
    }

    // StoreKit's SKStoreReviewController.requestReview needs a live UIWindowScene,
    // which isn't available from plain shared code - wire this up from the iOS app
    // shell later (low priority polish, not a blocker for anything else).
    actual suspend fun requestReviewFlow() = Unit
}

@Composable
actual fun rememberReviewPromptRepository(): ReviewPromptRepository =
    remember { ReviewPromptRepository() }

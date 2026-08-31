package com.truenorth.citizenshiptest.data

import androidx.compose.runtime.Composable

/**
 * Local-only (not account-tied) - the in-app review ask is a device/install
 * concern. This flag exists so we only ever attempt the request once per
 * install, at one deliberate moment (a passed test, not the very first one -
 * see AppNavHost), rather than re-triggering it on every subsequent pass.
 */
expect class ReviewPromptRepository {
    suspend fun hasRequestedReview(): Boolean
    suspend fun markReviewRequested()
    suspend fun requestReviewFlow()
}

@Composable
expect fun rememberReviewPromptRepository(): ReviewPromptRepository

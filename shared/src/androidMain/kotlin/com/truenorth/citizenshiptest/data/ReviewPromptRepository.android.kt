package com.truenorth.citizenshiptest.data

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private val Context.reviewPromptDataStore by preferencesDataStore(name = "review_prompt")
private val HAS_REQUESTED_REVIEW_KEY = booleanPreferencesKey("has_requested_review")

actual class ReviewPromptRepository(private val context: Context) {

    actual suspend fun hasRequestedReview(): Boolean =
        context.reviewPromptDataStore.data.first()[HAS_REQUESTED_REVIEW_KEY] ?: false

    actual suspend fun markReviewRequested() {
        context.reviewPromptDataStore.edit { it[HAS_REQUESTED_REVIEW_KEY] = true }
    }

    actual suspend fun requestReviewFlow() {
        val activity = context as? Activity ?: return
        val manager = ReviewManagerFactory.create(activity)
        val reviewInfo = suspendCancellableCoroutine<ReviewInfo?> { continuation ->
            manager.requestReviewFlow().addOnCompleteListener { task ->
                continuation.resume(if (task.isSuccessful) task.result else null)
            }
        }
        if (reviewInfo != null) {
            manager.launchReviewFlow(activity, reviewInfo)
        }
    }
}

@Composable
actual fun rememberReviewPromptRepository(): ReviewPromptRepository {
    val context = LocalContext.current
    return remember { ReviewPromptRepository(context) }
}

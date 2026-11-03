package com.truenorth.citizenshiptest.data

import androidx.compose.runtime.Composable

/** The user dismissed the account sheet. Expected, and not worth an error message. */
class GoogleSignInCancelled : Exception("Google sign-in cancelled")

/** No Google account is available on the device. */
class NoGoogleAccountOnDevice : Exception("No Google account on this device")

/**
 * Obtains a Google ID token, which [AuthRepository.signInWithGoogle] then trades
 * for a Firebase session.
 *
 * [isAvailable] exists because this is genuinely one-sided for now. On iOS,
 * offering Google sign-in obliges us to offer Sign in with Apple too, under App
 * Store review guideline 4.8, so the two have to ship together. Rather than
 * stub an iOS implementation that would fail at the moment someone taps it,
 * the flag lets the shared screen leave the button out entirely on platforms
 * that cannot honour it.
 */
expect class GoogleSignIn {
    val isAvailable: Boolean
    suspend fun requestIdToken(): String
}

@Composable
expect fun rememberGoogleSignIn(): GoogleSignIn

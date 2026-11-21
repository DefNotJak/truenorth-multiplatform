package com.truenorth.citizenshiptest.data

import androidx.compose.runtime.Composable

/** The user dismissed the Apple sheet. Expected, and not worth an error message. */
class AppleSignInCancelled : Exception("Sign in with Apple cancelled")

/**
 * What Firebase needs to verify an Apple sign-in.
 *
 * Both halves matter. Apple is handed the SHA-256 of [rawNonce] and binds it
 * into [idToken]; Firebase is handed the raw value and checks the two agree.
 * That is what stops a stolen identity token being replayed, so the raw nonce
 * must never be reused and must never leave the device by any other route.
 */
data class AppleCredential(val idToken: String, val rawNonce: String)

/**
 * Sign in with Apple. Available on iOS only.
 *
 * [isAvailable] is false on Android, where nobody has an Apple ID to hand and
 * the system sheet does not exist, so [AuthScreen] leaves the button out
 * rather than showing one that cannot work. Same arrangement as
 * [GoogleSignIn], which is true on Android and false on iOS.
 */
expect class AppleSignIn {
    val isAvailable: Boolean
    suspend fun requestCredential(): AppleCredential
}

@Composable
expect fun rememberAppleSignIn(): AppleSignIn

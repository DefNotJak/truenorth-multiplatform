package com.truenorth.citizenshiptest.data

import androidx.compose.runtime.Composable

/** The user dismissed the account sheet. Expected, and not worth an error message. */
class GoogleSignInCancelled : Exception("Google sign-in cancelled")

/** No Google account is available on the device. */
class NoGoogleAccountOnDevice : Exception("No Google account on this device")

/**
 * What Google hands back, on its way to [AuthRepository.signInWithGoogle].
 *
 * Firebase only needs the ID token, but [accessToken] has to travel with it
 * anyway, because GitLive's two platform implementations of
 * `GoogleAuthProvider.credential` disagree about it. Android requires only
 * that one of the two is present; iOS calls `requireNotNull` on both and
 * throws an IllegalArgumentException before Firebase is ever reached.
 *
 * It stays nullable because Android genuinely cannot supply it: Credential
 * Manager returns an ID token and nothing else. Google's iOS SDK returns both,
 * so the one platform that insists on it is also the one that has it.
 */
data class GoogleCredential(
    val idToken: String,
    val accessToken: String?
)

/**
 * Obtains a Google credential, which [AuthRepository.signInWithGoogle] then
 * trades for a Firebase session.
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
    suspend fun requestCredential(): GoogleCredential
}

@Composable
expect fun rememberGoogleSignIn(): GoogleSignIn

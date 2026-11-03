package com.truenorth.citizenshiptest.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Deliberately not implemented. Offering Google sign-in on iOS obliges us to
 * offer Sign in with Apple alongside it, under App Store review guideline 4.8,
 * so the two ship together or not at all. Until then [isAvailable] is false and
 * AuthScreen leaves the button out, rather than showing one that fails on tap.
 */
actual class GoogleSignIn {
    actual val isAvailable: Boolean = false

    actual suspend fun requestIdToken(): String =
        throw UnsupportedOperationException("Google sign-in is not available on iOS yet")
}

@Composable
actual fun rememberGoogleSignIn(): GoogleSignIn = remember { GoogleSignIn() }

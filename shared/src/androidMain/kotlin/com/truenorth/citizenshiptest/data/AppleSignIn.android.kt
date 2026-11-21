package com.truenorth.citizenshiptest.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Deliberately absent on Android. Sign in with Apple exists there only as a
 * web redirect, which is a worse experience than the Google button already on
 * this screen and solves nothing those users have. Android keeps Google and
 * email/password.
 */
actual class AppleSignIn {
    actual val isAvailable: Boolean = false

    actual suspend fun requestCredential(): AppleCredential =
        throw UnsupportedOperationException("Sign in with Apple is not offered on Android")
}

@Composable
actual fun rememberAppleSignIn(): AppleSignIn = remember { AppleSignIn() }

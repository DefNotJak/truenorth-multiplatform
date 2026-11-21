package com.truenorth.citizenshiptest.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Implemented in Swift, in `iosApp`, and registered on [IosGoogleSignIn]
 * before any Compose code runs.
 *
 * Google's iOS sign-in SDK has a Swift-only surface, so Kotlin/Native cannot
 * call it through cinterop the way it calls `AuthenticationServices` for
 * Apple. Rather than hand-roll an OAuth flow, the native call stays in Swift
 * and only the result crosses back.
 *
 * Three separate single-argument callbacks rather than one callback with
 * several parameters: a Kotlin lambda taking a nullable String plus a Boolean
 * arrives in Swift with boxed types like `KotlinBoolean`, which is easy to get
 * subtly wrong on a boundary neither compiler checks end to end.
 */
interface GoogleSignInHandler {
    fun signIn(
        onToken: (String) -> Unit,
        onCancelled: () -> Unit,
        onError: (String) -> Unit
    )
}

/**
 * Set once from Swift at launch. Null on any platform or build where the
 * Swift side has not registered, which [GoogleSignIn.isAvailable] reports
 * honestly so the button is left out rather than failing on tap.
 */
object IosGoogleSignIn {
    var handler: GoogleSignInHandler? = null
}

actual class GoogleSignIn {

    actual val isAvailable: Boolean
        get() = IosGoogleSignIn.handler != null

    actual suspend fun requestIdToken(): String {
        val handler = IosGoogleSignIn.handler
            ?: throw UnsupportedOperationException("Google sign-in is not wired up on this build")

        return suspendCancellableCoroutine { continuation ->
            handler.signIn(
                onToken = { token ->
                    if (continuation.isActive) continuation.resume(token)
                },
                onCancelled = {
                    if (continuation.isActive) continuation.resumeWithException(GoogleSignInCancelled())
                },
                onError = { message ->
                    if (continuation.isActive) {
                        continuation.resumeWithException(IllegalStateException(message))
                    }
                }
            )
        }
    }
}

@Composable
actual fun rememberGoogleSignIn(): GoogleSignIn = remember { GoogleSignIn() }

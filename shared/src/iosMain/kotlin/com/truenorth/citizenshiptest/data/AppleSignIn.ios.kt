package com.truenorth.citizenshiptest.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AuthenticationServices.ASAuthorization
import platform.AuthenticationServices.ASAuthorizationAppleIDCredential
import platform.AuthenticationServices.ASAuthorizationAppleIDProvider
import platform.AuthenticationServices.ASAuthorizationController
import platform.AuthenticationServices.ASAuthorizationControllerDelegateProtocol
import platform.AuthenticationServices.ASAuthorizationControllerPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASAuthorizationScopeEmail
import platform.AuthenticationServices.ASAuthorizationScopeFullName
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUUID
import platform.Foundation.create
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** ASAuthorizationError.canceled. The user dismissed the sheet. */
private const val AS_AUTHORIZATION_ERROR_CANCELED = 1001L

@OptIn(ExperimentalForeignApi::class)
actual class AppleSignIn {

    actual val isAvailable: Boolean = true

    // Held for the lifetime of a request. ASAuthorizationController keeps only
    // weak references to its delegate and presentation provider, so a local
    // would be collected before the user had finished with the sheet and the
    // callbacks would never arrive.
    private var pendingDelegate: NSObject? = null

    actual suspend fun requestCredential(): AppleCredential {
        val rawNonce = randomNonce()

        return suspendCancellableCoroutine { continuation ->
            val request = ASAuthorizationAppleIDProvider().createRequest().apply {
                setRequestedScopes(listOf(ASAuthorizationScopeFullName, ASAuthorizationScopeEmail))
                // Apple binds the hash into the identity token. Firebase gets
                // the raw value and checks it matches, which is what makes a
                // stolen token useless to replay.
                setNonce(sha256Hex(rawNonce))
            }

            val handler = object :
                NSObject(),
                ASAuthorizationControllerDelegateProtocol,
                ASAuthorizationControllerPresentationContextProvidingProtocol {

                override fun authorizationController(
                    controller: ASAuthorizationController,
                    didCompleteWithAuthorization: ASAuthorization
                ) {
                    pendingDelegate = null
                    val credential = didCompleteWithAuthorization.credential
                    val identityToken = (credential as? ASAuthorizationAppleIDCredential)?.identityToken
                    val token = identityToken?.let { it.toUtf8String() }
                    if (token == null) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(
                                IllegalStateException("Apple returned no identity token")
                            )
                        }
                        return
                    }
                    if (continuation.isActive) {
                        continuation.resume(AppleCredential(idToken = token, rawNonce = rawNonce))
                    }
                }

                override fun authorizationController(
                    controller: ASAuthorizationController,
                    didCompleteWithError: NSError
                ) {
                    pendingDelegate = null
                    if (!continuation.isActive) return
                    if (didCompleteWithError.code == AS_AUTHORIZATION_ERROR_CANCELED) {
                        continuation.resumeWithException(AppleSignInCancelled())
                    } else {
                        continuation.resumeWithException(
                            IllegalStateException(
                                didCompleteWithError.localizedDescription ?: "Sign in with Apple failed"
                            )
                        )
                    }
                }

                override fun presentationAnchorForAuthorizationController(
                    controller: ASAuthorizationController
                ): UIWindow = keyWindow()
            }

            pendingDelegate = handler
            ASAuthorizationController(authorizationRequests = listOf(request)).apply {
                delegate = handler
                presentationContextProvider = handler
                performRequests()
            }
        }
    }
}

/**
 * 256 bits from two UUIDs rather than kotlin.random, which is not a
 * cryptographic source. NSUUID draws from the system's secure generator, and
 * the nonce only has to be unguessable and used once.
 */
private fun randomNonce(): String =
    (NSUUID().UUIDString() + NSUUID().UUIDString()).replace("-", "")

@OptIn(ExperimentalForeignApi::class)
private fun sha256Hex(value: String): String {
    val bytes = value.encodeToByteArray()
    val digest = UByteArray(CC_SHA256_DIGEST_LENGTH)
    bytes.usePinned { pinnedInput ->
        digest.usePinned { pinnedDigest ->
            CC_SHA256(
                pinnedInput.addressOf(0),
                bytes.size.toUInt(),
                pinnedDigest.addressOf(0)
            )
        }
    }
    return digest.joinToString("") { it.toString(16).padStart(2, '0') }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toUtf8String(): String? =
    platform.Foundation.NSString.create(data = this, encoding = NSUTF8StringEncoding) as String?

/**
 * The sheet has to be anchored to a window. Compose Multiplatform runs inside
 * a single UIWindow, so the first one is the right one; the scene-based
 * lookup Apple's samples use buys nothing here and fails the same way if
 * there is genuinely no window.
 */
private fun keyWindow(): UIWindow =
    UIApplication.sharedApplication.windows.firstOrNull() as? UIWindow
        ?: error("Sign in with Apple needs a window to present from")

@Composable
actual fun rememberAppleSignIn(): AppleSignIn = remember { AppleSignIn() }

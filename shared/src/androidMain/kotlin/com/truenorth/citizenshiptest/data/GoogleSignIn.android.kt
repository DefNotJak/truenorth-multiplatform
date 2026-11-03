package com.truenorth.citizenshiptest.data

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

actual class GoogleSignIn(private val context: Context) {

    actual val isAvailable: Boolean = true

    /**
     * Credential Manager rather than the deprecated GoogleSignInClient: its
     * startActivityForResult dance does not fit a Compose screen, and the same
     * request later carries passkey and saved-password options without a
     * second integration.
     */
    actual suspend fun requestIdToken(): String {
        val activity = context.findActivity()
            ?: throw IllegalStateException("Google sign-in needs an Activity context")

        val serverClientId = context.getString(context.webClientIdResource())

        val option = GetSignInWithGoogleOption.Builder(serverClientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()

        val response = try {
            CredentialManager.create(activity).getCredential(activity, request)
        } catch (e: GetCredentialCancellationException) {
            throw GoogleSignInCancelled()
        } catch (e: NoCredentialException) {
            throw NoGoogleAccountOnDevice()
        }

        val credential = response.credential
        if (credential !is CustomCredential ||
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            throw IllegalStateException("Unexpected credential type: ${credential.type}")
        }
        return GoogleIdTokenCredential.createFrom(credential.data).idToken
    }
}

@Composable
actual fun rememberGoogleSignIn(): GoogleSignIn {
    val context = LocalContext.current
    return remember { GoogleSignIn(context) }
}

/**
 * The WEB client ID, not the Android one. Firebase mints the ID token for this
 * audience and checks it server-side; the Android client ID produces a token
 * that looks fine here and is rejected at sign-in.
 *
 * Looked up by name rather than through R, because the google-services plugin
 * generates `default_web_client_id` into the application module and this is a
 * library module, whose R only carries its own resources. The resource is in
 * the merged APK at runtime, which is all getIdentifier needs.
 *
 * It is absent until the Google provider is enabled in the Firebase console and
 * a fresh google-services.json is downloaded, so say that rather than letting
 * getString fail with a bare resource ID.
 */
private fun Context.webClientIdResource(): Int {
    val id = resources.getIdentifier("default_web_client_id", "string", packageName)
    check(id != 0) {
        "default_web_client_id is missing. Enable the Google provider in the " +
            "Firebase console and replace androidApp/google-services.json."
    }
    return id
}

/**
 * Compose hands out whatever context the composition was created with, which is
 * usually a ContextThemeWrapper rather than the Activity. Credential Manager
 * needs the Activity, because it has a bottom sheet to show.
 */
private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

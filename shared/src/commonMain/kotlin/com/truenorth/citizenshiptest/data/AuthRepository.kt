package com.truenorth.citizenshiptest.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.EmailAuthProvider
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.Flow

class AuthRepository {
    private val auth = Firebase.auth

    val currentUserSnapshot: FirebaseUser? get() = auth.currentUser

    val currentUser: Flow<FirebaseUser?> = auth.authStateChanged

    suspend fun signUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
    }

    suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
    }

    suspend fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
    }

    suspend fun sendEmailVerification() {
        val user = auth.currentUser ?: return
        user.sendEmailVerification()
    }

    /**
     * FirebaseUser caches isEmailVerified client-side - it only reflects reality
     * right after sign-in/sign-up or after this reload, not continuously (there's
     * no push notification when the user clicks the link in their inbox).
     * Callers that display verification status should reload before reading it.
     */
    suspend fun reloadCurrentUser() {
        val user = auth.currentUser ?: return
        user.reload()
    }

    suspend fun signOut() {
        auth.signOut()
    }

    /**
     * Requires a fresh sign-in per Firebase's security model. If the user's session isn't
     * recent enough, this throws a re-authentication-required error - callers should
     * prompt for the password via [reauthenticate] and retry.
     */
    suspend fun deleteAccount() {
        val user = auth.currentUser ?: return
        user.delete()
    }

    suspend fun reauthenticate(password: String) {
        val user = auth.currentUser ?: return
        val email = user.email ?: return
        val credential = EmailAuthProvider.credential(email, password)
        user.reauthenticate(credential)
    }
}

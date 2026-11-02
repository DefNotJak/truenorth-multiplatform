package com.truenorth.citizenshiptest.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import dev.gitlive.firebase.auth.FirebaseAuthInvalidCredentialsException
import dev.gitlive.firebase.auth.FirebaseAuthInvalidUserException
import dev.gitlive.firebase.auth.FirebaseAuthUserCollisionException
import dev.gitlive.firebase.auth.FirebaseAuthWeakPasswordException
import com.truenorth.citizenshiptest.data.AuthRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(authRepository: AuthRepository) {
    var isSignUp by rememberSaveable { mutableStateOf(false) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    val canSubmit = !isLoading && email.isNotBlank() && password.isNotBlank() &&
        (!isSignUp || confirmPassword.isNotBlank())

    // Shared by the submit button and the keyboard's Done key, so the form can
    // be sent without reaching for a button that may be under the keyboard.
    fun submit() {
        errorMessage = null
        if (isSignUp) {
            val complexityError = passwordComplexityError(password)
            if (complexityError != null) {
                errorMessage = complexityError
                return
            }
            if (password != confirmPassword) {
                errorMessage = "Passwords don't match."
                return
            }
        }
        focusManager.clearFocus()
        isLoading = true
        scope.launch {
            try {
                if (isSignUp) {
                    authRepository.signUp(email.trim(), password)
                    // Best-effort - a failure here shouldn't block account
                    // creation, since the account itself already succeeded.
                    try {
                        authRepository.sendEmailVerification()
                        snackbarHostState.showSnackbar("Account created! We sent a verification link to ${email.trim()}.")
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Account created, but the verification email couldn't be sent. You can resend it from Settings.")
                    }
                } else {
                    authRepository.signIn(email.trim(), password)
                }
            } catch (e: Exception) {
                errorMessage = friendlyAuthErrorMessage(e)
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                // Shrinks the scroll viewport by the keyboard's height. Without
                // it the column has no idea the keyboard exists, so the bottom
                // of the form sits under it and scrolling cannot reach it -
                // including, on the sign-in screen, the only link to sign up.
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "TrueNorth",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = if (isSignUp) "Create an account to get started" else "Sign in to track your progress",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                // Tells the password manager what this field holds. Without a
                // content type the field is invisible to Google Password
                // Manager and iCloud Keychain, so they never offer to save or
                // fill it, and the user types everything by hand. That absence
                // is most of why this screen felt dated.
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentType = ContentType.Username + ContentType.EmailAddress }
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                label = { Text("Password") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    // KeyboardType.Password makes iOS flag the field as secure text
                    // entry, and iOS disables the keyboard drag-to-move-cursor
                    // gesture in secure fields. Once the user has revealed the
                    // password there is nothing left to hide, so drop to a plain
                    // text keyboard and the gesture comes back.
                    keyboardType = if (passwordVisible) KeyboardType.Text else KeyboardType.Password,
                    autoCorrectEnabled = false,
                    capitalization = KeyboardCapitalization.None,
                    imeAction = if (isSignUp) ImeAction.Next else ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                    onDone = { if (canSubmit) submit() }
                ),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                // NewPassword when signing up, Password when signing in. This
                // is the distinction that makes a manager offer to *generate
                // and save* a password on one screen and *fill* it on the
                // other, rather than doing the wrong one of the two.
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentType = if (isSignUp) ContentType.NewPassword else ContentType.Password
                    }
            )

            if (isSignUp) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; errorMessage = null },
                    label = { Text("Confirm Password") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (passwordVisible) KeyboardType.Text else KeyboardType.Password,
                        autoCorrectEnabled = false,
                        capitalization = KeyboardCapitalization.None,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { if (canSubmit) submit() }
                    ),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentType = ContentType.NewPassword }
                )
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { submit() },
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.height(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(if (isSignUp) "Create Account" else "Sign In")
                }
            }

            TextButton(onClick = { isSignUp = !isSignUp; errorMessage = null; confirmPassword = "" }) {
                Text(
                    if (isSignUp) "Already have an account? Sign In" else "Don't have an account? Sign Up"
                )
            }

            if (!isSignUp) {
                TextButton(onClick = {
                    if (email.isBlank()) {
                        scope.launch { snackbarHostState.showSnackbar("Enter your email above first") }
                    } else {
                        scope.launch {
                            try {
                                authRepository.sendPasswordResetEmail(email.trim())
                                snackbarHostState.showSnackbar("Password reset email sent")
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar(friendlyAuthErrorMessage(e))
                            }
                        }
                    }
                }) {
                    Text("Forgot password?")
                }
            }
        }
    }
}

internal fun passwordComplexityError(password: String): String? {
    return when {
        password.length < 8 -> "Password must be at least 8 characters."
        !password.any { it.isDigit() } -> "Password must include at least one number."
        !password.any { it.isLetter() } -> "Password must include at least one letter."
        else -> null
    }
}

internal fun friendlyAuthErrorMessage(e: Exception): String {
    // Type-based, not error-code-string-based - GitLive's Firebase SDK exposes a
    // real typed exception hierarchy, and the underlying platform SDK's raw error
    // code/format isn't guaranteed consistent between Android and iOS.
    return when (e) {
        is FirebaseAuthInvalidCredentialsException -> "Incorrect email or password."
        is FirebaseAuthUserCollisionException -> "An account already exists with that email."
        is FirebaseAuthWeakPasswordException -> "Password should be at least 6 characters."
        is FirebaseAuthInvalidUserException -> "No account found with that email."
        else -> "Something went wrong. Please try again."
    }
}

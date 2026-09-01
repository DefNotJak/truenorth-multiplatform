package com.truenorth.citizenshiptest.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
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

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                label = { Text("Password") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (isSignUp) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; errorMessage = null },
                    label = { Text("Confirm Password") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
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
                onClick = {
                    errorMessage = null
                    if (isSignUp) {
                        val complexityError = passwordComplexityError(password)
                        if (complexityError != null) {
                            errorMessage = complexityError
                            return@Button
                        }
                        if (password != confirmPassword) {
                            errorMessage = "Passwords don't match."
                            return@Button
                        }
                    }
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
                },
                enabled = !isLoading && email.isNotBlank() && password.isNotBlank() && (!isSignUp || confirmPassword.isNotBlank()),
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

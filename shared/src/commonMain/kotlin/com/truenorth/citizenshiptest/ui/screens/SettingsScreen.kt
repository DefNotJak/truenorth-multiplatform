package com.truenorth.citizenshiptest.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import com.truenorth.citizenshiptest.data.ThemeMode
import com.truenorth.citizenshiptest.data.UsageState
import com.truenorth.citizenshiptest.data.rememberAppVersionLabel
import dev.gitlive.firebase.auth.FirebaseAuthRecentLoginRequiredException
import kotlin.time.Instant
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private const val FEEDBACK_EMAIL = "t.north.apps@gmail.com"
private const val OFFICIAL_TEST_INFO_URL =
    "https://www.canada.ca/en/immigration-refugees-citizenship/services/canadian-citizenship/test.html"
private val VerifiedGreen = Color(0xFF2E7D32)

private val monthAbbreviations = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
)

/** M3's DatePicker encodes the picked date at UTC midnight - format in UTC too, so the
 *  displayed day matches what was actually picked regardless of the device's local timezone. */
internal fun formatDateUtc(millis: Long): String {
    val date = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date
    return "${monthAbbreviations[date.monthNumber - 1]} ${date.dayOfMonth}, ${date.year}"
}

private fun formatDateLocal(millis: Long): String {
    val date = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${monthAbbreviations[date.monthNumber - 1]} ${date.dayOfMonth}, ${date.year}"
}

internal fun percentEncode(value: String): String {
    val unreserved = (('A'..'Z') + ('a'..'z') + ('0'..'9') + listOf('-', '_', '.', '~')).toSet()
    return buildString {
        for (byte in value.encodeToByteArray()) {
            val char = byte.toInt().toChar()
            if (char in unreserved) {
                append(char)
            } else {
                append('%')
                append((byte.toInt() and 0xFF).toString(16).uppercase().padStart(2, '0'))
            }
        }
    }
}

private sealed class DeleteAccountDialog {
    data object None : DeleteAccountDialog()
    data object Confirm : DeleteAccountDialog()
    data object Loading : DeleteAccountDialog()
    data class Reauth(val error: String? = null) : DeleteAccountDialog()
}

private data class ThemeOption(
    val mode: ThemeMode,
    val label: String,
    val description: String
)

private val themeOptions = listOf(
    ThemeOption(ThemeMode.SYSTEM, "System default", "Follow your device's setting"),
    ThemeOption(ThemeMode.LIGHT, "Light", "Always use light mode"),
    ThemeOption(ThemeMode.DARK, "Dark", "Always use dark mode")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onBack: () -> Unit,
    onUpgradeClick: () -> Unit,
    userEmail: String?,
    isEmailVerified: Boolean,
    onSignOut: () -> Unit,
    usageState: UsageState,
    onSetTestDate: (Long?) -> Unit,
    onDeleteAccount: suspend () -> Unit,
    onReauthenticate: suspend (String) -> Unit,
    onResendVerificationEmail: suspend () -> Unit,
    onRefreshVerificationStatus: suspend () -> Unit
) {
    var dialog by remember { mutableStateOf<DeleteAccountDialog>(DeleteAccountDialog.None) }
    var reauthPassword by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val appVersionLabel = rememberAppVersionLabel()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        // Silent, best-effort - picks up a verification the user completed in
        // their inbox since the last time this screen loaded. Not worth
        // surfacing a failure for (e.g. offline); the manual resend button
        // below still works either way.
        try {
            onRefreshVerificationStatus()
        } catch (e: Exception) {
            // Ignore.
        }
    }

    fun sendFeedback() {
        val subject = "TrueNorth Feedback"
        val body = "\n\n\n---\nApp version: $appVersionLabel"
        val mailtoUrl = "mailto:$FEEDBACK_EMAIL?subject=${percentEncode(subject)}&body=${percentEncode(body)}"
        try {
            uriHandler.openUri(mailtoUrl)
        } catch (e: Exception) {
            scope.launch {
                snackbarHostState.showSnackbar("No email app found. Reach us at $FEEDBACK_EMAIL")
            }
        }
    }

    fun attemptDelete(onNeedsReauth: () -> Unit) {
        scope.launch {
            dialog = DeleteAccountDialog.Loading
            try {
                onDeleteAccount()
                dialog = DeleteAccountDialog.None
            } catch (e: FirebaseAuthRecentLoginRequiredException) {
                onNeedsReauth()
            } catch (e: Exception) {
                dialog = DeleteAccountDialog.Confirm
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                SettingsSection(title = "Account") {
                    Text(
                        text = userEmail ?: "Signed in",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isEmailVerified) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = VerifiedGreen,
                                modifier = Modifier.height(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Email verified",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.height(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Email not verified",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        TextButton(
                            onClick = {
                                scope.launch {
                                    try {
                                        onResendVerificationEmail()
                                        snackbarHostState.showSnackbar("Verification email sent.")
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Couldn't send the email. Check your connection and try again.")
                                    }
                                }
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Resend verification email")
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
                        Text("Sign Out")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { dialog = DeleteAccountDialog.Confirm },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Delete Account")
                    }
                }
            }
            item {
                SettingsSection(title = "Appearance") {
                    Column {
                        themeOptions.forEach { option ->
                            ThemeOptionRow(
                                option = option,
                                selected = themeMode == option.mode,
                                onClick = { onThemeModeChange(option.mode) }
                            )
                        }
                    }
                }
            }
            item {
                SettingsSection(title = "Test Date") {
                    if (usageState.testDateMillis != null) {
                        val formattedDate = remember(usageState.testDateMillis) {
                            formatDateUtc(usageState.testDateMillis)
                        }
                        Text(
                            text = "Your test is on $formattedDate.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { showDatePicker = true }) {
                                Text("Change")
                            }
                            TextButton(onClick = { onSetTestDate(null) }) {
                                Text("Clear")
                            }
                        }
                    } else {
                        Text(
                            text = "Set your test date to see a countdown on the home screen.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("Set Test Date")
                        }
                    }
                }
            }
            item {
                SettingsSection(title = "Subscription") {
                    if (usageState.hasActiveSubscription) {
                        val formattedDate = remember(usageState.subscriptionExpiresAtMillis) {
                            formatDateLocal(usageState.subscriptionExpiresAtMillis!!)
                        }
                        Text(
                            text = "You have full access until $formattedDate.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            text = "You've used ${usageState.effectiveUsedToday} of ${UsageState.DAILY_LIMIT} free practice tests today, no ads.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = onUpgradeClick, modifier = Modifier.fillMaxWidth()) {
                            Text("Upgrade")
                        }
                    }
                }
            }
            item {
                SettingsSection(title = "Feedback") {
                    Text(
                        text = "Spotted a bug, or have an idea to make TrueNorth better? We'd love to hear from you.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(onClick = { sendFeedback() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Send Feedback")
                    }
                }
            }
            item {
                SettingsSection(title = "About") {
                    val linkColor = MaterialTheme.colorScheme.primary
                    Text(
                        text = "TrueNorth is an independent study app - not affiliated with, endorsed by, or connected to the Government of Canada or IRCC. Our content is based on the official \"Discover Canada\" guide, but IRCC's rules and the real test can change.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = buildAnnotatedString {
                            append("Always confirm the details for your own test at ")
                            withLink(
                                LinkAnnotation.Url(
                                    url = OFFICIAL_TEST_INFO_URL,
                                    styles = TextLinkStyles(style = SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline))
                                )
                            ) {
                                append("canada.ca")
                            }
                            append(".")
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            item {
                Text(
                    text = "TrueNorth v1.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
        }

        when (val current = dialog) {
            is DeleteAccountDialog.None -> Unit
            is DeleteAccountDialog.Confirm -> AlertDialog(
                onDismissRequest = { dialog = DeleteAccountDialog.None },
                title = { Text("Delete your account?") },
                text = {
                    Text(
                        "This permanently deletes your account, your email, and your saved progress. " +
                            "This can't be undone."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            attemptDelete(onNeedsReauth = { dialog = DeleteAccountDialog.Reauth() })
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { dialog = DeleteAccountDialog.None }) {
                        Text("Cancel")
                    }
                }
            )
            is DeleteAccountDialog.Loading -> AlertDialog(
                onDismissRequest = {},
                confirmButton = {},
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.height(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Deleting your account…")
                    }
                }
            )
            is DeleteAccountDialog.Reauth -> AlertDialog(
                onDismissRequest = { dialog = DeleteAccountDialog.None; reauthPassword = "" },
                title = { Text("Confirm your password") },
                text = {
                    Column {
                        Text("For your security, please re-enter your password to finish deleting your account.")
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = reauthPassword,
                            onValueChange = { reauthPassword = it },
                            label = { Text("Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (current.error != null) {
                            Text(
                                text = current.error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            dialog = DeleteAccountDialog.Loading
                            try {
                                onReauthenticate(reauthPassword)
                                onDeleteAccount()
                                dialog = DeleteAccountDialog.None
                                reauthPassword = ""
                            } catch (e: Exception) {
                                dialog = DeleteAccountDialog.Reauth(error = "Incorrect password. Try again.")
                            }
                        }
                    }) {
                        Text("Confirm & Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { dialog = DeleteAccountDialog.None; reauthPassword = "" }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showDatePicker) {
            TestDatePickerDialog(
                initialMillis = usageState.testDateMillis,
                onConfirm = { millis ->
                    onSetTestDate(millis)
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun ThemeOptionRow(
    option: ThemeOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(text = option.label, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = option.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

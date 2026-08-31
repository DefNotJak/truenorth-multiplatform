package com.truenorth.citizenshiptest.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.truenorth.citizenshiptest.ui.theme.CanadaRed

private enum class OnboardingStep { WELCOME, TEST_DATE, CALENDAR }

/**
 * First-run intro, shown once as a popup over Home: welcome -> optional test
 * date. Not dismissible by back-press/outside-tap - every step has its own
 * explicit forward or skip action. On [onDone], the caller (HomeScreen)
 * follows up with the spotlight feature tutorial ([SpotlightTutorial]) before
 * onboarding is fully marked complete.
 *
 * The independence/trust disclosure ("Where This Comes From") used to be a
 * step here, but got moved to a persistent Settings section instead - it's
 * still a real disclosure, just not another gate a first-time user has to
 * tap through before reaching the app. See CLAUDE.md / roadmap for context.
 */
@Composable
fun OnboardingDialog(
    onSetTestDate: (Long) -> Unit,
    onDone: () -> Unit
) {
    var step by remember { mutableStateOf(OnboardingStep.WELCOME) }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.92f).padding(vertical = 32.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            when (step) {
                OnboardingStep.WELCOME -> WelcomeStep(onContinue = { step = OnboardingStep.TEST_DATE })
                OnboardingStep.TEST_DATE -> TestDateStep(
                    onKnowDate = { step = OnboardingStep.CALENDAR },
                    onSkip = onDone
                )
                OnboardingStep.CALENDAR -> CalendarStep(
                    onConfirm = { millis ->
                        onSetTestDate(millis)
                        onDone()
                    },
                    onSkip = onDone
                )
            }
        }
    }
}

@Composable
private fun WelcomeStep(onContinue: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(CanadaRed, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.School,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Welcome to TrueNorth",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "You're in the right place. We'll help you get ready for the Canadian citizenship test with realistic practice tests, flash cards for every fact, and a clear view of your progress along the way.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
            Text("Let's dive in")
        }
    }
}

@Composable
private fun TestDateStep(onKnowDate: () -> Unit, onSkip: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "First things first - do you have a test date?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Add your citizenship test date and we'll count down with you, so you always know how much time you have left to study.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onKnowDate, modifier = Modifier.fillMaxWidth()) {
            Text("Yes, set my date")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
            Text("I don't have a date yet")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarStep(onConfirm: (Long) -> Unit, onSkip: () -> Unit) {
    val datePickerState = rememberDatePickerState()
    Column(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 8.dp)) {
            Text(
                text = "When is your test?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        DatePicker(
            state = datePickerState,
            showModeToggle = false,
            headline = null,
            title = null
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
        ) {
            TextButton(onClick = onSkip) {
                Text("I don't have a date yet")
            }
            TextButton(
                onClick = { datePickerState.selectedDateMillis?.let(onConfirm) },
                enabled = datePickerState.selectedDateMillis != null
            ) {
                Text("Set Date")
            }
        }
    }
}

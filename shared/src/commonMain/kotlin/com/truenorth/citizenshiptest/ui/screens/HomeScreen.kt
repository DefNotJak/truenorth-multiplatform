package com.truenorth.citizenshiptest.ui.screens

import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.truenorth.citizenshiptest.data.HomeStats
import com.truenorth.citizenshiptest.ui.theme.CanadaRed

private data class MenuItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String
)

private enum class OnboardingPhase { INTRO, TUTORIAL, DONE }

// Fixed items() before the menu list in the LazyColumn: HomeHeader, StatsRow, "Get started" text.
private const val BASE_MENU_LIST_OFFSET = 3

@Composable
fun HomeScreen(
    stats: HomeStats,
    testDateMillis: Long?,
    onboardingCompleted: Boolean,
    privacyConsentGiven: Boolean,
    isDataLoaded: Boolean,
    hasActiveSubscription: Boolean,
    missedQuestionCount: Int,
    onSetTestDate: (Long) -> Unit,
    onOnboardingCompleted: () -> Unit,
    onPrivacyConsentGiven: () -> Unit,
    onUpgradeClick: () -> Unit,
    onStartPracticeTest: () -> Unit,
    onCustomizePracticeTest: () -> Unit,
    onNavigate: (String) -> Unit
) {
    // Keyed on isDataLoaded, not a bare remember - a bare remember captures
    // whatever onboardingCompleted/privacyConsentGiven are on the very first
    // composition, which fires before the real Firestore snapshot arrives (using
    // placeholder defaults of false/false). That locks in "false" permanently on
    // a cold cache (fresh install, cleared data, new sign-in), so the privacy
    // dialog reappeared on every single launch even though the write had
    // genuinely succeeded. Re-keying on isDataLoaded re-initializes exactly once
    // more, the moment real data replaces the placeholder.
    var onboardingPhase by remember(isDataLoaded) { mutableStateOf(if (onboardingCompleted) OnboardingPhase.DONE else OnboardingPhase.INTRO) }
    // Separate flag (not folded into onboardingPhase) so accounts that predate this
    // screen and already have onboardingCompleted = true still see it once too.
    var consentGiven by remember(isDataLoaded) { mutableStateOf(privacyConsentGiven) }
    val listState = rememberLazyListState()
    val menuItemBounds = remember { mutableStateMapOf<String, Rect>() }
    val items = remember(missedQuestionCount) { menuItems(missedQuestionCount) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold { padding ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { HomeHeader() }
                item { StatsRow(stats, testDateMillis) }
                item {
                    Text(
                        text = "Get started",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                item {
                    PracticeTestMenuCard(
                        onClick = onStartPracticeTest,
                        onCustomize = onCustomizePracticeTest,
                        modifier = Modifier.onGloballyPositioned { coordinates ->
                            menuItemBounds[com.truenorth.citizenshiptest.navigation.Routes.PRACTICE_TEST_SETUP] =
                                coordinates.boundsInRoot()
                        }
                    )
                }
                items(items) { menuItem ->
                    MenuCard(
                        menuItem = menuItem,
                        onClick = { onNavigate(menuItem.route) },
                        modifier = Modifier.onGloballyPositioned { coordinates ->
                            menuItemBounds[menuItem.route] = coordinates.boundsInRoot()
                        }
                    )
                }
                if (!hasActiveSubscription) {
                    item { UpgradeBanner(onClick = onUpgradeClick) }
                }
            }
        }

        if (isDataLoaded) {
            when {
                !consentGiven -> PrivacyConsentDialog(
                    onAgree = {
                        consentGiven = true
                        onPrivacyConsentGiven()
                    }
                )
                onboardingPhase == OnboardingPhase.INTRO -> OnboardingDialog(
                    onSetTestDate = onSetTestDate,
                    onDone = {
                        onboardingPhase = OnboardingPhase.TUTORIAL
                        // Persist here, not just at the end of the tutorial - if the app
                        // gets backgrounded or killed partway through the spotlight steps,
                        // the welcome dialog shouldn't come back on the next launch.
                        onOnboardingCompleted()
                    }
                )
                onboardingPhase == OnboardingPhase.TUTORIAL -> SpotlightTutorial(
                    menuItemBounds = menuItemBounds,
                    listState = listState,
                    listIndexForRoute = { route ->
                        if (route == com.truenorth.citizenshiptest.navigation.Routes.PRACTICE_TEST_SETUP) {
                            BASE_MENU_LIST_OFFSET
                        } else {
                            BASE_MENU_LIST_OFFSET + 1 + items.indexOfFirst { it.route == route }
                        }
                    },
                    onFinish = {
                        onboardingPhase = OnboardingPhase.DONE
                        onOnboardingCompleted()
                    }
                )
                else -> Unit
            }
        }
    }
}

@Composable
private fun HomeHeader() {
    Column {
        Text(
            text = "TrueNorth",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Citizenship Test Practice",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun UpgradeBanner(onClick: () -> Unit) {
    // Deliberately styled like a plain MenuCard (not a solid-fill promo banner) -
    // it's an ordinary, low-pressure entry point at the end of the list, not
    // something meant to compete with "Get started" for attention.
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.WorkspacePremium,
                contentDescription = null,
                tint = CanadaRed
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Unlock Full Access", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Unlimited practice tests & favourite questions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.height(20.dp)
            )
        }
    }
}

@Composable
private fun StatsRow(stats: HomeStats, testDateMillis: Long?) {
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            label = "Tests Taken",
            value = stats.testsTaken.toString()
        )
        StatCard(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            label = "Avg Score",
            value = stats.averageScorePercent?.let { "$it%" } ?: "--"
        )
        StatCard(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            label = "Pass Rate",
            value = if (stats.recentTestsCount != null && stats.recentTestsCount > 0) {
                "${stats.recentTestsPassed}/${stats.recentTestsCount}"
            } else {
                "--"
            }
        )
        if (testDateMillis != null) {
            StatCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                label = "Days Left",
                value = daysUntilLabel(testDateMillis)
            )
        }
    }
}

private fun daysUntilLabel(testDateMillis: Long): String {
    // Material3's DatePicker encodes the selected date at UTC midnight, regardless
    // of device timezone. Pull the Y/M/D out as seen in UTC, then diff that
    // calendar date against "today" in the device's own local timezone -
    // comparing raw millis directly would be off by a day in timezones behind UTC.
    val targetDate = Instant.fromEpochMilliseconds(testDateMillis).toLocalDateTime(TimeZone.UTC).date
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val days = today.daysUntil(targetDate)
    return when {
        days > 0 -> "$days"
        days == 0 -> "Today!"
        else -> "Passed"
    }
}

@Composable
private fun StatCard(modifier: Modifier = Modifier, label: String, value: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun PracticeTestMenuCard(
    onClick: () -> Unit,
    onCustomize: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Quiz,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Practice Test", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Starts right away with your last settings",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onCustomize) {
                Icon(
                    imageVector = Icons.Filled.Tune,
                    contentDescription = "Customize practice test",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MenuCard(menuItem: MenuItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = menuItem.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(text = menuItem.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = menuItem.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun menuItems(missedQuestionCount: Int): List<MenuItem> = listOf(
    MenuItem(
        title = "Flash Cards",
        subtitle = "Study By Category, always free",
        icon = Icons.Filled.Style,
        route = com.truenorth.citizenshiptest.navigation.Routes.STUDY
    ),
    MenuItem(
        title = "Favourite Questions",
        subtitle = "Questions you've starred, all in one place",
        icon = Icons.Filled.Star,
        route = com.truenorth.citizenshiptest.navigation.Routes.FAVORITE_QUESTIONS
    ),
    MenuItem(
        title = "Smart Review",
        subtitle = if (missedQuestionCount > 0) {
            "$missedQuestionCount question${if (missedQuestionCount == 1) "" else "s"} to review"
        } else {
            "You're all caught up"
        },
        icon = Icons.Filled.Psychology,
        route = com.truenorth.citizenshiptest.navigation.Routes.SMART_REVIEW
    ),
    MenuItem(
        title = "Progress",
        subtitle = "Your scores and accuracy over time",
        icon = Icons.Filled.BarChart,
        route = com.truenorth.citizenshiptest.navigation.Routes.PROGRESS
    ),
    MenuItem(
        title = "How the Real Test Works",
        subtitle = "Format, pass mark, and what to expect",
        icon = Icons.Filled.Info,
        route = com.truenorth.citizenshiptest.navigation.Routes.INFO
    ),
    MenuItem(
        title = "Settings",
        subtitle = "Dark mode, subscription, and more",
        icon = Icons.Filled.Settings,
        route = com.truenorth.citizenshiptest.navigation.Routes.SETTINGS
    )
)

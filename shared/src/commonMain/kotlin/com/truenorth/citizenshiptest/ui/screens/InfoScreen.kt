package com.truenorth.citizenshiptest.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Groups3
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import truenorth.shared.generated.resources.Res
import truenorth.shared.generated.resources.illustration_id_verify
import truenorth.shared.generated.resources.illustration_results
import truenorth.shared.generated.resources.illustration_test_navigation

private const val OFFICIAL_TEST_INFO_URL =
    "https://www.canada.ca/en/immigration-refugees-citizenship/services/canadian-citizenship/test.html"

private data class InfoSection(
    val icon: ImageVector,
    val title: String,
    val body: String,
    // Original TrueNorth-styled mockups illustrating the real test flow (not
    // screenshots of any official page - see the "Illustration only" caption
    // baked into each image).
    val illustrationRes: DrawableResource? = null
)

private val sections = listOf(
    InfoSection(
        icon = Icons.Filled.Quiz,
        title = "20 questions, 15 to pass",
        body = "The real test has 20 multiple-choice and true/false questions, based on the official \"Discover Canada\" guide. You need at least 15 correct (75%) to pass."
    ),
    InfoSection(
        icon = Icons.Filled.Mail,
        title = "Waiting for your invitation",
        body = "You can't book the test yourself. IRCC emails an invitation, usually 1 to 3 months after they acknowledge receipt of your application, with a 30-day window to take it, your application number, and your UCI. Check your junk folder and look for an address ending in \"@cic.gc.ca\" or \"@canada.ca.\" If you need to reschedule, email the address in your invitation's \"Rescheduling\" section — it usually takes 4 to 8 weeks. If you have an emergency and need to test sooner, you may qualify for urgent processing."
    ),
    InfoSection(
        icon = Icons.Filled.Videocam,
        title = "Taken online, on camera",
        body = "Most applicants take a self-administered test online with a webcam, on a desktop, laptop, or tablet using Chrome or Safari. Phones, Chrome on iPad, Microsoft Surface Pro, and VPNs aren't allowed. Some applicants are instead tested by video (Microsoft Teams) or in person. You can take it in English or French."
    ),
    InfoSection(
        icon = Icons.Filled.Timer,
        title = "45 minutes",
        body = "You get 45 minutes to complete the test once you start it."
    ),
    InfoSection(
        icon = Icons.Filled.TipsAndUpdates,
        title = "Before you start",
        body = "Test from a quiet, well-lit room with your face clearly visible and the light in front of you, not behind. Close other tabs, apps, and notifications, and have photo ID ready (driver's licence, health card, or PR card — an expired PR card is fine, it's still accepted)."
    ),
    InfoSection(
        icon = Icons.Filled.Badge,
        title = "Verifying your identity",
        body = "Before the test starts, you'll take a photo of your face and then your ID. Take your time and make sure they're clear and well lit — if there's a problem, retaking your photos is one of the supported help topics during the test.",
        illustrationRes = Res.drawable.illustration_id_verify
    ),
    InfoSection(
        icon = Icons.Filled.PlaylistAddCheck,
        title = "During the test",
        body = "Stay within view of your camera for the full test, and don't use your browser's back button — the official rules treat it as a hard no, since it can disrupt or end your session.",
        illustrationRes = Res.drawable.illustration_test_navigation
    ),
    InfoSection(
        icon = Icons.Filled.HourglassTop,
        title = "Your score isn't final right away",
        body = "The score shown right after you finish is unofficial. An officer reviews your session, including the webcam photos, before it becomes final, which can take anywhere from days to weeks. You may not get a confirmation email or be able to log back in to see your results later, so save, print, or email them before you close the page.",
        illustrationRes = Res.drawable.illustration_results
    ),
    InfoSection(
        icon = Icons.Filled.EventAvailable,
        title = "If you pass: interview and ceremony",
        body = "Passing means an invitation to the citizenship ceremony. Depending on your case, you may also be invited to an interview first — always if you're 14 to 17 and applying without a parent (you and the person who submitted your application must both attend). Interviews verify your identity, check your documents, and confirm your eligibility; they don't always end in a final decision. Bring the original documents from your application, plus any passport or travel documents you used in the 5 years before you applied."
    ),
    InfoSection(
        icon = Icons.Filled.EventRepeat,
        title = "Up to 3 attempts",
        body = "After IRCC invites you to test, you get a 30-day window with up to 3 attempts to pass."
    ),
    InfoSection(
        icon = Icons.Filled.EventBusy,
        title = "If you miss your test",
        body = "Miss your first online test and IRCC automatically sends a new invitation with a fresh 30-day window and all 3 chances intact. Miss a second one, and you must email the address in your invitation's \"Rescheduling\" section with an explanation, your application number, and your UCI — miss it twice without contacting them and your application is abandoned, meaning you'd have to start over. For in-person or Microsoft Teams tests, contact IRCC after any missed test rather than waiting to be rescheduled."
    ),
    InfoSection(
        icon = Icons.Filled.WifiOff,
        title = "If something goes wrong",
        body = "Stay alone in the room with your camera on, and don't switch to other tabs, programs, or a VPN during the test — the official rules are strict about this. If your connection or camera actually drops, try signing back in right away. If you can't reconnect, contact IRCC as soon as possible, including your application number and UCI. You'll generally have some time to do this (around a month, as an estimate), but the exact deadline is stated in your test invitation, so confirm it there."
    ),
    InfoSection(
        icon = Icons.Filled.Groups3,
        title = "If you don't pass after 3 tries",
        body = "You'll be invited to a hearing with a citizenship officer. They may ask about your knowledge of Canada (an oral version of the test — 20 questions, 15 to pass), your residence in Canada, and may assess your language skills (up to 9 questions, 6 to pass). The hearing lasts 30 to 90 minutes. Pass it, and you're invited to the citizenship ceremony. Fail it, and your application is refused — you'd need to reapply and pay the fees again."
    ),
    InfoSection(
        icon = Icons.Filled.CalendarMonth,
        title = "Exempt if you're 55+",
        body = "If you're 55 or older on the date you sign your citizenship application, you're automatically exempt from both the knowledge test and the language requirement."
    ),
    InfoSection(
        icon = Icons.Filled.Accessibility,
        title = "Other exemptions and accommodations",
        body = "Under 18 is also an automatic exemption, and you can request a waiver in other situations too — ask any time, even after you've received your invitation. If you need accommodation, IRCC offers the test in person (written or oral, alone or with other test takers) or orally by Microsoft Teams, plus alternate formats like Braille."
    ),
    InfoSection(
        icon = Icons.Filled.Checklist,
        title = "What it covers",
        body = "Questions are drawn from six areas: Rights & Responsibilities, Who We Are, Canada's History, How Canadians Govern, Canadian Symbols, and Economy & Geography — the same categories used throughout this app."
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("How the Real Test Works") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val listState = rememberLazyListState()
        val readProgress by remember {
            derivedStateOf {
                val layoutInfo = listState.layoutInfo
                val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()
                if (lastVisible == null || layoutInfo.totalItemsCount == 0) {
                    0f
                } else {
                    val itemFraction = if (lastVisible.size == 0) {
                        1f
                    } else {
                        ((layoutInfo.viewportEndOffset - lastVisible.offset).toFloat() / lastVisible.size)
                            .coerceIn(0f, 1f)
                    }
                    ((lastVisible.index + itemFraction) / layoutInfo.totalItemsCount).coerceIn(0f, 1f)
                }
            }
        }
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LinearProgressIndicator(
                progress = { readProgress },
                modifier = Modifier.fillMaxWidth()
            )
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sections) { section ->
                    InfoCard(section)
                }
                item {
                    DisclaimerCard()
                }
            }
        }
    }
}

@Composable
private fun InfoCard(section: InfoSection) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(modifier = Modifier.padding(16.dp)) {
                Icon(
                    imageVector = section.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = section.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            section.illustrationRes?.let { res ->
                Image(
                    painter = painterResource(res),
                    contentDescription = "Illustration of ${section.title}, not an official government screen",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        }
    }
}

@Composable
private fun DisclaimerCard() {
    val linkColor = MaterialTheme.colorScheme.primary
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Text(
            text = buildAnnotatedString {
                append("TrueNorth is an independent study app and is not affiliated with, endorsed by, or officially connected to the Government of Canada or Immigration, Refugees and Citizenship Canada (IRCC). IRCC rules and test format can change. This summary is current as of 2026. Always check ")
                withLink(
                    LinkAnnotation.Url(
                        url = OFFICIAL_TEST_INFO_URL,
                        styles = TextLinkStyles(
                            style = SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)
                        )
                    )
                ) {
                    append("canada.ca")
                }
                append(" for the latest before your test.")
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
        )
    }
}

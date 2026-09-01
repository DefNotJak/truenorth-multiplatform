package com.truenorth.citizenshiptest.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.truenorth.citizenshiptest.data.Question
import com.truenorth.citizenshiptest.data.QuestionBank
import com.truenorth.citizenshiptest.ui.theme.CanadaRed
import kotlinx.coroutines.delay
import kotlin.time.Clock

private const val PASS_THRESHOLD_PERCENT = 75
private const val TIMER_DURATION_MILLIS = 45 * 60 * 1000L
private const val TIMER_WARNING_THRESHOLD_MILLIS = 5 * 60 * 1000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeTestScreen(
    config: PracticeTestConfig = PracticeTestConfig(),
    favoriteQuestionIds: Set<Int>,
    onToggleFavorite: (Int) -> Unit,
    reportedQuestionIds: Set<Int>,
    onReportQuestion: (Int, String, String) -> Unit,
    hasActiveSubscription: Boolean,
    onUpgradeClick: () -> Unit,
    onBack: () -> Unit,
    onTestSaved: (
        correctCount: Int,
        total: Int,
        categoryTallies: Map<String, Pair<Int, Int>>,
        questionResults: Map<Int, Boolean>
    ) -> Unit = { _, _, _, _ -> }
) {
    var restartKey by rememberSaveable { mutableIntStateOf(0) }
    val questionIds = rememberSaveable(restartKey) {
        QuestionBank.customTestSet(
            count = config.questionCount,
            categories = config.categories,
            questionType = config.questionType,
            restrictToIds = config.restrictToIds
        ).map { it.id }
    }
    val questions = remember(questionIds) { QuestionBank.byIds(questionIds) }
    var currentIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedAnswer by rememberSaveable { mutableStateOf<Int?>(null) }
    var isRevealed by rememberSaveable { mutableStateOf(false) }
    var correctCount by rememberSaveable { mutableIntStateOf(0) }
    // category name -> (correct so far, total answered so far)
    val categoryTallies = rememberSaveable { mutableMapOf<String, Pair<Int, Int>>() }
    // question id -> selected answer index (absent = never reached/unanswered)
    val userAnswers = rememberSaveable { mutableMapOf<Int, Int?>() }

    var timerStartMillis by rememberSaveable { mutableLongStateOf(Clock.System.now().toEpochMilliseconds()) }
    var nowMillis by remember { mutableLongStateOf(Clock.System.now().toEpochMilliseconds()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = Clock.System.now().toEpochMilliseconds()
            delay(1000)
        }
    }
    val remainingMillis = (TIMER_DURATION_MILLIS - (nowMillis - timerStartMillis)).coerceAtLeast(0)
    val isTimeUp = remainingMillis <= 0L
    val isFinished = currentIndex >= questions.size || isTimeUp

    if (isFinished) {
        val timeSpentMillis = remember { (nowMillis - timerStartMillis).coerceIn(0, TIMER_DURATION_MILLIS) }
        LaunchedEffect(Unit) {
            // Only questions actually answered - an unanswered question (ran out of
            // time before reaching it) isn't evidence the user would get it wrong,
            // so it's left untouched rather than recorded as a miss.
            val questionResults = questions.mapNotNull { q ->
                userAnswers[q.id]?.let { selected -> q.id to (selected == q.correctAnswerIndex) }
            }.toMap()
            onTestSaved(correctCount, questions.size, categoryTallies, questionResults)
        }
        ResultsScreen(
            correctCount = correctCount,
            total = questions.size,
            timeSpentMillis = timeSpentMillis,
            questions = questions,
            userAnswers = userAnswers,
            categoryTallies = categoryTallies,
            favoriteQuestionIds = favoriteQuestionIds,
            onToggleFavorite = onToggleFavorite,
            reportedQuestionIds = reportedQuestionIds,
            onReportQuestion = onReportQuestion,
            hasActiveSubscription = hasActiveSubscription,
            onUpgradeClick = onUpgradeClick,
            onRestart = {
                restartKey += 1
                currentIndex = 0
                selectedAnswer = null
                isRevealed = false
                correctCount = 0
                categoryTallies.clear()
                userAnswers.clear()
                timerStartMillis = Clock.System.now().toEpochMilliseconds()
            },
            onDone = onBack
        )
        return
    }

    val question = questions[currentIndex]

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Question ${currentIndex + 1} of ${questions.size}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Text(
                        text = formatDuration(remainingMillis),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (remainingMillis <= TIMER_WARNING_THRESHOLD_MILLIS) {
                            IncorrectRed
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(onClick = { timerStartMillis = Clock.System.now().toEpochMilliseconds() }) {
                        Icon(Icons.Filled.Restore, contentDescription = "Reset timer")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LinearProgressIndicator(
                progress = { (currentIndex + 1f) / questions.size },
                modifier = Modifier.fillMaxWidth()
            )
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { CategoryChip(question.category.displayName) }
                item {
                    Text(
                        text = question.text,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(question.options.indices.toList()) { index ->
                    AnswerOption(
                        text = question.options[index],
                        state = optionState(
                            index = index,
                            correctIndex = question.correctAnswerIndex,
                            selectedIndex = selectedAnswer,
                            isRevealed = isRevealed
                        ),
                        onClick = {
                            if (!isRevealed) {
                                selectedAnswer = index
                                isRevealed = true
                                userAnswers[question.id] = index
                                val categoryName = question.category.displayName
                                val previous = categoryTallies[categoryName] ?: (0 to 0)
                                val wasCorrect = index == question.correctAnswerIndex
                                if (wasCorrect) {
                                    correctCount += 1
                                }
                                categoryTallies[categoryName] = Pair(
                                    previous.first + if (wasCorrect) 1 else 0,
                                    previous.second + 1
                                )
                            }
                        }
                    )
                }
                if (isRevealed) {
                    item {
                        ExplanationCard(
                            question = question,
                            wasCorrect = selectedAnswer == question.correctAnswerIndex
                        )
                    }
                }
            }
            if (isRevealed) {
                Button(
                    onClick = {
                        currentIndex += 1
                        selectedAnswer = null
                        isRevealed = false
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text(if (currentIndex == questions.size - 1) "See Results" else "Next Question")
                }
            }
        }
    }
}

@Composable
private fun ResultsScreen(
    correctCount: Int,
    total: Int,
    timeSpentMillis: Long,
    questions: List<Question>,
    userAnswers: Map<Int, Int?>,
    categoryTallies: Map<String, Pair<Int, Int>>,
    favoriteQuestionIds: Set<Int>,
    onToggleFavorite: (Int) -> Unit,
    reportedQuestionIds: Set<Int>,
    onReportQuestion: (Int, String, String) -> Unit,
    hasActiveSubscription: Boolean,
    onUpgradeClick: () -> Unit,
    onRestart: () -> Unit,
    onDone: () -> Unit
) {
    val percent = (correctCount * 100) / total
    val passed = percent >= PASS_THRESHOLD_PERCENT
    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (passed) "You passed!" else "Keep practising",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (passed) CorrectGreen else MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "$correctCount / $total correct ($percent%)",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = "Completed in ${formatDuration(timeSpentMillis)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = "The real test requires 15 / 20 (75%) to pass.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            item {
                Button(onClick = onRestart, modifier = Modifier.fillMaxWidth()) {
                    Text("Do Another Test")
                }
            }
            item {
                OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                    Text("Back to Home")
                }
            }
            if (!hasActiveSubscription) {
                item { UpgradeNudgeCard(onClick = onUpgradeClick) }
            }
            if (categoryTallies.isNotEmpty()) {
                item {
                    Text(
                        text = "This Test's Categories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                item { CategoryBreakdownSection(categoryTallies) }
            }
            item {
                Text(
                    text = "Review Your Answers",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(questions, key = { it.id }) { question ->
                ReviewCard(
                    question = question,
                    selectedAnswerIndex = userAnswers[question.id],
                    isFavorite = question.id in favoriteQuestionIds,
                    onToggleFavorite = { onToggleFavorite(question.id) },
                    isReported = question.id in reportedQuestionIds,
                    onReport = { reason, note -> onReportQuestion(question.id, reason, note) }
                )
            }
        }
    }
}

@Composable
private fun CategoryBreakdownSection(categoryTallies: Map<String, Pair<Int, Int>>) {
    val rows = categoryTallies.entries
        .map { (name, tally) -> Triple(name, tally.first, tally.second) }
        .sortedBy { (_, correct, total) -> if (total == 0) 0 else (correct * 100) / total }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { (name, correct, total) ->
            val rowPercent = if (total == 0) 0 else (correct * 100) / total
            val barColor = if (rowPercent >= PASS_THRESHOLD_PERCENT) CorrectGreen else IncorrectRed
            Column {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(text = name, modifier = Modifier.weight(1f))
                    Text(
                        text = "$correct/$total",
                        fontWeight = FontWeight.Bold,
                        color = barColor
                    )
                }
                LinearProgressIndicator(
                    progress = { rowPercent / 100f },
                    color = barColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun ReviewCard(
    question: Question,
    selectedAnswerIndex: Int?,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    isReported: Boolean,
    onReport: (reason: String, note: String) -> Unit
) {
    var showReportDialog by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryChip(question.category.displayName)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = if (isFavorite) "Remove from favourites" else "Add to favourites",
                        tint = CanadaRed
                    )
                }
            }
            Text(
                text = question.text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
            )
            if (selectedAnswerIndex == null) {
                Text(
                    text = "Not answered",
                    style = MaterialTheme.typography.labelLarge,
                    color = IncorrectRed,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                question.options.indices.forEach { index ->
                    AnswerOption(
                        text = question.options[index],
                        state = optionState(
                            index = index,
                            correctIndex = question.correctAnswerIndex,
                            selectedIndex = selectedAnswerIndex,
                            isRevealed = true
                        ),
                        enabled = false,
                        onClick = {}
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            ExplanationCard(
                question = question,
                wasCorrect = selectedAnswerIndex == question.correctAnswerIndex,
                wasUnanswered = selectedAnswerIndex == null
            )
            ReportIssueLink(
                reported = isReported,
                onClick = { showReportDialog = true },
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }

    if (showReportDialog) {
        ReportQuestionDialog(
            onSubmit = { reason, note ->
                onReport(reason, note)
                showReportDialog = false
            },
            onDismiss = { showReportDialog = false }
        )
    }
}

@Composable
private fun UpgradeNudgeCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Want more practice?",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Get unlimited tests and save your trickiest questions to Favourites.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

internal fun formatDuration(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

internal enum class OptionState { NEUTRAL, CORRECT, INCORRECT }

internal fun optionState(
    index: Int,
    correctIndex: Int,
    selectedIndex: Int?,
    isRevealed: Boolean
): OptionState {
    if (!isRevealed) return OptionState.NEUTRAL
    return when {
        index == correctIndex -> OptionState.CORRECT
        index == selectedIndex -> OptionState.INCORRECT
        else -> OptionState.NEUTRAL
    }
}

@Composable
private fun CategoryChip(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

private val CorrectGreen = Color(0xFF2E7D32)
private val CorrectGreenBackground = Color(0xFFE8F5E9)
private val IncorrectRed = Color(0xFFC62828)
private val IncorrectRedBackground = Color(0xFFFDECEA)

@Composable
private fun AnswerOption(
    text: String,
    state: OptionState,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val backgroundColor: Color
    val borderColor: Color
    val contentColor: Color
    when (state) {
        OptionState.CORRECT -> {
            backgroundColor = CorrectGreenBackground
            borderColor = CorrectGreen
            contentColor = CorrectGreen
        }
        OptionState.INCORRECT -> {
            backgroundColor = IncorrectRedBackground
            borderColor = IncorrectRed
            contentColor = IncorrectRed
        }
        OptionState.NEUTRAL -> {
            backgroundColor = MaterialTheme.colorScheme.surface
            borderColor = MaterialTheme.colorScheme.outlineVariant
            contentColor = MaterialTheme.colorScheme.onSurface
        }
    }
    val shape = RoundedCornerShape(12.dp)
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.5.dp, color = borderColor, shape = shape),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
            disabledContainerColor = backgroundColor,
            disabledContentColor = contentColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = contentColor,
                fontWeight = if (state == OptionState.NEUTRAL) FontWeight.Normal else FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            when (state) {
                OptionState.CORRECT -> {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Filled.CheckCircle, contentDescription = "Correct answer", tint = contentColor)
                }
                OptionState.INCORRECT -> {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Filled.Cancel, contentDescription = "Your incorrect answer", tint = contentColor)
                }
                OptionState.NEUTRAL -> Unit
            }
        }
    }
}

@Composable
private fun ExplanationCard(question: Question, wasCorrect: Boolean, wasUnanswered: Boolean = false) {
    val backgroundColor = if (wasCorrect) CorrectGreenBackground else IncorrectRedBackground
    val contentColor = if (wasCorrect) CorrectGreen else IncorrectRed
    val leadIn = when {
        wasUnanswered -> "Not answered."
        wasCorrect -> "Correct."
        else -> "Incorrect."
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("$leadIn ") }
                    append(question.explanation)
                    append(" (Discover Canada, ${question.category.displayName})")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor
            )
        }
    }
}

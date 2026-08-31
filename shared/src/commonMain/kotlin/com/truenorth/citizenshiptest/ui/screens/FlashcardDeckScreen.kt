package com.truenorth.citizenshiptest.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.truenorth.citizenshiptest.data.Category
import com.truenorth.citizenshiptest.data.FlashcardProgressRepository
import com.truenorth.citizenshiptest.data.Question
import com.truenorth.citizenshiptest.data.QuestionBank
import com.truenorth.citizenshiptest.data.rememberFlashcardProgressRepository
import com.truenorth.citizenshiptest.ui.theme.CanadaRed
import kotlin.math.abs
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardDeckScreen(
    category: Category,
    favoriteQuestionIds: Set<Int>,
    onToggleFavorite: (Int) -> Unit,
    reportedQuestionIds: Set<Int>,
    onReportQuestion: (Int, String, String) -> Unit,
    onCardViewed: (Int) -> Unit,
    onBack: () -> Unit
) {
    val progressRepository: FlashcardProgressRepository = rememberFlashcardProgressRepository()
    val scope = rememberCoroutineScope()

    val questionIds = rememberSaveable(category) {
        QuestionBank.flashcardEligible(category).map { it.id }
    }
    val questions = remember(questionIds) { QuestionBank.byIds(questionIds) }

    val savedIndex by progressRepository.lastViewedIndex(category).collectAsState(initial = -1)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(category.displayName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        // Wait for the real saved position before creating the pager - rememberPagerState's
        // initialPage is only honored on first composition, so starting at 0 and jumping once
        // the persisted read resolves would cause a visible flash.
        if (savedIndex < 0) {
            Box(modifier = Modifier.fillMaxSize().padding(padding))
            return@Scaffold
        }

        val pageCount = questions.size + 1
        val initialPage = savedIndex.coerceIn(0, questions.size)
        val pagerState = rememberPagerState(initialPage = initialPage) { pageCount }
        var expandedQuestionIds by rememberSaveable { mutableStateOf(setOf<Int>()) }

        LaunchedEffect(pagerState.currentPage) {
            if (pagerState.currentPage < questions.size) {
                progressRepository.setLastViewedIndex(category, pagerState.currentPage)
                onCardViewed(questions[pagerState.currentPage].id)
            }
        }

        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            val progressFraction = if (questions.isEmpty()) 1f else
                (pagerState.currentPage + 1).coerceAtMost(questions.size).toFloat() / questions.size
            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = if (pagerState.currentPage < questions.size)
                    "${pagerState.currentPage + 1} of ${questions.size}"
                else "Complete",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                CardStackBackdrop(modifier = Modifier.fillMaxSize())

                HorizontalPager(
                    state = pagerState,
                    key = { page -> questionIds.getOrNull(page) ?: "completion" },
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    // HorizontalPager already positions each page horizontally as the user
                    // drags - do NOT also translationX here, that would double the motion on
                    // top of the pager's own layout and produce an erratic double-speed swing.
                    // Only add a subtle scale/fade/tilt flourish on top of the native slide.
                    val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                    val absOffset = abs(pageOffset).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                            .graphicsLayer {
                                alpha = 1f - absOffset * 0.5f
                                scaleX = 1f - absOffset * 0.12f
                                scaleY = 1f - absOffset * 0.12f
                                rotationZ = pageOffset * 5f
                            }
                    ) {
                        if (page < questions.size) {
                            val question = questions[page]
                            FlashcardContent(
                                question = question,
                                isSourceExpanded = question.id in expandedQuestionIds,
                                onToggleSource = {
                                    expandedQuestionIds = if (question.id in expandedQuestionIds) {
                                        expandedQuestionIds - question.id
                                    } else {
                                        expandedQuestionIds + question.id
                                    }
                                },
                                isFavorite = question.id in favoriteQuestionIds,
                                onToggleFavorite = { onToggleFavorite(question.id) },
                                isReported = question.id in reportedQuestionIds,
                                onReport = { reason, note -> onReportQuestion(question.id, reason, note) }
                            )
                        } else {
                            DeckCompletionContent(
                                total = questions.size,
                                onStudyAgain = {
                                    expandedQuestionIds = emptySet()
                                    scope.launch {
                                        progressRepository.setLastViewedIndex(category, 0)
                                        pagerState.scrollToPage(0)
                                    }
                                },
                                onBackToCategories = onBack
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Purely decorative fixed stack behind the active card - not real upcoming content. */
@Composable
private fun CardStackBackdrop(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(28.dp)
    Box(modifier = modifier.padding(20.dp)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = 16.dp, y = 16.dp)
                .shadow(elevation = 4.dp, shape = shape)
                .background(Color(0xFFEAE3EF), shape)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = 8.dp, y = 8.dp)
                .shadow(elevation = 6.dp, shape = shape)
                .background(Color(0xFFF4EFF7), shape)
        )
    }
}

@Composable
private fun FlashcardContent(
    question: Question,
    isSourceExpanded: Boolean,
    onToggleSource: () -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    isReported: Boolean,
    onReport: (reason: String, note: String) -> Unit
) {
    var showReportDialog by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(28.dp)
    Card(
        modifier = Modifier.fillMaxSize().shadow(elevation = 12.dp, shape = shape),
        shape = shape
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
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
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = question.flashcardText ?: question.text,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "ANSWER",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = question.options[question.correctAnswerIndex],
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = CanadaRed
            )
            Spacer(modifier = Modifier.height(24.dp))
            SourceToggle(expanded = isSourceExpanded, onClick = onToggleSource)
            AnimatedVisibility(visible = isSourceExpanded) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Discover Canada - ${question.category.displayName}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = question.explanation,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            ReportIssueLink(
                reported = isReported,
                onClick = { showReportDialog = true },
                modifier = Modifier.padding(top = 12.dp)
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

@Composable
private fun SourceToggle(expanded: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = if (expanded) "Hide source" else "Show source",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DeckCompletionContent(
    total: Int,
    onStudyAgain: () -> Unit,
    onBackToCategories: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = CanadaRed,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "You've reviewed all $total cards",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onStudyAgain, modifier = Modifier.fillMaxWidth()) {
            Text("Study Again")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onBackToCategories, modifier = Modifier.fillMaxWidth()) {
            Text("Back to Categories")
        }
    }
}

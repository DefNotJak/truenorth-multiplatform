package com.truenorth.citizenshiptest.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.truenorth.citizenshiptest.data.Category
import com.truenorth.citizenshiptest.data.CategoryBreakdown
import com.truenorth.citizenshiptest.data.QuestionBank

private const val PASS_THRESHOLD_PERCENT = 75
private val CorrectGreen = Color(0xFF2E7D32)
private val IncorrectRed = Color(0xFFC62828)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    reviewedQuestionIds: Set<Int>,
    categoryBreakdown: List<CategoryBreakdown>,
    onCategoryClick: (Category) -> Unit,
    onBack: () -> Unit
) {
    val cardCounts = remember { Category.entries.associateWith { QuestionBank.flashcardEligible(it).size } }
    val reviewedCounts = remember(reviewedQuestionIds) {
        Category.entries.associateWith { category ->
            QuestionBank.flashcardEligible(category).count { it.id in reviewedQuestionIds }
        }
    }
    val accuracyByCategory = remember(categoryBreakdown) {
        categoryBreakdown.associateBy { it.categoryName }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Flash Cards") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Swipe through every fact, organized by category. The answer is always shown, tap the source icon for the Discover Canada explanation.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(Category.entries.toList()) { category ->
                CategoryPickerCard(
                    category = category,
                    cardCount = cardCounts[category] ?: 0,
                    reviewedCount = reviewedCounts[category] ?: 0,
                    accuracy = accuracyByCategory[category.displayName],
                    onClick = { onCategoryClick(category) }
                )
            }
        }
    }
}

@Composable
private fun CategoryPickerCard(
    category: Category,
    cardCount: Int,
    reviewedCount: Int,
    accuracy: CategoryBreakdown?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = iconFor(category),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$cardCount cards • ${reviewStatusLabel(reviewedCount, cardCount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            if (accuracy != null && accuracy.totalCount > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                CategoryAccuracyBar(percent = accuracy.percent)
            }
        }
    }
}

@Composable
private fun CategoryAccuracyBar(percent: Int) {
    val barColor = if (percent >= PASS_THRESHOLD_PERCENT) CorrectGreen else IncorrectRed
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Test accuracy",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$percent%",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = barColor
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(4.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(percent / 100f)
                .fillMaxHeight()
                .background(color = barColor, shape = RoundedCornerShape(4.dp))
        )
    }
}

private fun reviewStatusLabel(reviewedCount: Int, cardCount: Int): String = when {
    cardCount <= 0 || reviewedCount <= 0 -> "Not Started"
    reviewedCount >= cardCount -> "Reviewed"
    else -> "In Progress"
}

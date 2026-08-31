package com.truenorth.citizenshiptest.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.truenorth.citizenshiptest.data.Category
import com.truenorth.citizenshiptest.data.QuestionBank
import com.truenorth.citizenshiptest.data.QuestionType

data class PracticeTestConfig(
    val questionCount: Int = 20,
    val categories: Set<Category> = emptySet(),
    val questionType: QuestionType? = null,
    // Set only for a Smart Review session - never persisted via
    // PracticeTestPreferencesRepository, so it can't leak into the regular
    // "quick start with your last settings" flow.
    val restrictToIds: Set<Int>? = null
)

private val QUESTION_COUNT_OPTIONS = listOf(10, 15, 20, 25, 30)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PracticeTestSetupScreen(
    initialConfig: PracticeTestConfig,
    onStart: (PracticeTestConfig) -> Unit,
    onBack: () -> Unit
) {
    var questionCount by rememberSaveable { mutableIntStateOf(initialConfig.questionCount) }
    var selectedCategories by remember { mutableStateOf(initialConfig.categories) }
    var selectedType by remember { mutableStateOf(initialConfig.questionType) }

    val matchingCount = remember(selectedCategories, selectedType) {
        QuestionBank.matchingQuestionCount(selectedCategories, selectedType)
    }
    val actualCount = minOf(questionCount, matchingCount)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Practice Test Setup") },
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                SetupSection(title = "Number of Questions") {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        QUESTION_COUNT_OPTIONS.forEach { count ->
                            FilterChip(
                                selected = questionCount == count,
                                onClick = { questionCount = count },
                                label = { Text("$count") }
                            )
                        }
                    }
                }
            }
            item {
                SetupSection(title = "Topics") {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedCategories.isEmpty(),
                            onClick = { selectedCategories = emptySet() },
                            label = { Text("All Topics") }
                        )
                        Category.entries.forEach { category ->
                            FilterChip(
                                selected = category in selectedCategories,
                                onClick = {
                                    selectedCategories = if (category in selectedCategories) {
                                        selectedCategories - category
                                    } else {
                                        selectedCategories + category
                                    }
                                },
                                label = { Text(category.displayName) }
                            )
                        }
                    }
                }
            }
            item {
                SetupSection(title = "Question Type") {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedType == null,
                            onClick = { selectedType = null },
                            label = { Text("Mixed") }
                        )
                        FilterChip(
                            selected = selectedType == QuestionType.MULTIPLE_CHOICE,
                            onClick = { selectedType = QuestionType.MULTIPLE_CHOICE },
                            label = { Text("Multiple Choice") }
                        )
                        FilterChip(
                            selected = selectedType == QuestionType.TRUE_FALSE,
                            onClick = { selectedType = QuestionType.TRUE_FALSE },
                            label = { Text("True/False") }
                        )
                    }
                }
            }
            item {
                Text(
                    text = if (actualCount < questionCount) {
                        "Only $actualCount question${if (actualCount == 1) "" else "s"} match these filters."
                    } else {
                        "$actualCount questions match these filters."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                Button(
                    onClick = {
                        onStart(
                            PracticeTestConfig(
                                questionCount = questionCount,
                                categories = selectedCategories,
                                questionType = selectedType
                            )
                        )
                    },
                    enabled = matchingCount > 0,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start Test")
                }
            }
        }
    }
}

@Composable
private fun SetupSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        content()
    }
}

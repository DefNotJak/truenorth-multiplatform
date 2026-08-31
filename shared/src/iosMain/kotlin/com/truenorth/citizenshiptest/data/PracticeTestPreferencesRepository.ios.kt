package com.truenorth.citizenshiptest.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.truenorth.citizenshiptest.ui.screens.PracticeTestConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSUserDefaults

private const val QUESTION_COUNT_KEY = "question_count"
private const val CATEGORIES_KEY = "categories"
private const val QUESTION_TYPE_KEY = "question_type"

actual class PracticeTestPreferencesRepository {
    private val defaults = NSUserDefaults.standardUserDefaults
    private val state = MutableStateFlow(readConfig())

    private fun readConfig(): PracticeTestConfig {
        val questionCount = if (defaults.objectForKey(QUESTION_COUNT_KEY) != null) {
            defaults.integerForKey(QUESTION_COUNT_KEY).toInt()
        } else {
            20
        }
        // NSUserDefaults.stringArrayForKey is a real Foundation API, but Kotlin/Native's
        // interop erases the NSArray<NSString*> element type - cast explicitly.
        @Suppress("UNCHECKED_CAST")
        val categories = ((defaults.stringArrayForKey(CATEGORIES_KEY) as? List<String>) ?: emptyList())
            .mapNotNull { name -> runCatching { Category.valueOf(name) }.getOrNull() }
            .toSet()
        val questionType = (defaults.stringForKey(QUESTION_TYPE_KEY))
            ?.let { name -> runCatching { QuestionType.valueOf(name) }.getOrNull() }
        return PracticeTestConfig(
            questionCount = questionCount,
            categories = categories,
            questionType = questionType
        )
    }

    actual val config: Flow<PracticeTestConfig> = state.asStateFlow()

    actual suspend fun saveConfig(config: PracticeTestConfig) {
        defaults.setInteger(config.questionCount.toLong(), forKey = QUESTION_COUNT_KEY)
        defaults.setObject(config.categories.map { it.name }, forKey = CATEGORIES_KEY)
        if (config.questionType != null) {
            defaults.setObject(config.questionType.name, forKey = QUESTION_TYPE_KEY)
        } else {
            defaults.removeObjectForKey(QUESTION_TYPE_KEY)
        }
        state.value = config
    }
}

@Composable
actual fun rememberPracticeTestPreferencesRepository(): PracticeTestPreferencesRepository =
    remember { PracticeTestPreferencesRepository() }

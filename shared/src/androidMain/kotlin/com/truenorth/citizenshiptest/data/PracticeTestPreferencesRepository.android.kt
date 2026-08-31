package com.truenorth.citizenshiptest.data

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.truenorth.citizenshiptest.ui.screens.PracticeTestConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.practiceTestPrefsDataStore by preferencesDataStore(name = "practice_test_settings")
private val QUESTION_COUNT_KEY = intPreferencesKey("question_count")
private val CATEGORIES_KEY = stringSetPreferencesKey("categories")
private val QUESTION_TYPE_KEY = stringPreferencesKey("question_type")

actual class PracticeTestPreferencesRepository(private val context: Context) {

    actual val config: Flow<PracticeTestConfig> = context.practiceTestPrefsDataStore.data.map { prefs ->
        PracticeTestConfig(
            questionCount = prefs[QUESTION_COUNT_KEY] ?: 20,
            categories = prefs[CATEGORIES_KEY]
                ?.mapNotNull { name -> runCatching { Category.valueOf(name) }.getOrNull() }
                ?.toSet()
                ?: emptySet(),
            questionType = prefs[QUESTION_TYPE_KEY]?.let { name ->
                runCatching { QuestionType.valueOf(name) }.getOrNull()
            }
        )
    }

    actual suspend fun saveConfig(config: PracticeTestConfig) {
        context.practiceTestPrefsDataStore.edit { prefs ->
            prefs[QUESTION_COUNT_KEY] = config.questionCount
            prefs[CATEGORIES_KEY] = config.categories.map { it.name }.toSet()
            if (config.questionType != null) {
                prefs[QUESTION_TYPE_KEY] = config.questionType.name
            } else {
                prefs.remove(QUESTION_TYPE_KEY)
            }
        }
    }
}

@Composable
actual fun rememberPracticeTestPreferencesRepository(): PracticeTestPreferencesRepository {
    val context = LocalContext.current
    return remember { PracticeTestPreferencesRepository(context) }
}

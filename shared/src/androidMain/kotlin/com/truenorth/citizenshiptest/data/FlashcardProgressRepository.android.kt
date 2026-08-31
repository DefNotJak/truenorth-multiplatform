package com.truenorth.citizenshiptest.data

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Separate DataStore file from ThemePreferencesRepository's "settings" - AndroidX DataStore
// crashes if two Context.preferencesDataStore delegates target the same file name.
private val Context.flashcardProgressDataStore by preferencesDataStore(name = "flashcard_progress")

actual class FlashcardProgressRepository(private val context: Context) {

    actual fun lastViewedIndex(category: Category): Flow<Int> {
        val key = intPreferencesKey("last_index_${category.name}")
        return context.flashcardProgressDataStore.data.map { preferences -> preferences[key] ?: 0 }
    }

    actual suspend fun setLastViewedIndex(category: Category, index: Int) {
        val key = intPreferencesKey("last_index_${category.name}")
        context.flashcardProgressDataStore.edit { preferences -> preferences[key] = index }
    }
}

@Composable
actual fun rememberFlashcardProgressRepository(): FlashcardProgressRepository {
    val context = LocalContext.current
    return remember { FlashcardProgressRepository(context) }
}

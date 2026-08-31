package com.truenorth.citizenshiptest.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSUserDefaults

// No AndroidX DataStore on iOS - NSUserDefaults is the equivalent lightweight
// key-value store, wrapped in a per-key StateFlow so lastViewedIndex stays reactive.
actual class FlashcardProgressRepository {
    private val defaults = NSUserDefaults.standardUserDefaults
    private val flows = mutableMapOf<String, MutableStateFlow<Int>>()

    private fun flowFor(category: Category): MutableStateFlow<Int> {
        val key = "last_index_${category.name}"
        return flows.getOrPut(key) { MutableStateFlow(defaults.integerForKey(key).toInt()) }
    }

    actual fun lastViewedIndex(category: Category): Flow<Int> = flowFor(category).asStateFlow()

    actual suspend fun setLastViewedIndex(category: Category, index: Int) {
        defaults.setInteger(index.toLong(), forKey = "last_index_${category.name}")
        flowFor(category).value = index
    }
}

@Composable
actual fun rememberFlashcardProgressRepository(): FlashcardProgressRepository =
    remember { FlashcardProgressRepository() }

package com.truenorth.citizenshiptest.data

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow

expect class FlashcardProgressRepository {
    fun lastViewedIndex(category: Category): Flow<Int>
    suspend fun setLastViewedIndex(category: Category, index: Int)
}

@Composable
expect fun rememberFlashcardProgressRepository(): FlashcardProgressRepository

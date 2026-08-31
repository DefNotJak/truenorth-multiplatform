package com.truenorth.citizenshiptest.data

import androidx.compose.runtime.Composable
import com.truenorth.citizenshiptest.ui.screens.PracticeTestConfig
import kotlinx.coroutines.flow.Flow

expect class PracticeTestPreferencesRepository {
    val config: Flow<PracticeTestConfig>
    suspend fun saveConfig(config: PracticeTestConfig)
}

@Composable
expect fun rememberPracticeTestPreferencesRepository(): PracticeTestPreferencesRepository

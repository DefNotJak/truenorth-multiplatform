package com.truenorth.citizenshiptest.data

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow

expect class ThemePreferencesRepository {
    val themeMode: Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
}

@Composable
expect fun rememberThemePreferencesRepository(): ThemePreferencesRepository

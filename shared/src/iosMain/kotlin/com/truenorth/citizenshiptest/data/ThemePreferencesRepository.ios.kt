package com.truenorth.citizenshiptest.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSUserDefaults

private const val THEME_MODE_KEY = "theme_mode"

actual class ThemePreferencesRepository {
    private val defaults = NSUserDefaults.standardUserDefaults
    private val state = MutableStateFlow(
        defaults.stringForKey(THEME_MODE_KEY)?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
            ?: ThemeMode.SYSTEM
    )

    actual val themeMode: Flow<ThemeMode> = state.asStateFlow()

    actual suspend fun setThemeMode(mode: ThemeMode) {
        defaults.setObject(mode.name, forKey = THEME_MODE_KEY)
        state.value = mode
    }
}

@Composable
actual fun rememberThemePreferencesRepository(): ThemePreferencesRepository =
    remember { ThemePreferencesRepository() }

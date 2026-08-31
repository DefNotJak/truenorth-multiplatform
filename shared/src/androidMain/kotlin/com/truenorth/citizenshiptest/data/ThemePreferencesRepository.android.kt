package com.truenorth.citizenshiptest.data

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")
private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")

actual class ThemePreferencesRepository(private val context: Context) {

    actual val themeMode: Flow<ThemeMode> = context.settingsDataStore.data.map { preferences ->
        preferences[THEME_MODE_KEY]?.let { ThemeMode.valueOf(it) } ?: ThemeMode.SYSTEM
    }

    actual suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode.name
        }
    }
}

@Composable
actual fun rememberThemePreferencesRepository(): ThemePreferencesRepository {
    val context = LocalContext.current
    return remember { ThemePreferencesRepository(context) }
}

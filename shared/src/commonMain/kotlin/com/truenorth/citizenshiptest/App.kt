package com.truenorth.citizenshiptest

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.truenorth.citizenshiptest.data.AuthRepository
import com.truenorth.citizenshiptest.data.ThemeMode
import com.truenorth.citizenshiptest.data.rememberThemePreferencesRepository
import com.truenorth.citizenshiptest.navigation.AppNavHost
import com.truenorth.citizenshiptest.ui.screens.AuthScreen
import com.truenorth.citizenshiptest.ui.theme.TrueNorthTheme
import kotlinx.coroutines.launch

@Composable
fun App() {
    val themePreferences = rememberThemePreferencesRepository()
    val authRepository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()
    val themeMode by themePreferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
    val currentUser by authRepository.currentUser.collectAsState(initial = authRepository.currentUserSnapshot)

    TrueNorthTheme(themeMode = themeMode) {
        val user = currentUser
        if (user == null) {
            AuthScreen(authRepository = authRepository)
        } else {
            AppNavHost(
                themeMode = themeMode,
                onThemeModeChange = { mode -> scope.launch { themePreferences.setThemeMode(mode) } },
                userId = user.uid,
                userEmail = user.email,
                authRepository = authRepository,
                onSignOut = { scope.launch { authRepository.signOut() } }
            )
        }
    }
}

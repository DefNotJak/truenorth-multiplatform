package com.truenorth.citizenshiptest.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.truenorth.citizenshiptest.data.db.getDatabase

@Composable
actual fun rememberTestResultsRepository(): TestResultsRepository {
    val context = LocalContext.current
    return remember { TestResultsRepository(getDatabase(context).testAttemptDao()) }
}

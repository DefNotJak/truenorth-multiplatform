package com.truenorth.citizenshiptest.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.truenorth.citizenshiptest.data.db.getDatabase

@Composable
actual fun rememberTestResultsRepository(): TestResultsRepository =
    remember { TestResultsRepository(getDatabase().testAttemptDao()) }

package com.truenorth.citizenshiptest.data

import androidx.compose.runtime.Composable

/** Human-readable app version string (e.g. "1.0"), for diagnostics like a feedback email footer. */
@Composable
expect fun rememberAppVersionLabel(): String

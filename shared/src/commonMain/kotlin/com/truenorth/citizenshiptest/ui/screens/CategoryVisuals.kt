package com.truenorth.citizenshiptest.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Public
import androidx.compose.ui.graphics.vector.ImageVector
import com.truenorth.citizenshiptest.data.Category

fun iconFor(category: Category): ImageVector = when (category) {
    Category.RIGHTS_RESPONSIBILITIES -> Icons.Filled.Gavel
    Category.WHO_WE_ARE -> Icons.Filled.Groups
    Category.HISTORY -> Icons.Filled.History
    Category.GOVERNMENT -> Icons.Filled.AccountBalance
    Category.SYMBOLS -> Icons.Filled.Flag
    Category.ECONOMY_GEOGRAPHY -> Icons.Filled.Public
}

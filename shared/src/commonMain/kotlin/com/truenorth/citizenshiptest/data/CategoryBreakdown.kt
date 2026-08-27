package com.truenorth.citizenshiptest.data

data class CategoryBreakdown(
    val categoryName: String,
    val correctCount: Int,
    val totalCount: Int
) {
    val percent: Int get() = if (totalCount == 0) 0 else (correctCount * 100) / totalCount
}

data class ScorePoint(
    val timestampMillis: Long,
    val percent: Int
)

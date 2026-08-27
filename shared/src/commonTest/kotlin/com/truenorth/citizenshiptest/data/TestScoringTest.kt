package com.truenorth.citizenshiptest.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TestScoringTest {

    @Test
    fun computeHomeStats_withNoAttempts_returnsZeroedStats() {
        val stats = computeHomeStats(emptyList())

        assertEquals(0, stats.testsTaken)
        assertNull(stats.averageScorePercent)
        assertNull(stats.accuracyPercent)
    }

    @Test
    fun computeHomeStats_averagesScoresAndCountsRecentPasses() {
        val attempts = listOf(
            AttemptRecord(timestampMillis = 1, correctCount = 8, totalQuestions = 10), // 80%
            AttemptRecord(timestampMillis = 2, correctCount = 15, totalQuestions = 20), // 75%
            AttemptRecord(timestampMillis = 3, correctCount = 5, totalQuestions = 10)  // 50%
        )

        val stats = computeHomeStats(attempts)

        assertEquals(3, stats.testsTaken)
        assertEquals(68, stats.averageScorePercent) // (80 + 75 + 50) / 3 = 68.33 -> 68
        assertEquals(70, stats.accuracyPercent) // 28 correct / 40 total = 70%
        assertEquals(2, stats.recentTestsPassed) // 80% and 75% clear the 75% pass bar, 50% doesn't
        assertEquals(3, stats.recentTestsCount)
    }

    @Test
    fun computeCategoryBreakdown_sumsPerCategoryAndSortsAscendingByPercent() {
        val stats = listOf(
            CategoryStatRecord(category = "A", correctCount = 3, totalCount = 4),
            CategoryStatRecord(category = "B", correctCount = 1, totalCount = 4),
            CategoryStatRecord(category = "A", correctCount = 2, totalCount = 4)
        )

        val breakdown = computeCategoryBreakdown(stats)

        assertEquals(listOf("B", "A"), breakdown.map { it.categoryName })
        assertEquals(25, breakdown.first { it.categoryName == "B" }.percent)
        assertEquals(62, breakdown.first { it.categoryName == "A" }.percent) // 5/8 = 62.5 -> 62
    }

    @Test
    fun computeScoreHistory_mapsEachAttemptToItsPercent() {
        val attempts = listOf(AttemptRecord(timestampMillis = 1000, correctCount = 9, totalQuestions = 10))

        val history = computeScoreHistory(attempts)

        assertEquals(listOf(ScorePoint(timestampMillis = 1000, percent = 90)), history)
    }
}

package com.truenorth.citizenshiptest.data

const val PASS_THRESHOLD_PERCENT = 75
private const val RECENT_TESTS_WINDOW = 5

data class HomeStats(
    val testsTaken: Int,
    val averageScorePercent: Int?,
    val accuracyPercent: Int?,
    val recentTestsPassed: Int? = null,
    val recentTestsCount: Int? = null
)

data class AttemptRecord(
    val timestampMillis: Long,
    val correctCount: Int,
    val totalQuestions: Int
)

data class CategoryStatRecord(
    val category: String,
    val correctCount: Int,
    val totalCount: Int
)

fun computeHomeStats(attempts: List<AttemptRecord>): HomeStats {
    if (attempts.isEmpty()) {
        return HomeStats(testsTaken = 0, averageScorePercent = null, accuracyPercent = null)
    }
    val avgScore = attempts.map { (it.correctCount * 100) / it.totalQuestions }.average().toInt()
    val totalCorrect = attempts.sumOf { it.correctCount }
    val totalQuestions = attempts.sumOf { it.totalQuestions }
    val accuracy = if (totalQuestions == 0) 0 else (totalCorrect * 100) / totalQuestions
    val recentAttempts = attempts.takeLast(RECENT_TESTS_WINDOW)
    val recentPassed = recentAttempts.count {
        (it.correctCount * 100) / it.totalQuestions >= PASS_THRESHOLD_PERCENT
    }
    return HomeStats(
        testsTaken = attempts.size,
        averageScorePercent = avgScore,
        accuracyPercent = accuracy,
        recentTestsPassed = recentPassed,
        recentTestsCount = recentAttempts.size
    )
}

fun computeCategoryBreakdown(stats: List<CategoryStatRecord>): List<CategoryBreakdown> {
    return stats.groupBy { it.category }
        .map { (category, rows) ->
            CategoryBreakdown(
                categoryName = category,
                correctCount = rows.sumOf { it.correctCount },
                totalCount = rows.sumOf { it.totalCount }
            )
        }
        .sortedBy { it.percent }
}

fun computeScoreHistory(attempts: List<AttemptRecord>): List<ScorePoint> {
    return attempts.map {
        ScorePoint(timestampMillis = it.timestampMillis, percent = (it.correctCount * 100) / it.totalQuestions)
    }
}

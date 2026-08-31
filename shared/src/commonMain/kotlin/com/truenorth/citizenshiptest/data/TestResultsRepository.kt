package com.truenorth.citizenshiptest.data

import androidx.compose.runtime.Composable
import com.truenorth.citizenshiptest.data.db.CategoryStatEntity
import com.truenorth.citizenshiptest.data.db.QuestionResultEntity
import com.truenorth.citizenshiptest.data.db.TestAttemptDao
import com.truenorth.citizenshiptest.data.db.TestAttemptEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

@Composable
expect fun rememberTestResultsRepository(): TestResultsRepository

class TestResultsRepository(private val dao: TestAttemptDao) {

    suspend fun saveAttempt(
        correctCount: Int,
        total: Int,
        categoryTallies: Map<String, Pair<Int, Int>>
    ) {
        val attemptId = dao.insertAttempt(
            TestAttemptEntity(
                timestampMillis = Clock.System.now().toEpochMilliseconds(),
                correctCount = correctCount,
                totalQuestions = total
            )
        )
        val categoryStats = categoryTallies.map { (category, tally) ->
            CategoryStatEntity(
                attemptId = attemptId,
                category = category,
                correctCount = tally.first,
                totalCount = tally.second
            )
        }
        if (categoryStats.isNotEmpty()) {
            dao.insertCategoryStats(categoryStats)
        }
    }

    fun observeHomeStats(): Flow<HomeStats> =
        dao.observeAttempts().map { attempts -> computeHomeStats(attempts.map(TestAttemptEntity::toRecord)) }

    fun observeCategoryBreakdown(): Flow<List<CategoryBreakdown>> =
        dao.observeCategoryStats().map { stats -> computeCategoryBreakdown(stats.map(CategoryStatEntity::toRecord)) }

    suspend fun recordQuestionResults(results: Map<Int, Boolean>) {
        if (results.isEmpty()) return
        val now = Clock.System.now().toEpochMilliseconds()
        dao.upsertQuestionResults(
            results.map { (questionId, wasCorrect) -> QuestionResultEntity(questionId, wasCorrect, now) }
        )
    }

    fun observeMissedQuestionIds(): Flow<Set<Int>> =
        dao.observeMissedQuestionIds().map { it.toSet() }

    fun observeScoreHistory(): Flow<List<ScorePoint>> =
        dao.observeAttempts().map { attempts -> computeScoreHistory(attempts.map(TestAttemptEntity::toRecord)) }

    suspend fun clearAllData() {
        dao.deleteAllCategoryStats()
        dao.deleteAllAttempts()
        dao.deleteAllQuestionResults()
    }
}

private fun TestAttemptEntity.toRecord() = AttemptRecord(timestampMillis, correctCount, totalQuestions)
private fun CategoryStatEntity.toRecord() = CategoryStatRecord(category, correctCount, totalCount)

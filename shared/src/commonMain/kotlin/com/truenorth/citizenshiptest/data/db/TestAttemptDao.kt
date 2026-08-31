package com.truenorth.citizenshiptest.data.db

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TestAttemptDao {
    @Insert
    suspend fun insertAttempt(attempt: TestAttemptEntity): Long

    @Insert
    suspend fun insertCategoryStats(stats: List<CategoryStatEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertQuestionResults(results: List<QuestionResultEntity>)

    @Query("SELECT * FROM test_attempts ORDER BY timestampMillis ASC")
    fun observeAttempts(): Flow<List<TestAttemptEntity>>

    @Query("SELECT * FROM category_stats")
    fun observeCategoryStats(): Flow<List<CategoryStatEntity>>

    @Query("SELECT questionId FROM question_results WHERE wasCorrect = 0")
    fun observeMissedQuestionIds(): Flow<List<Int>>

    @Query("DELETE FROM test_attempts")
    suspend fun deleteAllAttempts()

    @Query("DELETE FROM category_stats")
    suspend fun deleteAllCategoryStats()

    @Query("DELETE FROM question_results")
    suspend fun deleteAllQuestionResults()
}

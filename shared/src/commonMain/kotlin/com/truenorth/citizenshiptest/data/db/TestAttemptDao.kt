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

    // --- Cloud sync ---
    //
    // One-shot suspend reads rather than Flows. The sync path needs a consistent
    // snapshot to work from and then finishes; a Flow would have it reacting to
    // its own writes.

    @Query("SELECT * FROM test_attempts ORDER BY timestampMillis ASC")
    suspend fun getAllAttempts(): List<TestAttemptEntity>

    @Query("SELECT * FROM test_attempts WHERE syncedAtMillis IS NULL ORDER BY timestampMillis ASC")
    suspend fun getUnsyncedAttempts(): List<TestAttemptEntity>

    @Query("UPDATE test_attempts SET syncedAtMillis = :syncedAtMillis WHERE id = :id")
    suspend fun markAttemptSynced(id: Long, syncedAtMillis: Long)

    @Query("SELECT * FROM category_stats WHERE attemptId = :attemptId")
    suspend fun getCategoryStatsForAttempt(attemptId: Long): List<CategoryStatEntity>

    @Query("SELECT questionId FROM question_results WHERE wasCorrect = 0")
    suspend fun getMissedQuestionIds(): List<Int>

    /**
     * Every question with a local row, right or wrong. Restore uses this to
     * avoid overwriting a local result with a cloud one: a question answered
     * correctly on this device must not be pushed back into Smart Review
     * because an older cloud snapshot still lists it as missed.
     */
    @Query("SELECT questionId FROM question_results")
    suspend fun getAllQuestionResultIds(): List<Int>

    @Query("SELECT ownerUid FROM sync_state WHERE id = 0")
    suspend fun getOwnerUid(): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSyncState(state: SyncStateEntity)
}

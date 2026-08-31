package com.truenorth.citizenshiptest.data.db

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "test_attempts")
data class TestAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMillis: Long,
    val correctCount: Int,
    val totalQuestions: Int
)

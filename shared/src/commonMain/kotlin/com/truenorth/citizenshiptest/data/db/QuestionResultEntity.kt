package com.truenorth.citizenshiptest.data.db

import androidx.room3.Entity

/**
 * Tracks only the most recent outcome per question (not full history) - "missed"
 * means the last attempt was wrong, and answering it correctly again in any
 * later practice session (including Smart Review) overwrites the row, clearing it.
 */
@Entity(tableName = "question_results", primaryKeys = ["questionId"])
data class QuestionResultEntity(
    val questionId: Int,
    val wasCorrect: Boolean,
    val lastAttemptMillis: Long
)

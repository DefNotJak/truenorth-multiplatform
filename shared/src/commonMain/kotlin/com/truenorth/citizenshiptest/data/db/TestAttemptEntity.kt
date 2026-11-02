package com.truenorth.citizenshiptest.data.db

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "test_attempts")
data class TestAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMillis: Long,
    val correctCount: Int,
    val totalQuestions: Int,
    /**
     * When this attempt was mirrored to Firestore, or null if it still needs
     * uploading. Nullable rather than a boolean so that if sync ever misbehaves
     * it can be diagnosed from *when* each row went up, not just whether it did.
     *
     * Every row that existed before the schema 1 to 2 migration gets null, which
     * is what makes an existing user's local-only history upload on first launch
     * instead of being stranded.
     */
    val syncedAtMillis: Long? = null
)

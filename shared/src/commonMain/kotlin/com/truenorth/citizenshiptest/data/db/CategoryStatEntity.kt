package com.truenorth.citizenshiptest.data.db

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "category_stats")
data class CategoryStatEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val attemptId: Long,
    val category: String,
    val correctCount: Int,
    val totalCount: Int
)

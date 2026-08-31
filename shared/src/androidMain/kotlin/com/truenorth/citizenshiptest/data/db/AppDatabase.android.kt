package com.truenorth.citizenshiptest.data.db

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase

private var instance: AppDatabase? = null
private val instanceLock = Any()

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val dbFile = context.applicationContext.getDatabasePath("truenorth.db")
    return Room.databaseBuilder<AppDatabase>(
        context = context.applicationContext,
        name = dbFile.absolutePath
    )
}

fun getDatabase(context: Context): AppDatabase {
    return instance ?: synchronized(instanceLock) {
        instance ?: buildDatabase(getDatabaseBuilder(context)).also { instance = it }
    }
}

package com.truenorth.citizenshiptest.data.db

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlinx.coroutines.Dispatchers

@Database(
    entities = [
        TestAttemptEntity::class,
        CategoryStatEntity::class,
        QuestionResultEntity::class,
        SyncStateEntity::class
    ],
    version = 2,
    exportSchema = true
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun testAttemptDao(): TestAttemptDao
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

/**
 * Adds `test_attempts.syncedAtMillis` and the `sync_state` table for the
 * Firestore mirror.
 *
 * Written by hand on purpose. `fallbackToDestructiveMigration` would have been
 * one line and would have wiped the exact test history that cloud sync exists
 * to preserve, on every existing install, at the moment of the upgrade.
 *
 * The CREATE TABLE below must match what Room generates for [SyncStateEntity]
 * character for character, including the backticks and the trailing
 * `PRIMARY KEY(...)` clause, or Room's identity check fails when the database
 * is opened. If [SyncStateEntity] changes, this statement changes with it, and
 * the exported schema under `shared/schemas` is how to confirm the two agree.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE `test_attempts` ADD COLUMN `syncedAtMillis` INTEGER")
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `sync_state` " +
                "(`id` INTEGER NOT NULL, `ownerUid` TEXT, PRIMARY KEY(`id`))"
        )
    }
}

fun buildDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase {
    return builder
        .addMigrations(MIGRATION_1_2)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.Default)
        .build()
}

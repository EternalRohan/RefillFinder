package com.refillfinder.app.data.local.db

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.refillfinder.app.data.local.converter.Converters
import com.refillfinder.app.data.local.dao.*
import com.refillfinder.app.data.local.entity.*

/**
 * ReFillDatabase — Room Database singleton.
 *
 * Syllabus:
 *  - Room Database Architecture
 *  - @TypeConverters (Timestamp, Date, List)
 *  - Database Migration (v1 → v2 example)
 *  - Singleton pattern with thread-safety (@Volatile + synchronized)
 */
@Database(
    entities = [CoolerEntity::class, ReportEntity::class, ChatMessageEntity::class, UserEntity::class, HydrationEntity::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ReFillDatabase : RoomDatabase() {

    abstract fun coolerDao(): CoolerDao
    abstract fun reportDao(): ReportDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun userDao(): UserDao
    abstract fun hydrationDao(): HydrationDao

    companion object {
        @Volatile
        private var INSTANCE: ReFillDatabase? = null

        // ── Migration: v1 → v2 (added lastUpdated index) ─────────────────
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Example: add a column in a future migration
                // db.execSQL("ALTER TABLE coolers ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
                // For now — no-op since we're still on schema v1 logic
            }
        }

        fun getInstance(context: Context): ReFillDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    ReFillDatabase::class.java,
                    "refillfinder.db"
                )
                    .addMigrations(MIGRATION_1_2)          // Safe migration
                    .fallbackToDestructiveMigration()       // Dev fallback
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

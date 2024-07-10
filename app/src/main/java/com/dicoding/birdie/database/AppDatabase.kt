package com.dicoding.birdie.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [AnalysisResult::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun analysisResultDao(): AnalysisResultDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java, "birdie_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build().also { instance = it }
            }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create the new table without the 'score' column
                database.execSQL("""
                    CREATE TABLE analysis_results_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        label TEXT NOT NULL,
                        imageUri TEXT NOT NULL
                    )
                """.trimIndent())

                // Copy the data from the old table to the new table
                database.execSQL("""
                    INSERT INTO analysis_results_new (id, label, imageUri)
                    SELECT id, label, imageUri FROM analysis_results
                """.trimIndent())

                // Remove the old table
                database.execSQL("DROP TABLE analysis_results")

                // Rename the new table to the old table name
                database.execSQL("ALTER TABLE analysis_results_new RENAME TO analysis_results")
            }
        }
    }
}
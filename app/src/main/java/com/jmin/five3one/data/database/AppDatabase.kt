package com.jmin.five3one.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.jmin.five3one.data.converter.TypeConverters as AppTypeConverters
import com.jmin.five3one.data.dao.*
import com.jmin.five3one.data.model.*

/**
 * 应用主数据库
 */
@Database(
    entities = [
        OneRM::class,
        TrainingMax::class,
        PlateConfig::class,
        WorkoutHistory::class,
        AppSettings::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun oneRMDao(): OneRMDao
    abstract fun trainingMaxDao(): TrainingMaxDao
    abstract fun plateConfigDao(): PlateConfigDao
    abstract fun workoutHistoryDao(): WorkoutHistoryDao
    abstract fun appSettingsDao(): AppSettingsDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 添加isDarkMode字段到app_settings表
                database.execSQL("ALTER TABLE app_settings ADD COLUMN isDarkMode INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "five3one_database"
                ).addMigrations(MIGRATION_1_2).build()
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * 用于测试的内存数据库
         */
        fun getInMemoryDatabase(context: Context): AppDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                AppDatabase::class.java
            ).build()
        }
    }
}

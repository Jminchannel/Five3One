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
        AppSettings::class,
        ExerciseTutorial::class,
        ExerciseTutorialContent::class,
        TutorialProgress::class,
        UserTrainingSchedule::class
    ],
    version = 8,
    exportSchema = false
)
@TypeConverters(AppTypeConverters::class, TrainingScheduleConverters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun oneRMDao(): OneRMDao
    abstract fun trainingMaxDao(): TrainingMaxDao
    abstract fun plateConfigDao(): PlateConfigDao
    abstract fun workoutHistoryDao(): WorkoutHistoryDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun exerciseTutorialDao(): ExerciseTutorialDao
    abstract fun trainingScheduleDao(): TrainingScheduleDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 添加isDarkMode字段到app_settings表
                database.execSQL("ALTER TABLE app_settings ADD COLUMN isDarkMode INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 创建exercise_tutorials表
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS exercise_tutorials (
                        exerciseType TEXT NOT NULL PRIMARY KEY,
                        videoUrl TEXT,
                        gifUrl TEXT,
                        commonMistakesVideoUrl TEXT,
                        muscleGroupImageUrl TEXT,
                        difficulty TEXT NOT NULL DEFAULT 'BEGINNER',
                        estimatedDurationMinutes INTEGER NOT NULL DEFAULT 5,
                        isEnabled INTEGER NOT NULL DEFAULT 1,
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0
                    )
                """)
                
                // 创建exercise_tutorial_content表
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS exercise_tutorial_content (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        exerciseType TEXT NOT NULL,
                        language TEXT NOT NULL,
                        title TEXT NOT NULL,
                        shortDescription TEXT NOT NULL,
                        keyPoints TEXT NOT NULL,
                        mentalCues TEXT NOT NULL,
                        commonMistakes TEXT NOT NULL,
                        targetMuscles TEXT NOT NULL,
                        faqItems TEXT NOT NULL,
                        safetyTips TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """)
                
                // 创建索引
                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS index_exercise_tutorial_content_exerciseType_language
                    ON exercise_tutorial_content(exerciseType, language)
                """)
                
                // 创建tutorial_progress表
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS tutorial_progress (
                        id TEXT NOT NULL PRIMARY KEY,
                        exerciseType TEXT NOT NULL,
                        language TEXT NOT NULL,
                        isCompleted INTEGER NOT NULL DEFAULT 0,
                        lastViewedAt INTEGER NOT NULL DEFAULT 0,
                        viewCount INTEGER NOT NULL DEFAULT 0,
                        completedAt INTEGER
                    )
                """)
                
                // 创建索引以提高查询性能
                database.execSQL("CREATE INDEX IF NOT EXISTS index_exercise_tutorial_content_exerciseType_language ON exercise_tutorial_content(exerciseType, language)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_tutorial_progress_exerciseType_language ON tutorial_progress(exerciseType, language)")
            }
        }
        
        @JvmField
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 修复exercise_tutorial_content表的结构问题
                // 检查表是否存在
                val cursor = database.query("SELECT name FROM sqlite_master WHERE type='table' AND name='exercise_tutorial_content'")
                val tableExists = cursor.moveToFirst()
                cursor.close()
                
                if (tableExists) {
                    // 如果表存在，重新创建表
                    database.execSQL("DROP TABLE exercise_tutorial_content")
                }
                
                // 创建新的表结构
                database.execSQL("""
                    CREATE TABLE exercise_tutorial_content (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        exerciseType TEXT NOT NULL,
                        language TEXT NOT NULL,
                        title TEXT NOT NULL,
                        shortDescription TEXT NOT NULL,
                        keyPoints TEXT NOT NULL,
                        mentalCues TEXT NOT NULL,
                        commonMistakes TEXT NOT NULL,
                        targetMuscles TEXT NOT NULL,
                        faqItems TEXT NOT NULL,
                        safetyTips TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """)
                
                // 创建索引
                database.execSQL("""
                    CREATE INDEX index_exercise_tutorial_content_exerciseType_language
                    ON exercise_tutorial_content(exerciseType, language)
                """)
            }
        }
        
        @JvmField
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 完全重新创建所有教程相关的表以确保结构正确
                
                // 删除所有教程相关的表（如果存在）
                database.execSQL("DROP TABLE IF EXISTS exercise_tutorials")
                database.execSQL("DROP TABLE IF EXISTS exercise_tutorial_content")
                database.execSQL("DROP TABLE IF EXISTS tutorial_progress")
                
                // 重新创建exercise_tutorials表
                database.execSQL("""
                    CREATE TABLE exercise_tutorials (
                        exerciseType TEXT NOT NULL PRIMARY KEY,
                        videoUrl TEXT,
                        gifUrl TEXT,
                        commonMistakesVideoUrl TEXT,
                        muscleGroupImageUrl TEXT,
                        difficulty TEXT NOT NULL,
                        estimatedDurationMinutes INTEGER NOT NULL,
                        isEnabled INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """)
                
                // 重新创建exercise_tutorial_content表
                database.execSQL("""
                    CREATE TABLE exercise_tutorial_content (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        exerciseType TEXT NOT NULL,
                        language TEXT NOT NULL,
                        title TEXT NOT NULL,
                        shortDescription TEXT NOT NULL,
                        keyPoints TEXT NOT NULL,
                        mentalCues TEXT NOT NULL,
                        commonMistakes TEXT NOT NULL,
                        targetMuscles TEXT NOT NULL,
                        faqItems TEXT NOT NULL,
                        safetyTips TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """)
                
                // 创建索引
                database.execSQL("""
                    CREATE INDEX index_exercise_tutorial_content_exerciseType_language
                    ON exercise_tutorial_content(exerciseType, language)
                """)
                
                // 重新创建tutorial_progress表
                database.execSQL("""
                    CREATE TABLE tutorial_progress (
                        id TEXT NOT NULL PRIMARY KEY,
                        exerciseType TEXT NOT NULL,
                        language TEXT NOT NULL,
                        isCompleted INTEGER NOT NULL,
                        lastViewedAt INTEGER NOT NULL,
                        viewCount INTEGER NOT NULL,
                        completedAt INTEGER
                    )
                """)
                
                // 创建索引
                database.execSQL("""
                    CREATE INDEX index_tutorial_progress_exerciseType_language
                    ON tutorial_progress(exerciseType, language)
                """)
            }
        }
        
        @JvmField
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 创建训练计划表
                database.execSQL("""
                    CREATE TABLE training_schedules (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        templateType TEXT NOT NULL,
                        startDate TEXT NOT NULL,
                        currentWeek INTEGER NOT NULL,
                        currentCycle INTEGER NOT NULL,
                        trainingDayRecords TEXT NOT NULL,
                        isActive INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """)
                
                // 创建索引以提高查询性能
                database.execSQL("""
                    CREATE INDEX index_training_schedules_isActive 
                    ON training_schedules(isActive)
                """)
            }
        }
        
        @JvmField
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 重新创建training_schedules表以匹配实体定义
                database.execSQL("DROP TABLE IF EXISTS training_schedules")
                
                // 创建符合实体定义的表结构
                database.execSQL("""
                    CREATE TABLE training_schedules (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        templateType TEXT NOT NULL,
                        startDate TEXT NOT NULL,
                        currentWeek INTEGER NOT NULL,
                        currentCycle INTEGER NOT NULL,
                        trainingDayRecords TEXT NOT NULL,
                        isActive INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """)
                
                // 创建索引（这次会被Room正确识别）
                database.execSQL("""
                    CREATE INDEX index_training_schedules_isActive 
                    ON training_schedules(isActive)
                """)
            }
        }
        
        @JvmField
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 为app_settings表添加isDebugMode字段
                database.execSQL("ALTER TABLE app_settings ADD COLUMN isDebugMode INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "five3one_database"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8).build()
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

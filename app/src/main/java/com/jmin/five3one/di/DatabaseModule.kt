package com.jmin.five3one.di

import android.content.Context
import androidx.room.Room
import com.jmin.five3one.data.dao.*
import com.jmin.five3one.data.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 数据库依赖注入模块
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "five3one_database"
        )
        .fallbackToDestructiveMigration() // 开发阶段使用，生产环境需要实现Migration
        .build()
    }
    
    @Provides
    fun provideOneRMDao(database: AppDatabase): OneRMDao {
        return database.oneRMDao()
    }
    
    @Provides
    fun provideTrainingMaxDao(database: AppDatabase): TrainingMaxDao {
        return database.trainingMaxDao()
    }
    
    @Provides
    fun providePlateConfigDao(database: AppDatabase): PlateConfigDao {
        return database.plateConfigDao()
    }
    
    @Provides
    fun provideWorkoutHistoryDao(database: AppDatabase): WorkoutHistoryDao {
        return database.workoutHistoryDao()
    }
    
    @Provides
    fun provideAppSettingsDao(database: AppDatabase): AppSettingsDao {
        return database.appSettingsDao()
    }
}

package com.jmin.five3one.di

import com.jmin.five3one.data.dao.*
import com.jmin.five3one.data.repository.PlateCalculatorRepository
import com.jmin.five3one.data.repository.UserDataRepository
import com.jmin.five3one.data.repository.WorkoutRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository依赖注入模块
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideUserDataRepository(
        oneRMDao: OneRMDao,
        trainingMaxDao: TrainingMaxDao,
        plateConfigDao: PlateConfigDao,
        appSettingsDao: AppSettingsDao
    ): UserDataRepository {
        return UserDataRepository(oneRMDao, trainingMaxDao, plateConfigDao, appSettingsDao)
    }
    
    @Provides
    @Singleton
    fun provideWorkoutRepository(
        workoutHistoryDao: WorkoutHistoryDao
    ): WorkoutRepository {
        return WorkoutRepository(workoutHistoryDao)
    }
    
    @Provides
    @Singleton
    fun providePlateCalculatorRepository(): PlateCalculatorRepository {
        return PlateCalculatorRepository()
    }
}

package com.jmin.five3one.data.repository

import com.jmin.five3one.data.dao.WorkoutHistoryDao
import com.jmin.five3one.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 训练数据仓库
 * 管理训练历史记录、训练进度等
 */
@Singleton
class WorkoutRepository @Inject constructor(
    private val workoutHistoryDao: WorkoutHistoryDao
) {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    /**
     * 获取所有训练记录
     */
    fun getAllWorkouts(): Flow<List<WorkoutHistory>> = workoutHistoryDao.getAllWorkouts()
    
    /**
     * 获取最近的训练记录
     */
    fun getRecentWorkouts(limit: Int = 10): Flow<List<WorkoutHistory>> = 
        workoutHistoryDao.getRecentWorkouts(limit)
    
    /**
     * 根据动作获取训练记录
     */
    fun getWorkoutsByLift(lift: LiftType): Flow<List<WorkoutHistory>> = 
        workoutHistoryDao.getWorkoutsByLift(lift)
    
    /**
     * 获取训练统计信息
     */
    fun getWorkoutStats(): Flow<WorkoutStats> = 
        workoutHistoryDao.getAllWorkouts().map { workouts ->
            WorkoutStats.fromWorkouts(workouts)
        }
    
    /**
     * 获取当前训练周期进度
     */
    fun getCurrentCycleProgress(): Flow<CycleProgress> = 
        workoutHistoryDao.getAllWorkouts().map { workouts ->
            calculateCycleProgress(workouts)
        }
    
    /**
     * 保存训练记录
     */
    suspend fun saveWorkout(workout: WorkoutHistory): Long {
        return workoutHistoryDao.insertWorkout(workout)
    }
    
    /**
     * 创建并保存训练记录
     */
    suspend fun createWorkout(
        lift: LiftType,
        week: Int,
        day: Int,
        template: TemplateType,
        trainingMax: Double,
        sets: List<WorkoutSet>,
        duration: Long = 0,
        notes: String = "",
        feeling: WorkoutFeeling = WorkoutFeeling.GOOD
    ): Long {
        val workout = WorkoutHistory(
            date = dateFormat.format(Date()),
            lift = lift,
            week = week,
            day = day,
            template = template,
            trainingMax = trainingMax,
            sets = sets,
            duration = duration,
            notes = notes,
            feeling = feeling
        )
        return saveWorkout(workout)
    }
    
    /**
     * 更新训练记录
     */
    suspend fun updateWorkout(workout: WorkoutHistory) {
        workoutHistoryDao.updateWorkout(workout)
    }
    
    /**
     * 删除训练记录
     */
    suspend fun deleteWorkout(workout: WorkoutHistory) {
        workoutHistoryDao.deleteWorkout(workout)
    }
    
    /**
     * 获取指定日期和动作的训练记录
     */
    suspend fun hasWorkoutOnDate(date: String, lift: LiftType): Boolean {
        return workoutHistoryDao.hasWorkoutOnDate(date, lift) > 0
    }
    
    /**
     * 获取最后一次训练记录
     */
    suspend fun getLastWorkout(): WorkoutHistory? {
        return workoutHistoryDao.getLastWorkout()
    }
    
    /**
     * 计算当前周期进度
     */
    private fun calculateCycleProgress(workouts: List<WorkoutHistory>): CycleProgress {
        if (workouts.isEmpty()) {
            return CycleProgress()
        }
        
        val lastWorkout = workouts.first() // 最新的训练记录
        val currentWeek = lastWorkout.week
        val currentDay = lastWorkout.day
        
        // 计算当前周期内已完成的训练数量
        val currentCycleWorkouts = workouts.filter { 
            // 简化处理：假设相同周期的训练在时间上是连续的
            it.week <= currentWeek
        }
        
        var nextWeek = currentWeek
        var nextDay = currentDay + 1
        var cycleNumber = 1 // 简化处理：暂时固定为1
        
        if (nextDay > 4) {
            nextDay = 1
            nextWeek++
            if (nextWeek > 4) {
                nextWeek = 1
                cycleNumber++
            }
        }
        
        return CycleProgress(
            currentWeek = nextWeek,
            currentDay = nextDay,
            cycleNumber = cycleNumber,
            completedWorkouts = currentCycleWorkouts.size
        )
    }
    
    /**
     * 删除所有训练记录
     */
    suspend fun deleteAllWorkouts() {
        workoutHistoryDao.deleteAllWorkouts()
    }
}

/**
 * 训练统计信息
 */
data class WorkoutStats(
    val totalWorkouts: Int = 0,
    val completedCycles: Int = 0,
    val averageAmrapReps: Double = 0.0,
    val completionRate: Double = 100.0,
    val liftProgress: Map<LiftType, LiftProgress> = emptyMap()
) {
    companion object {
        fun fromWorkouts(workouts: List<WorkoutHistory>): WorkoutStats {
            if (workouts.isEmpty()) {
                return WorkoutStats()
            }
            
            val totalWorkouts = workouts.size
            val completedCycles = totalWorkouts / 16 // 4周 × 4天
            
            // 计算平均AMRAP次数
            val amrapWorkouts = workouts.filter { it.amrapReps > 0 }
            val averageAmrapReps = if (amrapWorkouts.isNotEmpty()) {
                amrapWorkouts.map { it.amrapReps }.average()
            } else 0.0
            
            // 计算各动作的进步情况
            val liftProgress = LiftType.values().associateWith { lift ->
                val liftWorkouts = workouts.filter { it.lift == lift }
                LiftProgress.fromWorkouts(liftWorkouts)
            }
            
            return WorkoutStats(
                totalWorkouts = totalWorkouts,
                completedCycles = completedCycles,
                averageAmrapReps = averageAmrapReps,
                completionRate = 100.0, // 简化处理：假设所有记录的训练都已完成
                liftProgress = liftProgress
            )
        }
    }
}

/**
 * 单个动作的进步统计
 */
data class LiftProgress(
    val initialTM: Double = 0.0,
    val currentTM: Double = 0.0,
    val progressPercentage: Double = 0.0,
    val bestAmrap: Int = 0,
    val averageAmrap: Double = 0.0,
    val totalWorkouts: Int = 0
) {
    companion object {
        fun fromWorkouts(workouts: List<WorkoutHistory>): LiftProgress {
            if (workouts.isEmpty()) {
                return LiftProgress()
            }
            
            val sortedWorkouts = workouts.sortedBy { it.date }
            val initialTM = sortedWorkouts.first().trainingMax
            val currentTM = sortedWorkouts.last().trainingMax
            val progressPercentage = if (initialTM > 0) {
                ((currentTM - initialTM) / initialTM) * 100
            } else 0.0
            
            val amrapReps = workouts.map { it.amrapReps }.filter { it > 0 }
            val bestAmrap = amrapReps.maxOrNull() ?: 0
            val averageAmrap = if (amrapReps.isNotEmpty()) amrapReps.average() else 0.0
            
            return LiftProgress(
                initialTM = initialTM,
                currentTM = currentTM,
                progressPercentage = progressPercentage,
                bestAmrap = bestAmrap,
                averageAmrap = averageAmrap,
                totalWorkouts = workouts.size
            )
        }
    }
}

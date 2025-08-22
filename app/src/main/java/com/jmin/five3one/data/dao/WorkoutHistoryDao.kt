package com.jmin.five3one.data.dao

import androidx.room.*
import com.jmin.five3one.data.model.LiftType
import com.jmin.five3one.data.model.WorkoutHistory
import kotlinx.coroutines.flow.Flow

/**
 * 训练历史数据访问对象
 */
@Dao
interface WorkoutHistoryDao {
    
    /**
     * 获取所有训练记录（按日期降序）
     */
    @Query("SELECT * FROM workout_history ORDER BY date DESC, createdAt DESC")
    fun getAllWorkouts(): Flow<List<WorkoutHistory>>
    
    /**
     * 获取最近N次训练记录
     */
    @Query("SELECT * FROM workout_history ORDER BY date DESC, createdAt DESC LIMIT :limit")
    fun getRecentWorkouts(limit: Int = 10): Flow<List<WorkoutHistory>>
    
    /**
     * 根据动作类型获取训练记录
     */
    @Query("SELECT * FROM workout_history WHERE lift = :lift ORDER BY date DESC")
    fun getWorkoutsByLift(lift: LiftType): Flow<List<WorkoutHistory>>
    
    /**
     * 获取指定日期范围的训练记录
     */
    @Query("SELECT * FROM workout_history WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getWorkoutsByDateRange(startDate: String, endDate: String): Flow<List<WorkoutHistory>>
    
    /**
     * 获取指定周期的训练记录
     */
    @Query("SELECT * FROM workout_history WHERE week = :week ORDER BY day ASC")
    fun getWorkoutsByWeek(week: Int): Flow<List<WorkoutHistory>>
    
    /**
     * 获取训练统计信息
     */
    @Query("SELECT COUNT(*) FROM workout_history")
    fun getTotalWorkoutCount(): Flow<Int>
    
    /**
     * 获取平均AMRAP次数（仅统计完成的训练）
     */
    @Query("""
        SELECT AVG(
            CASE 
                WHEN json_extract(sets, '$[2].actualReps') IS NOT NULL 
                THEN CAST(json_extract(sets, '$[2].actualReps') AS INTEGER)
                ELSE 0 
            END
        ) 
        FROM workout_history 
        WHERE json_extract(sets, '$[2].isCompleted') = 1
    """)
    fun getAverageAmrapReps(): Flow<Double?>
    
    /**
     * 根据ID获取单次训练记录
     */
    @Query("SELECT * FROM workout_history WHERE id = :id")
    suspend fun getWorkoutById(id: Long): WorkoutHistory?
    
    /**
     * 插入训练记录
     */
    @Insert
    suspend fun insertWorkout(workout: WorkoutHistory): Long
    
    /**
     * 更新训练记录
     */
    @Update
    suspend fun updateWorkout(workout: WorkoutHistory)
    
    /**
     * 删除训练记录
     */
    @Delete
    suspend fun deleteWorkout(workout: WorkoutHistory)
    
    /**
     * 根据ID删除训练记录
     */
    @Query("DELETE FROM workout_history WHERE id = :id")
    suspend fun deleteWorkoutById(id: Long)
    
    /**
     * 删除所有训练记录
     */
    @Query("DELETE FROM workout_history")
    suspend fun deleteAllWorkouts()
    
    /**
     * 获取最后一次训练记录
     */
    @Query("SELECT * FROM workout_history ORDER BY date DESC, createdAt DESC LIMIT 1")
    suspend fun getLastWorkout(): WorkoutHistory?
    
    /**
     * 检查指定日期是否已有训练记录
     */
    @Query("SELECT COUNT(*) FROM workout_history WHERE date = :date AND lift = :lift")
    suspend fun hasWorkoutOnDate(date: String, lift: LiftType): Int
}

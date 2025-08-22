package com.jmin.five3one.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * 用户的最大单次重量记录
 */
@Entity(tableName = "one_rm")
@Serializable
data class OneRM(
    @PrimaryKey
    val id: Int = 1, // 只有一条记录
    val benchPress: Double = 80.0,
    val squat: Double = 100.0,
    val deadlift: Double = 120.0,
    val overheadPress: Double = 55.0,
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * 根据动作类型获取1RM值
     */
    fun getByLift(lift: LiftType): Double = when (lift) {
        LiftType.BENCH_PRESS -> benchPress
        LiftType.SQUAT -> squat
        LiftType.DEADLIFT -> deadlift
        LiftType.OVERHEAD_PRESS -> overheadPress
    }
    
    /**
     * 更新指定动作的1RM值
     */
    fun updateLift(lift: LiftType, weight: Double): OneRM = when (lift) {
        LiftType.BENCH_PRESS -> copy(benchPress = weight, updatedAt = System.currentTimeMillis())
        LiftType.SQUAT -> copy(squat = weight, updatedAt = System.currentTimeMillis())
        LiftType.DEADLIFT -> copy(deadlift = weight, updatedAt = System.currentTimeMillis())
        LiftType.OVERHEAD_PRESS -> copy(overheadPress = weight, updatedAt = System.currentTimeMillis())
    }
    
    /**
     * 计算训练最大重量 (90% 1RM)
     */
    fun calculateTrainingMax(): TrainingMax = TrainingMax(
        benchPress = benchPress * 0.9,
        squat = squat * 0.9,
        deadlift = deadlift * 0.9,
        overheadPress = overheadPress * 0.9
    )
}

package com.jmin.five3one.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * 训练最大重量 (Training Max - TM)
 * 通常为1RM的90%，是531训练法计算的基础
 */
@Entity(tableName = "training_max")
@Serializable
data class TrainingMax(
    @PrimaryKey
    val id: Int = 1, // 只有一条记录
    val benchPress: Double = 72.0,
    val squat: Double = 90.0,
    val deadlift: Double = 108.0,
    val overheadPress: Double = 49.5,
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * 根据动作类型获取TM值
     */
    fun getByLift(lift: LiftType): Double = when (lift) {
        LiftType.BENCH_PRESS -> benchPress
        LiftType.SQUAT -> squat
        LiftType.DEADLIFT -> deadlift
        LiftType.OVERHEAD_PRESS -> overheadPress
    }
    
    /**
     * 更新指定动作的TM值
     */
    fun updateLift(lift: LiftType, weight: Double): TrainingMax = when (lift) {
        LiftType.BENCH_PRESS -> copy(benchPress = weight, updatedAt = System.currentTimeMillis())
        LiftType.SQUAT -> copy(squat = weight, updatedAt = System.currentTimeMillis())
        LiftType.DEADLIFT -> copy(deadlift = weight, updatedAt = System.currentTimeMillis())
        LiftType.OVERHEAD_PRESS -> copy(overheadPress = weight, updatedAt = System.currentTimeMillis())
    }
    
    /**
     * 周期结束后增加TM
     * 上肢动作（卧推、站姿推举）+2.5kg
     * 下肢动作（深蹲、硬拉）+5kg
     */
    fun increaseCycle(): TrainingMax = copy(
        benchPress = benchPress + 2.5,
        squat = squat + 5.0,
        deadlift = deadlift + 5.0,
        overheadPress = overheadPress + 2.5,
        updatedAt = System.currentTimeMillis()
    )
    
    /**
     * 四舍五入到最近的0.5kg
     */
    fun roundToHalf(): TrainingMax = copy(
        benchPress = kotlin.math.round(benchPress * 2) / 2.0,
        squat = kotlin.math.round(squat * 2) / 2.0,
        deadlift = kotlin.math.round(deadlift * 2) / 2.0,
        overheadPress = kotlin.math.round(overheadPress * 2) / 2.0
    )
}

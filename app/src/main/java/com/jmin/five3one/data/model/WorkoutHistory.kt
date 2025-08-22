package com.jmin.five3one.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * 训练历史记录
 */
@Entity(tableName = "workout_history")
@Serializable
data class WorkoutHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // YYYY-MM-DD格式
    val lift: LiftType,
    val week: Int,
    val day: Int,
    val template: TemplateType,
    val trainingMax: Double,
    val sets: List<WorkoutSet>,
    val duration: Long = 0, // 训练持续时间（毫秒）
    val notes: String = "",
    val feeling: WorkoutFeeling = WorkoutFeeling.GOOD,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * 获取AMRAP组的实际次数
     */
    val amrapReps: Int
        get() = sets.lastOrNull()?.actualReps ?: 0
        
    /**
     * 是否完成训练
     */
    val isCompleted: Boolean
        get() = sets.all { it.isCompleted }
        
    /**
     * 训练表现评估
     */
    val performance: WorkoutPerformance
        get() {
            val amrapSet = sets.lastOrNull()
            if (amrapSet == null || !amrapSet.isCompleted) return WorkoutPerformance.INCOMPLETE
            
            val targetReps = amrapSet.targetReps
            val actualReps = amrapSet.actualReps
            
            return when {
                actualReps >= targetReps + 3 -> WorkoutPerformance.EXCELLENT
                actualReps >= targetReps + 1 -> WorkoutPerformance.GOOD
                actualReps >= targetReps -> WorkoutPerformance.FAIR
                else -> WorkoutPerformance.POOR
            }
        }
}

/**
 * 训练组记录
 */
@Serializable
data class WorkoutSet(
    val setNumber: Int,
    val weight: Double,
    val targetReps: Int,
    val actualReps: Int = 0,
    val isCompleted: Boolean = false,
    val isAmrap: Boolean = false
) {
    /**
     * 是否达到目标
     */
    val isTargetMet: Boolean
        get() = actualReps >= targetReps
        
    /**
     * 超额完成次数
     */
    val bonusReps: Int
        get() = kotlin.math.max(0, actualReps - targetReps)
}

/**
 * 训练感受
 */
@Serializable
enum class WorkoutFeeling(val emoji: String, val nameKey: String) {
    EXCELLENT("😊", "feeling_excellent"),
    GOOD("🙂", "feeling_good"),
    FAIR("😐", "feeling_fair"),
    POOR("😞", "feeling_poor")
}

/**
 * 训练表现评估
 */
@Serializable
enum class WorkoutPerformance(val nameKey: String, val color: String) {
    EXCELLENT("performance_excellent", "green"),
    GOOD("performance_good", "blue"),
    FAIR("performance_fair", "orange"),
    POOR("performance_poor", "red"),
    INCOMPLETE("performance_incomplete", "gray")
}

/**
 * 训练周期进度
 */
@Serializable
data class CycleProgress(
    val currentWeek: Int = 1,
    val currentDay: Int = 1,
    val cycleNumber: Int = 1,
    val completedWorkouts: Int = 0,
    val totalWorkouts: Int = 16 // 4周 × 4天
) {
    /**
     * 当前训练的动作
     */
    val currentLift: LiftType
        get() = LiftType.getByDay(currentDay)
        
    /**
     * 是否完成当前周期
     */
    val isCycleComplete: Boolean
        get() = completedWorkouts >= totalWorkouts
        
    /**
     * 周期完成进度（0.0 - 1.0）
     */
    val progressPercentage: Float
        get() = completedWorkouts.toFloat() / totalWorkouts.toFloat()
        
    /**
     * 完成一次训练后的进度
     */
    fun completeWorkout(): CycleProgress {
        val newCompletedWorkouts = completedWorkouts + 1
        var newWeek = currentWeek
        var newDay = currentDay + 1
        var newCycle = cycleNumber
        
        // 检查是否需要进入下一周
        if (newDay > 4) {
            newDay = 1
            newWeek++
            
            // 检查是否需要进入下一个周期
            if (newWeek > 4) {
                newWeek = 1
                newCycle++
            }
        }
        
        return copy(
            currentWeek = newWeek,
            currentDay = newDay,
            cycleNumber = newCycle,
            completedWorkouts = if (isCycleComplete) 0 else newCompletedWorkouts
        )
    }
}

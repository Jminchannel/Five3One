package com.jmin.five3one.data.model

import kotlinx.serialization.Serializable

/**
 * 四大项动作类型
 */
@Serializable
enum class LiftType(val displayNameKey: String, val iconName: String) {
    BENCH_PRESS("lift_bench", "bed"),
    SQUAT("lift_squat", "arrow-down"),
    DEADLIFT("lift_deadlift", "arrow-up"),
    OVERHEAD_PRESS("lift_press", "hands-pray");

    companion object {
        /**
         * 训练顺序：卧推 -> 深蹲 -> 站姿推举 -> 硬拉
         */
        val TRAINING_ORDER = listOf(BENCH_PRESS, SQUAT, OVERHEAD_PRESS, DEADLIFT)
        
        /**
         * 获取星期几对应的动作
         */
        fun getByDay(day: Int): LiftType = TRAINING_ORDER[(day - 1) % 4]
        
        /**
         * 获取动作在训练计划中的天数
         */
        fun getDayByLift(lift: LiftType): Int = TRAINING_ORDER.indexOf(lift) + 1
    }
}

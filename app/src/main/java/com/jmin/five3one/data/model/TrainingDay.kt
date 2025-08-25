package com.jmin.five3one.data.model

import kotlinx.serialization.Serializable

/**
 * 训练日计划，表示某一天的训练安排
 */
@Serializable
data class TrainingDay(
    val dayNumber: Int, // 第几天 (1-7)
    val liftTypes: List<LiftType> // 当天安排的动作列表
) {
    /**
     * 检查当天是否安排了多个主项
     */
    fun hasMultipleMainLifts(): Boolean {
        return liftTypes.size > 1
    }
    
    /**
     * 获取主项列表
     */
    fun getMainLifts(): List<LiftType> {
        return liftTypes
    }
}

/**
 * 自定义训练计划模板
 */
@Serializable
data class CustomWorkoutTemplate(
    val name: String,
    val trainingDays: List<TrainingDay>
) {
    /**
     * 验证训练计划是否合理
     * @return 验证结果，如果为空则表示验证通过，否则返回错误信息
     */
    fun validate(): String? {
        for (day in trainingDays) {
            if (day.hasMultipleMainLifts()) {
                return "提醒：出于安全和效率考虑，不建议在同一次训练中安排多个主项动作。这会导致极度疲劳并大幅增加受伤风险。建议为每个主项安排单独的训练日。"
            }
        }
        return null
    }
}
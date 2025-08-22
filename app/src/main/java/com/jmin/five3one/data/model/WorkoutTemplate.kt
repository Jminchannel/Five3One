package com.jmin.five3one.data.model

import kotlinx.serialization.Serializable

/**
 * 531训练模板类型
 */
@Serializable
enum class TemplateType(
    val id: String,
    val nameKey: String,
    val descriptionKey: String
) {
    FIVES("5s", "template_5s_name", "template_5s_description"),
    THREES("3s", "template_3s_name", "template_3s_description"),
    FIVE_THREE_ONE("531", "template_531_name", "template_531_description")
}

/**
 * 训练模板配置
 */
@Serializable
data class WorkoutTemplate(
    val type: TemplateType,
    val week1Percentages: List<Double>,
    val week2Percentages: List<Double>,
    val week3Percentages: List<Double>,
    val week4Percentages: List<Double>, // 减载周
    val reps: List<Int> // 每组的基础次数 [第1组, 第2组, 第3组+]
) {
    companion object {
        /**
         * 预定义的训练模板
         */
        val TEMPLATES = mapOf(
            TemplateType.FIVES to WorkoutTemplate(
                type = TemplateType.FIVES,
                week1Percentages = listOf(0.65, 0.75, 0.85),
                week2Percentages = listOf(0.70, 0.80, 0.90),
                week3Percentages = listOf(0.75, 0.85, 0.95),
                week4Percentages = listOf(0.40, 0.50, 0.60),
                reps = listOf(5, 5, 5)
            ),
            TemplateType.THREES to WorkoutTemplate(
                type = TemplateType.THREES,
                week1Percentages = listOf(0.70, 0.80, 0.90),
                week2Percentages = listOf(0.75, 0.85, 0.95),
                week3Percentages = listOf(0.80, 0.90, 1.00),
                week4Percentages = listOf(0.40, 0.50, 0.60),
                reps = listOf(3, 3, 3)
            ),
            TemplateType.FIVE_THREE_ONE to WorkoutTemplate(
                type = TemplateType.FIVE_THREE_ONE,
                week1Percentages = listOf(0.75, 0.85, 0.95),
                week2Percentages = listOf(0.80, 0.90, 1.00),
                week3Percentages = listOf(0.85, 0.95, 1.05),
                week4Percentages = listOf(0.40, 0.50, 0.60),
                reps = listOf(5, 3, 1)
            )
        )
        
        /**
         * 根据类型获取模板
         */
        fun getTemplate(type: TemplateType): WorkoutTemplate {
            return TEMPLATES[type] ?: TEMPLATES[TemplateType.FIVES]!!
        }
    }
    
    /**
     * 获取指定周的百分比
     */
    fun getWeekPercentages(week: Int): List<Double> = when (week) {
        1 -> week1Percentages
        2 -> week2Percentages
        3 -> week3Percentages
        4 -> week4Percentages
        else -> week1Percentages
    }
    
    /**
     * 计算指定周和组的重量
     */
    fun calculateWeight(trainingMax: Double, week: Int, set: Int): Double {
        val percentages = getWeekPercentages(week)
        if (set < 1 || set > percentages.size) return 0.0
        
        val weight = trainingMax * percentages[set - 1]
        // 四舍五入到最近的0.5kg
        return kotlin.math.round(weight * 2) / 2.0
    }
    
    /**
     * 获取指定组的目标次数
     */
    fun getTargetReps(set: Int): Int {
        if (set < 1 || set > reps.size) return 0
        return reps[set - 1]
    }
    
    /**
     * 是否为AMRAP组（最后一组）
     */
    fun isAmrapSet(set: Int): Boolean = set == reps.size
}

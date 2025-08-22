package com.jmin.five3one.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * 杠铃配重配置
 */
@Entity(tableName = "plate_config")
@Serializable
data class PlateConfig(
    @PrimaryKey
    val id: Int = 1, // 只有一条记录
    val barbellWeight: Double = 20.0, // 杠铃杆重量
    val availablePlates: List<Double> = listOf(25.0, 20.0, 10.0, 5.0, 2.5, 1.25), // 可用杠铃片
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        /**
         * 默认杠铃片配置（常见健身房配置）
         */
        val DEFAULT_PLATES = listOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 1.25)
        
        /**
         * 杠铃片颜色映射（用于UI显示）
         */
        val PLATE_COLORS = mapOf(
            25.0 to "red",
            20.0 to "blue", 
            15.0 to "yellow",
            10.0 to "green",
            5.0 to "purple",
            2.5 to "gray",
            1.25 to "lightgray"
        )
        
        /**
         * 杠铃片相对大小映射（用于UI显示）
         */
        val PLATE_SIZES = mapOf(
            25.0 to 1.0f,
            20.0 to 0.9f,
            15.0 to 0.8f,
            10.0 to 0.7f,
            5.0 to 0.6f,
            2.5 to 0.5f,
            1.25 to 0.4f
        )
    }
    
    /**
     * 添加杠铃片规格
     */
    fun addPlate(weight: Double): PlateConfig {
        if (availablePlates.contains(weight)) return this
        val newPlates = (availablePlates + weight).sortedDescending()
        return copy(availablePlates = newPlates, updatedAt = System.currentTimeMillis())
    }
    
    /**
     * 移除杠铃片规格
     */
    fun removePlate(weight: Double): PlateConfig {
        val newPlates = availablePlates.filter { it != weight }
        return copy(availablePlates = newPlates, updatedAt = System.currentTimeMillis())
    }
    
    /**
     * 切换杠铃片可用性
     */
    fun togglePlate(weight: Double): PlateConfig {
        return if (availablePlates.contains(weight)) {
            removePlate(weight)
        } else {
            addPlate(weight)
        }
    }
    
    /**
     * 更新杠铃杆重量
     */
    fun updateBarbellWeight(weight: Double): PlateConfig {
        return copy(barbellWeight = weight, updatedAt = System.currentTimeMillis())
    }
}

/**
 * 杠铃配重方案
 */
@Serializable
data class PlateSolution(
    val targetWeight: Double,
    val actualWeight: Double,
    val barbellWeight: Double,
    val platesPerSide: List<Double>, // 每侧所需的杠铃片
    val error: Double = actualWeight - targetWeight
) {
    /**
     * 是否为精确匹配
     */
    val isExactMatch: Boolean
        get() = kotlin.math.abs(error) < 0.1
        
    /**
     * 每侧总重量
     */
    val weightPerSide: Double
        get() = platesPerSide.sum()
        
    /**
     * 杠铃片使用统计
     */
    val plateCount: Map<Double, Int>
        get() = platesPerSide.groupingBy { it }.eachCount()
}

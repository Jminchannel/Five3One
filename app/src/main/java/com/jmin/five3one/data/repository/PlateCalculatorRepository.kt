package com.jmin.five3one.data.repository

import com.jmin.five3one.data.model.PlateConfig
import com.jmin.five3one.data.model.PlateSolution
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.floor

/**
 * 杠铃配重计算仓库
 * 提供智能杠铃配重算法
 */
@Singleton
class PlateCalculatorRepository @Inject constructor() {
    
    /**
     * 计算最佳配重方案
     */
    fun calculateBestSolution(
        targetWeight: Double,
        plateConfig: PlateConfig
    ): PlateSolution? {
        val barbellWeight = plateConfig.barbellWeight
        val availablePlates = plateConfig.availablePlates.sortedDescending()
        
        // 计算每侧需要的重量
        val weightPerSide = (targetWeight - barbellWeight) / 2.0
        
        if (weightPerSide <= 0) {
            return null // 目标重量太小
        }
        
        // 使用贪心算法找最佳组合
        val bestSolution = findBestPlateCombo(weightPerSide, availablePlates)
        
        val actualWeight = barbellWeight + bestSolution.sum() * 2
        
        return PlateSolution(
            targetWeight = targetWeight,
            actualWeight = actualWeight,
            barbellWeight = barbellWeight,
            platesPerSide = bestSolution
        )
    }
    
    /**
     * 获取多个配重方案（提供备选）
     */
    fun calculateMultipleSolutions(
        targetWeight: Double,
        plateConfig: PlateConfig,
        maxSolutions: Int = 3
    ): List<PlateSolution> {
        val solutions = mutableListOf<PlateSolution>()
        val barbellWeight = plateConfig.barbellWeight
        val availablePlates = plateConfig.availablePlates.sortedDescending()
        val weightPerSide = (targetWeight - barbellWeight) / 2.0
        
        if (weightPerSide <= 0) {
            return emptyList()
        }
        
        // 尝试不同的组合策略
        val allCombinations = generatePlateCombinations(weightPerSide, availablePlates)
        
        // 按误差排序，选择最好的几个方案
        allCombinations
            .map { plates ->
                val actualWeight = barbellWeight + plates.sum() * 2
                PlateSolution(
                    targetWeight = targetWeight,
                    actualWeight = actualWeight,
                    barbellWeight = barbellWeight,
                    platesPerSide = plates
                )
            }
            .sortedBy { abs(it.error) }
            .take(maxSolutions)
            .forEach { solutions.add(it) }
        
        return solutions
    }
    
    /**
     * 贪心算法找最佳杠铃片组合
     */
    private fun findBestPlateCombo(
        targetWeight: Double,
        availablePlates: List<Double>
    ): List<Double> {
        val result = mutableListOf<Double>()
        var remainingWeight = targetWeight
        
        for (plate in availablePlates) {
            // 计算可以使用多少个这种片（最多2个，因为是每侧）
            val maxCount = minOf(2, floor(remainingWeight / plate).toInt())
            
            if (maxCount > 0) {
                repeat(maxCount) {
                    result.add(plate)
                    remainingWeight -= plate
                }
            }
            
            if (remainingWeight <= 0.1) break // 足够接近目标重量
        }
        
        return result
    }
    
    /**
     * 生成所有可能的杠铃片组合（限制搜索空间）
     */
    private fun generatePlateCombinations(
        targetWeight: Double,
        availablePlates: List<Double>,
        maxPlatesPerSide: Int = 6 // 限制每侧最多6片
    ): List<List<Double>> {
        val combinations = mutableListOf<List<Double>>()
        
        fun backtrack(
            index: Int,
            currentCombo: MutableList<Double>,
            currentWeight: Double
        ) {
            // 如果已经很接近目标重量，添加到结果中
            if (abs(currentWeight - targetWeight) <= 2.5) {
                combinations.add(currentCombo.toList())
            }
            
            // 如果超重太多或者杠铃片太多，剪枝
            if (currentWeight > targetWeight + 5.0 || 
                currentCombo.size >= maxPlatesPerSide ||
                index >= availablePlates.size) {
                return
            }
            
            val plate = availablePlates[index]
            
            // 尝试不使用当前重量的片
            backtrack(index + 1, currentCombo, currentWeight)
            
            // 尝试使用1个当前重量的片
            if (currentCombo.size < maxPlatesPerSide) {
                currentCombo.add(plate)
                backtrack(index + 1, currentCombo, currentWeight + plate)
                
                // 尝试使用2个当前重量的片
                if (currentCombo.size < maxPlatesPerSide) {
                    currentCombo.add(plate)
                    backtrack(index + 1, currentCombo, currentWeight + plate)
                    currentCombo.removeAt(currentCombo.lastIndex)
                }
                currentCombo.removeAt(currentCombo.lastIndex)
            }
        }
        
        backtrack(0, mutableListOf(), 0.0)
        
        // 返回前20个最好的组合（按误差排序）
        return combinations
            .distinctBy { it.sorted() } // 去重
            .sortedBy { abs(it.sum() - targetWeight) }
            .take(20)
    }
    
    /**
     * 验证配重方案是否有效
     */
    fun validateSolution(
        solution: PlateSolution,
        plateConfig: PlateConfig
    ): Boolean {
        // 检查每种杠铃片的使用数量是否超限
        val plateCount = solution.plateCount
        
        for ((plate, count) in plateCount) {
            if (!plateConfig.availablePlates.contains(plate)) {
                return false // 使用了不可用的杠铃片
            }
            if (count > 2) {
                return false // 每侧每种片最多使用2个
            }
        }
        
        return true
    }
    
    /**
     * 计算重量范围内的所有可能重量
     */
    fun getAvailableWeights(
        plateConfig: PlateConfig,
        minWeight: Double = 0.0,
        maxWeight: Double = 300.0
    ): List<Double> {
        val weights = mutableSetOf<Double>()
        val barbellWeight = plateConfig.barbellWeight
        val availablePlates = plateConfig.availablePlates
        
        // 递归生成所有可能的组合
        fun generateWeights(
            plateIndex: Int,
            currentWeight: Double,
            plateCount: Map<Double, Int>
        ) {
            if (currentWeight >= minWeight && currentWeight <= maxWeight) {
                weights.add(currentWeight)
            }
            
            if (plateIndex >= availablePlates.size || currentWeight > maxWeight) {
                return
            }
            
            val plate = availablePlates[plateIndex]
            val currentCount = plateCount[plate] ?: 0
            
            // 不使用当前杠铃片
            generateWeights(plateIndex + 1, currentWeight, plateCount)
            
            // 使用1个或2个当前杠铃片（每侧，总共2个或4个）
            for (count in 1..2) {
                if (currentCount + count <= 2) { // 每侧最多2个
                    val newWeight = currentWeight + plate * count * 2 // 两侧
                    val newPlateCount = plateCount.toMutableMap()
                    newPlateCount[plate] = currentCount + count
                    generateWeights(plateIndex + 1, newWeight, newPlateCount)
                }
            }
        }
        
        generateWeights(0, barbellWeight, emptyMap())
        
        return weights.sorted()
    }
}

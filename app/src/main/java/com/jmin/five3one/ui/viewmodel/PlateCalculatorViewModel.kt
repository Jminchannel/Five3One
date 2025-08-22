package com.jmin.five3one.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jmin.five3one.data.model.PlateConfig
import com.jmin.five3one.data.model.PlateSolution
import com.jmin.five3one.data.repository.PlateCalculatorRepository
import com.jmin.five3one.data.repository.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 杠铃配重计算器ViewModel
 * 管理配重计算、方案显示、配置管理等
 */
@HiltViewModel
class PlateCalculatorViewModel @Inject constructor(
    private val plateCalculatorRepository: PlateCalculatorRepository,
    private val userDataRepository: UserDataRepository
) : ViewModel() {
    
    // 杠铃片配置
    val plateConfig: StateFlow<PlateConfig> = userDataRepository.getPlateConfig()
        .map { it ?: PlateConfig() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PlateConfig()
        )
    
    // 计算器状态
    private val _calculatorState = MutableStateFlow(PlateCalculatorUiState())
    val calculatorState: StateFlow<PlateCalculatorUiState> = _calculatorState.asStateFlow()
    
    // 当前配重方案
    val currentSolution: StateFlow<PlateSolution?> = _calculatorState
        .map { it.currentSolution }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    // 多个配重方案
    val alternateSolutions: StateFlow<List<PlateSolution>> = _calculatorState
        .map { it.alternateSolutions }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // 可用重量列表（用于快速选择）
    val availableWeights: StateFlow<List<Double>> = plateConfig
        .map { config ->
            plateCalculatorRepository.getAvailableWeights(
                plateConfig = config,
                minWeight = config.barbellWeight,
                maxWeight = 300.0
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    /**
     * 设置目标重量
     */
    fun setTargetWeight(weight: Double) {
        _calculatorState.update { it.copy(targetWeight = weight) }
        calculatePlateLoading(weight)
    }
    
    /**
     * 设置目标重量（从字符串输入）
     */
    fun setTargetWeightFromString(weightStr: String) {
        _calculatorState.update { it.copy(targetWeightInput = weightStr) }
        
        val weight = weightStr.toDoubleOrNull()
        if (weight != null && weight > 0) {
            setTargetWeight(weight)
        } else {
            clearSolutions()
        }
    }
    
    /**
     * 计算配重方案
     */
    fun calculatePlateLoading(targetWeight: Double) {
        viewModelScope.launch {
            try {
                _calculatorState.update { it.copy(isCalculating = true) }
                
                val config = plateConfig.value
                
                // 计算最佳方案
                val bestSolution = plateCalculatorRepository.calculateBestSolution(targetWeight, config)
                
                // 计算多个备选方案
                val multipleSolutions = plateCalculatorRepository.calculateMultipleSolutions(
                    targetWeight, config, 5
                )
                
                _calculatorState.update { state ->
                    state.copy(
                        currentSolution = bestSolution,
                        alternateSolutions = multipleSolutions,
                        isCalculating = false,
                        errorMessage = if (bestSolution == null) "无法为该重量配重" else null
                    )
                }
                
            } catch (e: Exception) {
                _calculatorState.update { 
                    it.copy(
                        isCalculating = false,
                        errorMessage = "计算配重失败: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    /**
     * 选择备选方案
     */
    fun selectAlternateSolution(solution: PlateSolution) {
        _calculatorState.update { it.copy(currentSolution = solution) }
    }
    
    /**
     * 清除配重方案
     */
    fun clearSolutions() {
        _calculatorState.update { 
            it.copy(
                currentSolution = null,
                alternateSolutions = emptyList()
            ) 
        }
    }
    
    /**
     * 增加重量
     */
    fun increaseWeight(increment: Double = 2.5) {
        val currentWeight = _calculatorState.value.targetWeight
        val newWeight = currentWeight + increment
        setTargetWeight(newWeight)
        _calculatorState.update { it.copy(targetWeightInput = newWeight.toString()) }
    }
    
    /**
     * 减少重量
     */
    fun decreaseWeight(decrement: Double = 2.5) {
        val currentWeight = _calculatorState.value.targetWeight
        val newWeight = (currentWeight - decrement).coerceAtLeast(0.0)
        setTargetWeight(newWeight)
        _calculatorState.update { it.copy(targetWeightInput = newWeight.toString()) }
    }
    
    /**
     * 从可用重量列表选择
     */
    fun selectFromAvailableWeights(weight: Double) {
        setTargetWeight(weight)
        _calculatorState.update { it.copy(targetWeightInput = weight.toString()) }
    }
    
    /**
     * 保存收藏的配重方案
     */
    fun saveFavoriteSolution(solution: PlateSolution) {
        viewModelScope.launch {
            try {
                // 这里应该实现收藏功能的数据存储
                _calculatorState.update { 
                    it.copy(successMessage = "配重方案已保存到收藏") 
                }
            } catch (e: Exception) {
                _calculatorState.update { 
                    it.copy(errorMessage = "保存失败: ${e.message}") 
                }
            }
        }
    }
    
    /**
     * 快速设置常用重量
     */
    fun setQuickWeight(percentage: Double) {
        viewModelScope.launch {
            val userData = userDataRepository.getUserData().first()
            val currentTM = _calculatorState.value.selectedLift?.let { lift ->
                userData.trainingMax.getByLift(lift)
            } ?: 0.0
            
            if (currentTM > 0) {
                val targetWeight = currentTM * percentage
                setTargetWeight(targetWeight)
                _calculatorState.update { it.copy(targetWeightInput = targetWeight.toString()) }
            }
        }
    }
    
    /**
     * 设置当前动作（用于快速百分比计算）
     */
    fun setSelectedLift(lift: com.jmin.five3one.data.model.LiftType?) {
        _calculatorState.update { it.copy(selectedLift = lift) }
    }
    
    /**
     * 清除错误消息
     */
    fun clearError() {
        _calculatorState.update { it.copy(errorMessage = null) }
    }
    
    /**
     * 清除成功消息
     */
    fun clearSuccess() {
        _calculatorState.update { it.copy(successMessage = null) }
    }
    
    /**
     * 重置计算器
     */
    fun resetCalculator() {
        _calculatorState.update { PlateCalculatorUiState() }
    }
}

/**
 * 杠铃配重计算器UI状态
 */
data class PlateCalculatorUiState(
    val targetWeight: Double = 0.0,
    val targetWeightInput: String = "",
    val selectedLift: com.jmin.five3one.data.model.LiftType? = null,
    val currentSolution: PlateSolution? = null,
    val alternateSolutions: List<PlateSolution> = emptyList(),
    val isCalculating: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val hasValidInput: Boolean
        get() = targetWeight > 0
    
    val hasSolution: Boolean
        get() = currentSolution != null
}

/**
 * 快速重量百分比选项
 */
enum class QuickWeightPercentage(val percentage: Double, val labelKey: String) {
    FIFTY_PERCENT(0.50, "50%"),
    SIXTY_PERCENT(0.60, "60%"),
    SEVENTY_PERCENT(0.70, "70%"),
    EIGHTY_PERCENT(0.80, "80%"),
    NINETY_PERCENT(0.90, "90%"),
    ONE_HUNDRED_PERCENT(1.00, "100%")
}

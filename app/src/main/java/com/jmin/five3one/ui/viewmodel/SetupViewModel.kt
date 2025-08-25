package com.jmin.five3one.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jmin.five3one.data.model.*
import com.jmin.five3one.data.repository.UserDataRepository
import com.jmin.five3one.data.repository.UserData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 设置流程ViewModel
 * 管理初始设置流程：1RM设置、杠铃片配置、模板选择
 */
@HiltViewModel
class SetupViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository
) : ViewModel() {
    
    // 当前用户数据
    val userData: StateFlow<UserData> = userDataRepository.getUserData()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserData(
                oneRM = OneRM(),
                trainingMax = TrainingMax(),
                plateConfig = PlateConfig(),
                appSettings = AppSettings()
            )
        )
    
    // 设置UI状态
    private val _setupState = MutableStateFlow(SetupUiState())
    val setupState: StateFlow<SetupUiState> = _setupState.asStateFlow()
    
    // 1RM输入状态
    private val _oneRMInputs = MutableStateFlow(OneRMInputs())
    val oneRMInputs: StateFlow<OneRMInputs> = _oneRMInputs.asStateFlow()
    
    // 杠铃片配置状态
    private val _plateConfigState = MutableStateFlow(PlateConfigState())
    val plateConfigState: StateFlow<PlateConfigState> = _plateConfigState.asStateFlow()
    
    init {
        // 初始化当前数据
        viewModelScope.launch {
            userData.collect { data ->
                _oneRMInputs.update {
                    OneRMInputs(
                        benchPress = data.oneRM.benchPress.toString(),
                        squat = data.oneRM.squat.toString(),
                        deadlift = data.oneRM.deadlift.toString(),
                        overheadPress = data.oneRM.overheadPress.toString()
                    )
                }
                
                _plateConfigState.update {
                    PlateConfigState(
                        barbellWeight = data.plateConfig.barbellWeight,
                        availablePlates = data.plateConfig.availablePlates,
                        selectedPlates = data.plateConfig.availablePlates.toSet()
                    )
                }
                
                _setupState.update { state ->
                    state.copy(selectedTemplate = data.appSettings.currentTemplate)
                }
            }
        }
    }
    
    // 1RM输入更新方法
    fun updateBenchPress(value: String) {
        _oneRMInputs.update { it.copy(benchPress = value) }
    }
    
    fun updateSquat(value: String) {
        _oneRMInputs.update { it.copy(squat = value) }
    }
    
    fun updateDeadlift(value: String) {
        _oneRMInputs.update { it.copy(deadlift = value) }
    }
    
    fun updateOverheadPress(value: String) {
        _oneRMInputs.update { it.copy(overheadPress = value) }
    }
    
    /**
     * 保存1RM数据
     */
    fun save1RM() {
        viewModelScope.launch {
            try {
                val inputs = _oneRMInputs.value
                val oneRM = OneRM(
                    benchPress = inputs.benchPress.toDoubleOrNull() ?: 80.0,
                    squat = inputs.squat.toDoubleOrNull() ?: 100.0,
                    deadlift = inputs.deadlift.toDoubleOrNull() ?: 120.0,
                    overheadPress = inputs.overheadPress.toDoubleOrNull() ?: 55.0
                )
                
                userDataRepository.saveOneRM(oneRM)
                _setupState.update { it.copy(currentStep = 2) }
                
            } catch (e: Exception) {
                _setupState.update { it.copy(errorMessage = "保存1RM失败: ${e.message}") }
            }
        }
    }
    
    /**
     * 验证1RM输入
     */
    fun validate1RM(): Boolean {
        val inputs = _oneRMInputs.value
        return listOf(
            inputs.benchPress.toDoubleOrNull(),
            inputs.squat.toDoubleOrNull(),
            inputs.deadlift.toDoubleOrNull(),
            inputs.overheadPress.toDoubleOrNull()
        ).all { it != null && it > 0 }
    }
    
    // 杠铃片配置方法
    fun updateBarbellWeight(weight: Double) {
        _plateConfigState.update { it.copy(barbellWeight = weight) }
    }
    
    fun togglePlate(weight: Double) {
        _plateConfigState.update { state ->
            val newSelected = if (state.selectedPlates.contains(weight)) {
                state.selectedPlates - weight
            } else {
                state.selectedPlates + weight
            }
            state.copy(selectedPlates = newSelected)
        }
    }
    
    fun addCustomPlate(weight: Double) {
        _plateConfigState.update { state ->
            val newAvailable = (state.availablePlates + weight).sortedDescending()
            val newSelected = state.selectedPlates + weight
            state.copy(
                availablePlates = newAvailable,
                selectedPlates = newSelected
            )
        }
    }
    
    /**
     * 保存杠铃片配置
     */
    fun savePlateConfig() {
        viewModelScope.launch {
            try {
                val state = _plateConfigState.value
                val plateConfig = PlateConfig(
                    barbellWeight = state.barbellWeight,
                    availablePlates = state.selectedPlates.sortedDescending()
                )
                
                userDataRepository.savePlateConfig(plateConfig)
                _setupState.update { it.copy(currentStep = 3) }
                
            } catch (e: Exception) {
                _setupState.update { it.copy(errorMessage = "保存杠铃片配置失败: ${e.message}") }
            }
        }
    }
    
    // 模板选择方法
    fun selectTemplate(template: TemplateType) {
        _setupState.update { it.copy(selectedTemplate = template) }
    }
    
    /**
     * 保存模板选择并完成设置
     */
    fun completeSetup() {
        viewModelScope.launch {
            try {
                val selectedTemplate = _setupState.value.selectedTemplate
                if (selectedTemplate != null) {
                    val currentSettings = userData.value.appSettings
                    val updatedSettings = currentSettings
                        .updateTemplate(selectedTemplate)
                        .completeSetup()
                    
                    userDataRepository.saveAppSettings(updatedSettings)
                    _setupState.update { it.copy(isCompleted = true) }
                }
            } catch (e: Exception) {
                _setupState.update { it.copy(errorMessage = "完成设置失败: ${e.message}") }
            }
        }
    }
    
    /**
     * 导航到上一步
     */
    fun previousStep() {
        _setupState.update { state ->
            if (state.currentStep > 1) {
                state.copy(currentStep = state.currentStep - 1)
            } else state
        }
    }
    
    /**
     * 导航到下一步
     */
    fun nextStep() {
        _setupState.update { state ->
            if (state.currentStep < 3) {
                state.copy(currentStep = state.currentStep + 1)
            } else state
        }
    }
    
    /**
     * 设置当前步骤
     */
    fun setCurrentStep(step: Int) {
        val validStep = step.coerceIn(1, 3)
        _setupState.update { it.copy(currentStep = validStep) }
    }
    
    /**
     * 清除错误消息
     */
    fun clearError() {
        _setupState.update { it.copy(errorMessage = null) }
    }
    
    /**
     * 重置设置流程
     */
    fun resetSetup() {
        _setupState.update { SetupUiState() }
        _oneRMInputs.update { OneRMInputs() }
        _plateConfigState.update { PlateConfigState() }
    }
}

/**
 * 设置流程UI状态
 */
data class SetupUiState(
    val currentStep: Int = 1, // 1: 1RM设置, 2: 杠铃片配置, 3: 模板选择
    val selectedTemplate: TemplateType? = null,
    val isCompleted: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val totalSteps: Int = 3
    val progressPercentage: Float = currentStep.toFloat() / totalSteps.toFloat()
}

/**
 * 1RM输入状态
 */
data class OneRMInputs(
    val benchPress: String = "80",
    val squat: String = "100",
    val deadlift: String = "120",
    val overheadPress: String = "55"
) {
    fun isValid(): Boolean {
        return listOf(benchPress, squat, deadlift, overheadPress)
            .all { it.toDoubleOrNull()?.let { weight -> weight > 0 } ?: false }
    }
    
    fun toOneRM(): OneRM? {
        return if (isValid()) {
            OneRM(
                benchPress = benchPress.toDouble(),
                squat = squat.toDouble(),
                deadlift = deadlift.toDouble(),
                overheadPress = overheadPress.toDouble()
            )
        } else null
    }
}

/**
 * 杠铃片配置状态
 */
data class PlateConfigState(
    val barbellWeight: Double = 20.0,
    val availablePlates: List<Double> = PlateConfig.DEFAULT_PLATES,
    val selectedPlates: Set<Double> = setOf(25.0, 20.0, 10.0, 5.0, 2.5, 1.25)
) {
    fun toPlateConfig(): PlateConfig {
        return PlateConfig(
            barbellWeight = barbellWeight,
            availablePlates = selectedPlates.sortedDescending()
        )
    }
}

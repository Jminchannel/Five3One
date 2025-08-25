package com.jmin.five3one.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jmin.five3one.data.model.*
import com.jmin.five3one.data.repository.UserDataRepository
import com.jmin.five3one.data.repository.UserData
import com.jmin.five3one.data.repository.WorkoutRepository
import com.jmin.five3one.data.repository.WorkoutStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 主界面ViewModel
 * 管理应用整体状态、用户数据和训练进度
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {
    
    // 用户数据状态
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
    
    // 训练统计状态
    val workoutStats: StateFlow<WorkoutStats> = workoutRepository.getWorkoutStats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WorkoutStats()
        )
    
    // 当前周期进度状态
    val cycleProgress: StateFlow<CycleProgress> = workoutRepository.getCurrentCycleProgress()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CycleProgress()
        )
    
    // 最近训练记录
    val recentWorkouts: StateFlow<List<WorkoutHistory>> = workoutRepository.getRecentWorkouts(5)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // UI状态
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    // 组合状态：今日训练信息
    val todayWorkout: StateFlow<TodayWorkoutInfo> = combine(
        userData,
        cycleProgress
    ) { userData, progress ->
        val currentLift = progress.currentLift
        val template = WorkoutTemplate.getTemplate(userData.appSettings.currentTemplate)
        val trainingMax = userData.trainingMax.getByLift(currentLift)
        
        TodayWorkoutInfo(
            lift = currentLift,
            week = progress.currentWeek,
            day = progress.currentDay,
            template = template,
            trainingMax = trainingMax
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TodayWorkoutInfo()
    )
    
    init {
        // 检查设置完成状态
        viewModelScope.launch {
            val isSetupCompleted = userDataRepository.isSetupCompleted()
            _uiState.update { it.copy(isSetupCompleted = isSetupCompleted) }
        }
    }
    
    /**
     * 检查是否已完成设置
     */
    fun checkSetupStatus() {
        viewModelScope.launch {
            val isCompleted = userDataRepository.isSetupCompleted()
            _uiState.update { it.copy(isSetupCompleted = isCompleted) }
        }
    }
    
    /**
     * 完成初始设置
     */
    fun completeSetup() {
        viewModelScope.launch {
            userDataRepository.completeSetup()
            _uiState.update { it.copy(isSetupCompleted = true) }
        }
    }
    
    /**
     * 更新1RM
     */
    fun updateOneRM(lift: LiftType, weight: Double) {
        viewModelScope.launch {
            userDataRepository.updateOneRM(lift, weight)
        }
    }
    
    /**
     * 更新训练最大重量
     */
    fun updateTrainingMax(lift: LiftType, weight: Double) {
        viewModelScope.launch {
            userDataRepository.updateTrainingMax(lift, weight)
        }
    }
    
    /**
     * 更新训练模板
     */
    fun updateTemplate(template: TemplateType) {
        viewModelScope.launch {
            val currentSettings = userData.value.appSettings
            val updatedSettings = currentSettings.updateTemplate(template)
            userDataRepository.saveAppSettings(updatedSettings)
        }
    }
    
    /**
     * 更新语言设置
     */
    fun updateLanguage(language: Language) {
        viewModelScope.launch {
            val currentSettings = userData.value.appSettings
            val updatedSettings = currentSettings.updateLanguage(language)
            userDataRepository.saveAppSettings(updatedSettings)
        }
    }
    
    /**
     * 切换深色模式
     */
    fun toggleDarkMode() {
        viewModelScope.launch {
            val currentSettings = userData.value.appSettings
            val updatedSettings = currentSettings.toggleDarkMode()
            userDataRepository.saveAppSettings(updatedSettings)
        }
    }
    
    /**
     * 开始训练
     */
    fun startWorkout() {
        _uiState.update { it.copy(isWorkoutInProgress = true) }
    }
    
    /**
     * 完成训练
     */
    fun completeWorkout(sets: List<WorkoutSet>, duration: Long, notes: String = "", feeling: WorkoutFeeling = WorkoutFeeling.GOOD) {
        viewModelScope.launch {
            val currentWorkout = todayWorkout.value
            
            // 保存训练记录
            workoutRepository.createWorkout(
                lift = currentWorkout.lift,
                week = currentWorkout.week,
                day = currentWorkout.day,
                template = currentWorkout.template.type,
                trainingMax = currentWorkout.trainingMax,
                sets = sets,
                duration = duration,
                notes = notes,
                feeling = feeling
            )
            
            // 检查是否需要增加周期TM
            val newProgress = cycleProgress.value.completeWorkout()
            if (newProgress.isCycleComplete) {
                userDataRepository.increaseCycleTM()
            }
            
            _uiState.update { it.copy(isWorkoutInProgress = false) }
        }
    }
    
    /**
     * 重置所有数据
     */
    fun resetAllData() {
        viewModelScope.launch {
            userDataRepository.resetAllData()
            workoutRepository.deleteAllWorkouts()
            _uiState.update { it.copy(isSetupCompleted = false) }
        }
    }
    
    /**
     * 导出数据
     */
    fun exportData() {
        viewModelScope.launch {
            try {
                val exportData = userDataRepository.exportUserData()
                // 这里应该触发文件导出，简化处理
                _uiState.update { it.copy(showExportSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Export failed: ${e.message}") }
            }
        }
    }
    
    /**
     * 清除错误消息
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    /**
     * 清除成功消息
     */
    fun clearSuccessMessage() {
        _uiState.update { it.copy(showExportSuccess = false) }
    }
}

/**
 * 主界面UI状态
 */
data class MainUiState(
    val isSetupCompleted: Boolean = false,
    val isWorkoutInProgress: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showExportSuccess: Boolean = false
)

/**
 * 今日训练信息
 */
data class TodayWorkoutInfo(
    val lift: LiftType = LiftType.BENCH_PRESS,
    val week: Int = 1,
    val day: Int = 1,
    val template: WorkoutTemplate = WorkoutTemplate.getTemplate(TemplateType.FIVES),
    val trainingMax: Double = 0.0
) {
    /**
     * 获取训练组信息
     */
    fun getWorkoutSets(): List<WorkoutSetInfo> {
        val percentages = template.getWeekPercentages(week)
        return percentages.mapIndexed { index, percentage ->
            val weight = template.calculateWeight(trainingMax, week, index + 1)
            val targetReps = template.getTargetReps(index + 1)
            val isAmrap = template.isAmrapSet(index + 1)
            
            WorkoutSetInfo(
                setNumber = index + 1,
                weight = weight,
                percentage = percentage,
                targetReps = targetReps,
                isAmrap = isAmrap
            )
        }
    }
}

/**
 * 训练组信息
 */
data class WorkoutSetInfo(
    val setNumber: Int,
    val weight: Double,
    val percentage: Double,
    val targetReps: Int,
    val isAmrap: Boolean
)

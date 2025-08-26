package com.jmin.five3one.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jmin.five3one.data.model.*
import com.jmin.five3one.data.repository.UserDataRepository
import com.jmin.five3one.data.repository.UserData
import com.jmin.five3one.data.repository.WorkoutRepository
import com.jmin.five3one.data.repository.WorkoutStats
import com.jmin.five3one.data.repository.TrainingScheduleRepository
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
    private val workoutRepository: WorkoutRepository,
    private val trainingScheduleRepository: TrainingScheduleRepository
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
    
    // 当前训练计划
    val activeTrainingSchedule: StateFlow<UserTrainingSchedule?> = trainingScheduleRepository.getActiveSchedule()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    // UI状态
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    // 组合状态：今日训练信息
    val todayWorkout: StateFlow<TodayWorkoutInfo> = combine(
        userData,
        cycleProgress,
        activeTrainingSchedule
    ) { userData, progress, schedule ->
        // 如果有活跃的训练计划，使用训练计划决定今日训练
        if (schedule != null) {
            println("DEBUG: MainViewModel - Found active schedule: $schedule")
            val todayTrainingDay = schedule.getTodayTrainingDay()
            val currentLift = todayTrainingDay?.mainLift ?: progress.currentLift
            val template = WorkoutTemplate.getTemplate(userData.appSettings.currentTemplate)
            val trainingMax = userData.trainingMax.getByLift(currentLift)
            
            // 检查训练限制
            val canStartTraining = schedule.canStartTraining()
            val restrictionMessage = schedule.getTrainingRestrictionMessage()
            
            println("DEBUG: MainViewModel - TodayTrainingDay: $todayTrainingDay")
            println("DEBUG: MainViewModel - CanStartTraining: $canStartTraining")
            println("DEBUG: MainViewModel - RestrictionMessage: $restrictionMessage")
            
            TodayWorkoutInfo(
                lift = currentLift,
                week = progress.currentWeek,
                day = progress.currentDay,
                template = template,
                trainingMax = trainingMax,
                canStartTraining = canStartTraining,
                restrictionMessage = restrictionMessage
            )
        } else {
            // 没有训练计划时，使用原来的逻辑
            val currentLift = progress.currentLift
            val template = WorkoutTemplate.getTemplate(userData.appSettings.currentTemplate)
            val trainingMax = userData.trainingMax.getByLift(currentLift)
            
            TodayWorkoutInfo(
                lift = currentLift,
                week = progress.currentWeek,
                day = progress.currentDay,
                template = template,
                trainingMax = trainingMax,
                canStartTraining = true,
                restrictionMessage = null
            )
        }
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
        
        // 确保有活跃的训练计划
        viewModelScope.launch {
            val activeSchedule = trainingScheduleRepository.getActiveScheduleOnce()
            if (activeSchedule == null && userDataRepository.isSetupCompleted()) {
                // 如果没有活跃的训练计划但已完成设置，创建一个默认的训练计划
                trainingScheduleRepository.createAndActivateSchedule(TrainingTemplateType.CLASSIC_4_DAY)
            }
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
            
            // 完成训练计划中的今日训练
            val currentSchedule = activeTrainingSchedule.value
            if (currentSchedule != null) {
                trainingScheduleRepository.completeTodayTraining(currentSchedule)
            }
            
            // 检查是否需要增加周期TM
            val newProgress = cycleProgress.value.completeWorkout()
            if (newProgress.isCycleComplete) {
                userDataRepository.increaseCycleTM()
            }
            
            _uiState.update { it.copy(isWorkoutInProgress = false) }
        }
    }
    
    /**
     * 创建并激活训练计划
     */
    fun createTrainingSchedule(templateType: TrainingTemplateType) {
        viewModelScope.launch {
            trainingScheduleRepository.createAndActivateSchedule(templateType)
        }
    }
    
    /**
     * 停用当前训练计划
     */
    fun deactivateCurrentSchedule() {
        viewModelScope.launch {
            trainingScheduleRepository.deactivateCurrentSchedule()
        }
    }
    
    /**
     * 强制创建测试训练计划 (用于调试)
     */
    fun forceCreateTestSchedule() {
        viewModelScope.launch {
            println("DEBUG: forceCreateTestSchedule - Creating test schedule")
            trainingScheduleRepository.createAndActivateSchedule(TrainingTemplateType.CLASSIC_4_DAY)
        }
    }
    
    /**
     * 切换调试模式
     */
    fun toggleDebugMode() {
        viewModelScope.launch {
            val currentUserData = userData.value
            val updatedSettings = currentUserData.appSettings.toggleDebugMode()
            userDataRepository.saveAppSettings(updatedSettings)
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
    val trainingMax: Double = 0.0,
    val canStartTraining: Boolean = true,
    val restrictionMessage: String? = null
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

package com.jmin.five3one.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jmin.five3one.data.model.*
import com.jmin.five3one.data.repository.UserDataRepository
import com.jmin.five3one.data.repository.UserData
import com.jmin.five3one.data.repository.WorkoutRepository
import com.jmin.five3one.data.repository.PlateCalculatorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 训练执行ViewModel
 * 管理训练过程、计算重量、记录成绩等
 */
@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val workoutRepository: WorkoutRepository,
    private val plateCalculatorRepository: PlateCalculatorRepository
) : ViewModel() {
    
    // 用户数据
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
    
    // 当前周期进度
    val cycleProgress: StateFlow<CycleProgress> = workoutRepository.getCurrentCycleProgress()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CycleProgress()
        )
    
    // 训练状态
    private val _workoutState = MutableStateFlow(WorkoutUiState())
    val workoutState: StateFlow<WorkoutUiState> = _workoutState.asStateFlow()
    
    // 当前训练信息
    val currentWorkout: StateFlow<CurrentWorkoutInfo> = combine(
        userData,
        cycleProgress,
        _workoutState
    ) { userData, progress, state ->
        val lift = state.selectedLift ?: progress.currentLift
        val template = WorkoutTemplate.getTemplate(userData.appSettings.currentTemplate)
        val trainingMax = userData.trainingMax.getByLift(lift)
        
        CurrentWorkoutInfo(
            lift = lift,
            week = progress.currentWeek,
            day = progress.currentDay,
            template = template,
            trainingMax = trainingMax,
            sets = generateWorkoutSets(template, trainingMax, progress.currentWeek)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CurrentWorkoutInfo()
    )
    
    /**
     * 选择训练动作（用于查看其他动作的训练计划）
     */
    fun selectLift(lift: LiftType) {
        _workoutState.update { it.copy(selectedLift = lift) }
    }
    
    /**
     * 开始训练
     */
    fun startWorkout() {
        // 获取当前训练的第三组（AMRAP组）的最小次数
        val currentWorkoutInfo = currentWorkout.value
        val amrapSet = currentWorkoutInfo.sets.find { it.setNumber == 3 && it.isAmrap }
        val minAmrapReps = amrapSet?.targetReps ?: 0
        
        _workoutState.update { 
            it.copy(
                isWorkoutActive = true,
                startTime = System.currentTimeMillis(),
                selectedLift = null, // 重置为当前日期对应的动作
                amrapReps = minAmrapReps // 初始化AMRAP组为最小次数
            ) 
        }
    }
    
    /**
     * 完成一组训练
     */
    fun completeSet(setNumber: Int, actualReps: Int) {
        _workoutState.update { state ->
            val updatedSets = state.completedSets.toMutableMap()
            updatedSets[setNumber] = actualReps
            
            // 检查是否是AMRAP组（第3组）且完成目标次数
            val currentWorkoutInfo = currentWorkout.value
            val amrapSet = currentWorkoutInfo.sets.find { it.setNumber == 3 && it.isAmrap }
            
            var newState = state.copy(completedSets = updatedSets)
            
            if (amrapSet != null && setNumber == 3 && actualReps >= amrapSet.targetReps) {
                // AMRAP组完成，显示庆祝弹窗
                val celebrationData = AmrapCelebrationData(
                    targetReps = amrapSet.targetReps,
                    actualReps = actualReps,
                    weight = amrapSet.weight,
                    exceededBy = actualReps - amrapSet.targetReps,
                    liftType = currentWorkoutInfo.lift
                )
                
                newState = newState.copy(
                    showAmrapCelebration = true,
                    amrapCelebrationData = celebrationData
                )
            }
            
            newState
        }
    }
    
    /**
     * 更新AMRAP组次数
     */
    fun updateAmrapReps(reps: Int) {
        // 获取当前训练的第三组（AMRAP组）的最小次数
        val currentTemplate = currentWorkout.value.template
        val minReps = currentTemplate.getTargetReps(3) // 第三组的目标次数就是最小次数
        val validReps = reps.coerceAtLeast(minReps)
        _workoutState.update { it.copy(amrapReps = validReps) }
    }
    
    /**
     * 增加AMRAP次数
     */
    fun increaseAmrapReps() {
        _workoutState.update { 
            it.copy(amrapReps = (it.amrapReps + 1).coerceAtLeast(0)) 
        }
    }
    
    /**
     * 减少AMRAP次数
     */
    fun decreaseAmrapReps() {
        _workoutState.update { 
            it.copy(amrapReps = (it.amrapReps - 1).coerceAtLeast(0)) 
        }
    }
    
    /**
     * 添加训练笔记
     */
    fun updateNotes(notes: String) {
        _workoutState.update { it.copy(notes = notes) }
    }
    
    /**
     * 设置训练感受
     */
    fun setFeeling(feeling: WorkoutFeeling) {
        _workoutState.update { it.copy(feeling = feeling) }
    }
    
    /**
     * 完成整个训练
     */
    fun completeWorkout() {
        viewModelScope.launch {
            try {
                val state = _workoutState.value
                val workout = currentWorkout.value
                
                // 构建训练组记录
                val sets = workout.sets.mapIndexed { index, setInfo ->
                    val actualReps = if (setInfo.isAmrap) {
                        state.amrapReps
                    } else {
                        state.completedSets[index + 1] ?: setInfo.targetReps
                    }
                    
                    WorkoutSet(
                        setNumber = index + 1,
                        weight = setInfo.weight,
                        targetReps = setInfo.targetReps,
                        actualReps = actualReps,
                        isCompleted = true,
                        isAmrap = setInfo.isAmrap
                    )
                }
                
                val duration = System.currentTimeMillis() - state.startTime
                
                // 保存训练记录
                workoutRepository.createWorkout(
                    lift = workout.lift,
                    week = workout.week,
                    day = workout.day,
                    template = workout.template.type,
                    trainingMax = workout.trainingMax,
                    sets = sets,
                    duration = duration,
                    notes = state.notes,
                    feeling = state.feeling
                )
                
                _workoutState.update { 
                    it.copy(
                        isWorkoutActive = false,
                        isCompleted = true
                    ) 
                }
                
            } catch (e: Exception) {
                _workoutState.update { 
                    it.copy(errorMessage = "保存训练记录失败: ${e.message}") 
                }
            }
        }
    }
    
    /**
     * 取消训练
     */
    fun cancelWorkout() {
        _workoutState.update { WorkoutUiState() }
    }
    
    /**
     * 放弃当前训练但保持训练状态活跃（归零记录重新开始）
     */
    fun abandonWorkout() {
        // 获取当前训练的第三组（AMRAP组）的最小次数
        val currentWorkoutInfo = currentWorkout.value
        val amrapSet = currentWorkoutInfo.sets.find { it.setNumber == 3 && it.isAmrap }
        val minAmrapReps = amrapSet?.targetReps ?: 0
        
        _workoutState.update { state ->
            state.copy(
                completedSets = emptyMap(),
                amrapReps = minAmrapReps, // 重置为最小次数而不是0
                notes = "",
                feeling = WorkoutFeeling.GOOD,
                currentPlateSolution = null
                // 保持 isWorkoutActive = true 和 startTime
            )
        }
    }
    
    /**
     * 计算杠铃配重
     */
    fun calculatePlateLoading(weight: Double) {
        viewModelScope.launch {
            val plateConfig = userData.value.plateConfig
            val solution = plateCalculatorRepository.calculateBestSolution(weight, plateConfig)
            _workoutState.update { it.copy(currentPlateSolution = solution) }
        }
    }
    
    /**
     * 清除配重方案
     */
    fun clearPlateSolution() {
        _workoutState.update { it.copy(currentPlateSolution = null) }
    }
    
    /**
     * 清除错误消息
     */
    fun clearError() {
        _workoutState.update { it.copy(errorMessage = null) }
    }
    
    /**
     * 显示AMRAP庆祝弹窗
     */
    fun showAmrapCelebration(celebrationData: AmrapCelebrationData) {
        _workoutState.update { 
            it.copy(
                showAmrapCelebration = true,
                amrapCelebrationData = celebrationData
            ) 
        }
    }
    
    /**
     * 关闭AMRAP庆祝弹窗
     */
    fun dismissAmrapCelebration() {
        _workoutState.update { 
            it.copy(
                showAmrapCelebration = false,
                amrapCelebrationData = null
            ) 
        }
    }
    
    /**
     * 重置训练状态
     */
    fun resetWorkout() {
        _workoutState.update { WorkoutUiState() }
    }
    
    /**
     * 生成训练组信息
     */
    private fun generateWorkoutSets(
        template: WorkoutTemplate,
        trainingMax: Double,
        week: Int
    ): List<WorkoutSetInfo> {
        val percentages = template.getWeekPercentages(week)
        return percentages.mapIndexed { index, percentage ->
            WorkoutSetInfo(
                setNumber = index + 1,
                weight = template.calculateWeight(trainingMax, week, index + 1),
                percentage = percentage,
                targetReps = template.getTargetReps(index + 1),
                isAmrap = template.isAmrapSet(index + 1)
            )
        }
    }
}

/**
 * 训练UI状态
 */
data class WorkoutUiState(
    val selectedLift: LiftType? = null,
    val isWorkoutActive: Boolean = false,
    val isCompleted: Boolean = false,
    val startTime: Long = 0L,
    val completedSets: Map<Int, Int> = emptyMap(), // 组数 -> 实际完成次数
    val amrapReps: Int = 0, // AMRAP组的次数
    val notes: String = "",
    val feeling: WorkoutFeeling = WorkoutFeeling.GOOD,
    val currentPlateSolution: PlateSolution? = null,
    val errorMessage: String? = null,
    val showAmrapCelebration: Boolean = false, // 是否显示AMRAP庆祝弹窗
    val amrapCelebrationData: AmrapCelebrationData? = null // AMRAP庆祝数据
) {
    val workoutDuration: Long
        get() = if (startTime > 0) System.currentTimeMillis() - startTime else 0L
    
    val isAllSetsCompleted: Boolean
        get() = completedSets.size >= 2 && amrapReps > 0 // 至少完成前两组和AMRAP组
}

/**
 * AMRAP庆祝数据
 */
data class AmrapCelebrationData(
    val targetReps: Int, // 目标次数
    val actualReps: Int, // 实际完成次数
    val weight: Double, // 重量
    val exceededBy: Int, // 超出的次数
    val liftType: LiftType // 训练动作
)

/**
 * 当前训练信息
 */
data class CurrentWorkoutInfo(
    val lift: LiftType = LiftType.BENCH_PRESS,
    val week: Int = 1,
    val day: Int = 1,
    val template: WorkoutTemplate = WorkoutTemplate.getTemplate(TemplateType.FIVES),
    val trainingMax: Double = 0.0,
    val sets: List<WorkoutSetInfo> = emptyList()
)

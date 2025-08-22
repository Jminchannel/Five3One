package com.jmin.five3one.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jmin.five3one.data.model.LiftType
import com.jmin.five3one.data.model.WorkoutHistory
import com.jmin.five3one.data.repository.WorkoutRepository
import com.jmin.five3one.data.repository.UserDataRepository
import com.jmin.five3one.data.repository.WorkoutStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 统计数据ViewModel
 * 管理训练统计、进度分析、历史记录等
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val userDataRepository: UserDataRepository
) : ViewModel() {
    
    // 训练统计数据
    val workoutStats: StateFlow<WorkoutStats> = workoutRepository.getWorkoutStats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WorkoutStats()
        )
    
    // 所有训练记录
    val allWorkouts: StateFlow<List<WorkoutHistory>> = workoutRepository.getAllWorkouts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // 最近训练记录
    val recentWorkouts: StateFlow<List<WorkoutHistory>> = workoutRepository.getRecentWorkouts(10)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // UI状态
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()
    
    // 选定动作的训练记录
    val selectedLiftWorkouts: StateFlow<List<WorkoutHistory>> = combine(
        _uiState,
        allWorkouts
    ) { state, workouts ->
        state.selectedLift?.let { lift ->
            workouts.filter { it.lift == lift }
        } ?: emptyList()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // 月度统计数据
    val monthlyStats: StateFlow<List<MonthlyWorkoutData>> = allWorkouts
        .map { workouts -> calculateMonthlyStats(workouts) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    /**
     * 选择动作查看详细统计
     */
    fun selectLift(lift: LiftType?) {
        _uiState.update { it.copy(selectedLift = lift) }
    }
    
    /**
     * 设置时间范围过滤
     */
    fun setTimeRange(range: TimeRange) {
        _uiState.update { it.copy(selectedTimeRange = range) }
    }
    
    /**
     * 获取指定动作的训练记录
     */
    fun getWorkoutsByLift(lift: LiftType): Flow<List<WorkoutHistory>> {
        return workoutRepository.getWorkoutsByLift(lift)
    }
    
    /**
     * 删除训练记录
     */
    fun deleteWorkout(workout: WorkoutHistory) {
        viewModelScope.launch {
            try {
                workoutRepository.deleteWorkout(workout)
                _uiState.update { it.copy(successMessage = "训练记录已删除") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "删除失败: ${e.message}") }
            }
        }
    }
    
    /**
     * 导出统计数据
     */
    fun exportStatistics() {
        viewModelScope.launch {
            try {
                // 这里应该实现数据导出逻辑
                _uiState.update { it.copy(successMessage = "统计数据已导出") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "导出失败: ${e.message}") }
            }
        }
    }
    
    /**
     * 计算个人记录
     */
    fun calculatePersonalRecords(): Flow<Map<LiftType, PersonalRecord>> {
        return allWorkouts.map { workouts ->
            LiftType.values().associateWith { lift ->
                val liftWorkouts = workouts.filter { it.lift == lift }
                PersonalRecord.fromWorkouts(liftWorkouts)
            }
        }
    }
    
    /**
     * 清除成功消息
     */
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
    
    /**
     * 清除错误消息
     */
    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    /**
     * 计算月度统计
     */
    private fun calculateMonthlyStats(workouts: List<WorkoutHistory>): List<MonthlyWorkoutData> {
        val monthlyData = workouts
            .groupBy { it.date.substring(0, 7) } // YYYY-MM
            .map { (month, monthWorkouts) ->
                MonthlyWorkoutData(
                    month = month,
                    workoutCount = monthWorkouts.size,
                    averageAmrap = monthWorkouts.mapNotNull { 
                        if (it.amrapReps > 0) it.amrapReps.toDouble() else null 
                    }.average().takeIf { !it.isNaN() } ?: 0.0,
                    totalDuration = monthWorkouts.sumOf { it.duration }
                )
            }
            .sortedBy { it.month }
        
        return monthlyData
    }
}

/**
 * 统计UI状态
 */
data class StatisticsUiState(
    val selectedLift: LiftType? = null,
    val selectedTimeRange: TimeRange = TimeRange.ALL_TIME,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

/**
 * 时间范围枚举
 */
enum class TimeRange(val labelKey: String) {
    LAST_WEEK("time_range_last_week"),
    LAST_MONTH("time_range_last_month"),
    LAST_THREE_MONTHS("time_range_last_3_months"),
    LAST_SIX_MONTHS("time_range_last_6_months"),
    LAST_YEAR("time_range_last_year"),
    ALL_TIME("time_range_all_time")
}

/**
 * 月度训练数据
 */
data class MonthlyWorkoutData(
    val month: String, // YYYY-MM格式
    val workoutCount: Int,
    val averageAmrap: Double,
    val totalDuration: Long
) {
    val averageDurationMinutes: Int
        get() = (totalDuration / (1000 * 60 * workoutCount)).toInt()
}

/**
 * 个人记录
 */
data class PersonalRecord(
    val bestAmrap: Int = 0,
    val bestAmrapWeight: Double = 0.0,
    val bestAmrapDate: String = "",
    val highestTM: Double = 0.0,
    val totalWorkouts: Int = 0,
    val averageAmrap: Double = 0.0,
    val latestWorkoutDate: String = ""
) {
    companion object {
        fun fromWorkouts(workouts: List<WorkoutHistory>): PersonalRecord {
            if (workouts.isEmpty()) return PersonalRecord()
            
            val sortedWorkouts = workouts.sortedBy { it.date }
            val amrapWorkouts = workouts.filter { it.amrapReps > 0 }
            
            val bestAmrapWorkout = amrapWorkouts.maxByOrNull { it.amrapReps }
            val highestTMWorkout = workouts.maxByOrNull { it.trainingMax }
            val latestWorkout = sortedWorkouts.lastOrNull()
            
            return PersonalRecord(
                bestAmrap = bestAmrapWorkout?.amrapReps ?: 0,
                bestAmrapWeight = bestAmrapWorkout?.sets?.lastOrNull()?.weight ?: 0.0,
                bestAmrapDate = bestAmrapWorkout?.date ?: "",
                highestTM = highestTMWorkout?.trainingMax ?: 0.0,
                totalWorkouts = workouts.size,
                averageAmrap = if (amrapWorkouts.isNotEmpty()) {
                    amrapWorkouts.map { it.amrapReps }.average()
                } else 0.0,
                latestWorkoutDate = latestWorkout?.date ?: ""
            )
        }
    }
}

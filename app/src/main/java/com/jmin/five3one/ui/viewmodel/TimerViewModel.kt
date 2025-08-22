package com.jmin.five3one.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jmin.five3one.data.model.TimerState
import com.jmin.five3one.data.repository.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 计时器ViewModel
 * 管理组间休息计时功能
 */
@HiltViewModel
class TimerViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository
) : ViewModel() {
    
    // 计时器状态
    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()
    
    // 计时器工作任务
    private var timerJob: Job? = null
    
    // 用户设置的默认休息时间
    val defaultRestTime: StateFlow<Int> = userDataRepository.getAppSettings()
        .map { it?.restTimerSeconds ?: 180 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 180
        )
    
    // UI状态
    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()
    
    init {
        // 监听默认休息时间变化
        viewModelScope.launch {
            defaultRestTime.collect { defaultTime ->
                if (!_timerState.value.isRunning) {
                    _timerState.update { 
                        it.copy(
                            timeLeftSeconds = defaultTime,
                            totalTimeSeconds = defaultTime
                        ) 
                    }
                }
            }
        }
    }
    
    /**
     * 设置计时器时间
     */
    fun setTimer(seconds: Int) {
        if (!_timerState.value.isRunning) {
            _timerState.update {
                TimerState(
                    timeLeftSeconds = seconds,
                    totalTimeSeconds = seconds,
                    isRunning = false,
                    startTime = 0L
                )
            }
        }
    }
    
    /**
     * 开始计时
     */
    fun startTimer() {
        val currentState = _timerState.value
        if (!currentState.isRunning && currentState.timeLeftSeconds > 0) {
            _timerState.update { 
                it.copy(
                    isRunning = true,
                    startTime = System.currentTimeMillis()
                ) 
            }
            
            startTimerJob()
        }
    }
    
    /**
     * 暂停计时
     */
    fun pauseTimer() {
        timerJob?.cancel()
        _timerState.update { it.copy(isRunning = false) }
    }
    
    /**
     * 停止计时（重置）
     */
    fun stopTimer() {
        timerJob?.cancel()
        val defaultTime = defaultRestTime.value
        _timerState.update {
            TimerState(
                timeLeftSeconds = defaultTime,
                totalTimeSeconds = defaultTime,
                isRunning = false,
                startTime = 0L
            )
        }
    }
    
    /**
     * 切换计时器状态（开始/暂停）
     */
    fun toggleTimer() {
        if (_timerState.value.isRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }
    
    /**
     * 添加时间
     */
    fun addTime(seconds: Int) {
        _timerState.update { state ->
            val newTimeLeft = (state.timeLeftSeconds + seconds).coerceAtLeast(0)
            val newTotalTime = if (!state.isRunning) newTimeLeft else state.totalTimeSeconds
            state.copy(
                timeLeftSeconds = newTimeLeft,
                totalTimeSeconds = newTotalTime
            )
        }
    }
    
    /**
     * 快速设置预设时间
     */
    fun setPresetTime(seconds: Int) {
        stopTimer()
        setTimer(seconds)
    }
    
    /**
     * 设置初始时间（用于从其他页面传入的时间）
     */
    fun setInitialTime(seconds: Int) {
        if (_timerState.value.timeLeftSeconds == _timerState.value.totalTimeSeconds) {
            // 只有在计时器未被修改过时才设置初始时间
            setTimer(seconds)
        }
    }
    
    /**
     * 更新默认休息时间设置
     */
    fun updateDefaultRestTime(seconds: Int) {
        viewModelScope.launch {
            try {
                val currentSettings = userDataRepository.getAppSettings().first()
                if (currentSettings != null) {
                    val updatedSettings = currentSettings.updateRestTimer(seconds)
                    userDataRepository.saveAppSettings(updatedSettings)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "更新设置失败: ${e.message}") }
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
     * 确认计时器完成
     */
    fun acknowledgeTimerComplete() {
        _uiState.update { it.copy(showCompletionDialog = false) }
        stopTimer()
    }
    
    /**
     * 启动计时器任务
     */
    private fun startTimerJob() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timerState.value.isRunning && _timerState.value.timeLeftSeconds > 0) {
                delay(1000) // 每秒更新一次
                
                _timerState.update { state ->
                    val newTimeLeft = (state.timeLeftSeconds - 1).coerceAtLeast(0)
                    state.copy(timeLeftSeconds = newTimeLeft)
                }
                
                // 检查是否完成
                if (_timerState.value.timeLeftSeconds <= 0) {
                    onTimerComplete()
                    break
                }
            }
        }
    }
    
    /**
     * 计时器完成处理
     */
    private fun onTimerComplete() {
        _timerState.update { it.copy(isRunning = false) }
        _uiState.update { it.copy(showCompletionDialog = true) }
        
        // 这里可以添加声音/振动提醒
        // TODO: 实现声音和振动提醒
    }
    
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

/**
 * 计时器UI状态
 */
data class TimerUiState(
    val showCompletionDialog: Boolean = false,
    val errorMessage: String? = null
)

/**
 * 预设时间选项
 */
enum class PresetTime(val seconds: Int, val labelKey: String) {
    NINETY_SECONDS(90, "timer_90s"),
    TWO_MINUTES(120, "timer_2min"),
    THREE_MINUTES(180, "timer_3min"),
    FOUR_MINUTES(240, "timer_4min"),
    FIVE_MINUTES(300, "timer_5min")
}

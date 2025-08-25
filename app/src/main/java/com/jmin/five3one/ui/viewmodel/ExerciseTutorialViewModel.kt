package com.jmin.five3one.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jmin.five3one.data.model.*
import com.jmin.five3one.data.repository.ExerciseTutorialRepository
import com.jmin.five3one.data.repository.TutorialWithProgress
import com.jmin.five3one.data.repository.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 动作教程页面ViewModel
 */
@HiltViewModel
class ExerciseTutorialViewModel @Inject constructor(
    private val tutorialRepository: ExerciseTutorialRepository,
    private val userDataRepository: UserDataRepository
) : ViewModel() {
    
    private val _currentExerciseType = MutableStateFlow<LiftType?>(null)
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // 当前语言
    private val currentLanguage = userDataRepository.getUserData()
        .map { it.appSettings.language }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Language.ENGLISH
        )
    
    // 教程数据
    val tutorialData: StateFlow<TutorialWithProgress?> = combine(
        _currentExerciseType,
        currentLanguage
    ) { exerciseType, language ->
        if (exerciseType != null) {
            tutorialRepository.getTutorialWithProgress(exerciseType, language)
        } else {
            flowOf(null)
        }
    }.flatMapLatest { it ?: flowOf(null) }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
    
    // 是否已完成
    val isCompleted: StateFlow<Boolean> = tutorialData
        .map { it?.progress?.isCompleted ?: false }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
    
    /**
     * 加载特定动作的教程
     */
    fun loadTutorial(exerciseType: LiftType) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                _currentExerciseType.value = exerciseType
                
                // 记录教程查看
                val language = currentLanguage.value
                tutorialRepository.recordTutorialView(exerciseType, language)
                
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 标记教程为已完成
     */
    fun markAsCompleted() {
        viewModelScope.launch {
            val exerciseType = _currentExerciseType.value ?: return@launch
            val language = currentLanguage.value
            
            try {
                tutorialRepository.markTutorialCompleted(exerciseType, language)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to mark as completed"
            }
        }
    }
    
    /**
     * 标记教程为未完成
     */
    fun markAsIncomplete() {
        viewModelScope.launch {
            val exerciseType = _currentExerciseType.value ?: return@launch
            val language = currentLanguage.value
            
            try {
                // 重置完成状态
                val progressId = TutorialProgress.generateId(exerciseType, language)
                val currentProgress = tutorialData.value?.progress
                
                if (currentProgress != null) {
                    val updatedProgress = currentProgress.copy(
                        isCompleted = false,
                        completedAt = null
                    )
                    // 这里需要在Repository中添加updateProgress方法
                    // tutorialRepository.updateTutorialProgress(updatedProgress)
                    
                    // 暂时通过重新记录查看来更新
                    tutorialRepository.recordTutorialView(exerciseType, language)
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to mark as incomplete"
            }
        }
    }
    
    /**
     * 清除错误状态
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * 获取相关教程推荐
     */
    fun getRelatedTutorials(): Flow<List<ExerciseTutorialContent>> {
        return currentLanguage.flatMapLatest { language ->
            tutorialRepository.getAllTutorialContents(language)
                .map { tutorials ->
                    // 排除当前教程，返回其他教程
                    val currentType = _currentExerciseType.value
                    tutorials.filter { it.exerciseType != currentType }.take(3)
                }
        }
    }
    
    /**
     * 搜索教程
     */
    suspend fun searchTutorials(query: String): List<ExerciseTutorialContent> {
        val language = currentLanguage.value
        return tutorialRepository.searchTutorials(query, language)
    }
}

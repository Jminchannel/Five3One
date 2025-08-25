package com.jmin.five3one.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jmin.five3one.data.model.*
import com.jmin.five3one.data.repository.ExerciseTutorialRepository
import com.jmin.five3one.data.repository.LearningCenterOverview
import com.jmin.five3one.data.repository.TutorialOverviewItem
import com.jmin.five3one.data.repository.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 学习中心ViewModel
 */
@HiltViewModel
class LearningCenterViewModel @Inject constructor(
    private val tutorialRepository: ExerciseTutorialRepository,
    private val userDataRepository: UserDataRepository
) : ViewModel() {
    
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
    
    // 学习中心概览
    val overview: StateFlow<LearningCenterOverview> = currentLanguage
        .flatMapLatest { language ->
            combine(
                tutorialRepository.getAllTutorialContents(language),
                tutorialRepository.getCompletedTutorialCount(language)
            ) { contents, completedCount ->
                val totalCount = contents.size
                val progressPercentage = if (totalCount > 0) {
                    (completedCount.toFloat() / totalCount) * 100f
                } else {
                    0f
                }
                
                // 创建教程概览项目（暂时标记为未完成，将在组合流中更新）
                val tutorialItems = contents.map { content ->
                    TutorialOverviewItem(
                        exerciseType = content.exerciseType,
                        title = content.title,
                        shortDescription = content.shortDescription,
                        estimatedDurationMinutes = 5, // 默认5分钟
                        isCompleted = false // 暂时标记为未完成
                    )
                }
                
                LearningCenterOverview(
                    totalTutorials = totalCount,
                    completedTutorials = completedCount,
                    progressPercentage = progressPercentage,
                    tutorials = tutorialItems
                )
            }
        }
        .catch { e ->
            _error.value = e.message ?: "Unknown error"
            emit(LearningCenterOverview(0, 0, 0f, emptyList()))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LearningCenterOverview(0, 0, 0f, emptyList())
        )
    
    // 所有教程内容
    val tutorialContents: StateFlow<List<ExerciseTutorialContent>> = currentLanguage
        .flatMapLatest { language ->
            tutorialRepository.getAllTutorialContents(language)
        }
        .catch { e ->
            _error.value = e.message ?: "Failed to load tutorials"
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // 推荐的下一个教程
    val recommendedTutorial: StateFlow<ExerciseTutorialContent?> = currentLanguage
        .flatMapLatest { language ->
            flow {
                try {
                    val nextTutorial = tutorialRepository.getRecommendedNextTutorial(language)
                    emit(nextTutorial)
                } catch (e: Exception) {
                    _error.value = e.message ?: "Failed to get recommendation"
                    emit(null)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    /**
     * 加载学习中心概览
     */
    fun loadOverview() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // 确保教程数据已初始化
                tutorialRepository.initializePresetTutorials()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to initialize tutorials"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 刷新数据
     */
    fun refresh() {
        loadOverview()
    }
    
    /**
     * 搜索教程
     */
    suspend fun searchTutorials(query: String): List<ExerciseTutorialContent> {
        return try {
            val language = currentLanguage.value
            tutorialRepository.searchTutorials(query, language)
        } catch (e: Exception) {
            _error.value = e.message ?: "Search failed"
            emptyList()
        }
    }
    
    /**
     * 获取特定动作类型的教程
     */
    fun getTutorialByType(exerciseType: LiftType): Flow<ExerciseTutorialContent?> {
        return currentLanguage.flatMapLatest { language ->
            tutorialRepository.getTutorialContent(exerciseType, language)
        }
    }
    
    /**
     * 标记教程为已完成
     */
    fun markTutorialCompleted(exerciseType: LiftType) {
        viewModelScope.launch {
            try {
                val language = currentLanguage.value
                tutorialRepository.markTutorialCompleted(exerciseType, language)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to mark as completed"
            }
        }
    }
    
    /**
     * 记录教程查看
     */
    fun recordTutorialView(exerciseType: LiftType) {
        viewModelScope.launch {
            try {
                val language = currentLanguage.value
                tutorialRepository.recordTutorialView(exerciseType, language)
            } catch (e: Exception) {
                // 查看记录失败不需要显示错误给用户
                // 只记录日志即可
            }
        }
    }
    
    /**
     * 获取学习统计数据
     */
    fun getLearningStats(): Flow<LearningStats> {
        return currentLanguage.flatMapLatest { language ->
            combine(
                tutorialRepository.getCompletedTutorialCount(language),
                tutorialRepository.getAllTutorialContents(language)
            ) { completedCount, allTutorials ->
                val totalCount = allTutorials.size
                val completionRate = if (totalCount > 0) {
                    (completedCount.toFloat() / totalCount) * 100f
                } else {
                    0f
                }
                
                LearningStats(
                    totalTutorials = totalCount,
                    completedTutorials = completedCount,
                    completionRate = completionRate,
                    exerciseTypeStats = LiftType.values().map { type ->
                        val tutorialExists = allTutorials.any { it.exerciseType == type }
                        ExerciseTypeStats(
                            exerciseType = type,
                            hasCompleted = tutorialExists && completedCount > 0 // 简化的逻辑，实际需要检查具体类型
                        )
                    }
                )
            }
        }
    }
    
    /**
     * 清除错误状态
     */
    fun clearError() {
        _error.value = null
    }
}

/**
 * 学习统计数据
 */
data class LearningStats(
    val totalTutorials: Int,
    val completedTutorials: Int,
    val completionRate: Float,
    val exerciseTypeStats: List<ExerciseTypeStats>
)

/**
 * 动作类型统计
 */
data class ExerciseTypeStats(
    val exerciseType: LiftType,
    val hasCompleted: Boolean
)

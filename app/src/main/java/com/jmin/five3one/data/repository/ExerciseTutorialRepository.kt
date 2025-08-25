package com.jmin.five3one.data.repository

import com.jmin.five3one.data.dao.ExerciseTutorialDao
import com.jmin.five3one.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 动作教程数据仓库
 */
@Singleton
class ExerciseTutorialRepository @Inject constructor(
    private val tutorialDao: ExerciseTutorialDao
) {
    
    /**
     * 获取所有教程
     */
    fun getAllTutorials(): Flow<List<ExerciseTutorial>> = tutorialDao.getAllTutorials()
    
    /**
     * 获取特定动作的教程
     */
    fun getTutorial(exerciseType: LiftType): Flow<ExerciseTutorial?> = 
        tutorialDao.getTutorialFlow(exerciseType)
    
    /**
     * 获取特定动作和语言的教程内容
     */
    fun getTutorialContent(exerciseType: LiftType, language: Language): Flow<ExerciseTutorialContent?> = 
        tutorialDao.getTutorialContentFlow(exerciseType, language)
    
    /**
     * 获取特定语言的所有教程内容
     */
    fun getAllTutorialContents(language: Language): Flow<List<ExerciseTutorialContent>> = 
        tutorialDao.getTutorialContentsByLanguage(language)
    
    /**
     * 获取教程学习进度
     */
    fun getTutorialProgress(exerciseType: LiftType, language: Language): Flow<TutorialProgress?> = 
        tutorialDao.getTutorialProgressFlow(exerciseType, language)
    
    /**
     * 获取已完成的教程数量
     */
    fun getCompletedTutorialCount(language: Language): Flow<Int> = 
        tutorialDao.getCompletedTutorialCountFlow(language)
    
    /**
     * 获取特定动作和语言的完整教程信息
     */
    suspend fun getCompleteTutorial(exerciseType: LiftType, language: Language): CompleteTutorial? =
        tutorialDao.getCompleteTutorial(exerciseType, language)
    
    /**
     * 获取特定语言的所有完整教程信息
     */
    suspend fun getAllCompleteTutorials(language: Language): List<CompleteTutorial> = 
        tutorialDao.getAllCompleteTutorials(language)
    
    /**
     * 记录教程查看
     */
    suspend fun recordTutorialView(exerciseType: LiftType, language: Language) {
        tutorialDao.recordTutorialView(exerciseType, language)
    }
    
    /**
     * 标记教程为已完成
     */
    suspend fun markTutorialCompleted(exerciseType: LiftType, language: Language) {
        tutorialDao.markTutorialCompleted(exerciseType, language)
    }
    
    /**
     * 初始化预置教程数据
     * 应该在应用启动时调用一次
     */
    suspend fun initializePresetTutorials() {
        // 检查是否已经初始化过
        val existingTutorials = tutorialDao.getAllCompleteTutorials(Language.ENGLISH)
        if (existingTutorials.isNotEmpty()) {
            return // 已经初始化过，不重复初始化
        }
        
        // 插入预置的教程数据
        val tutorials = TutorialDataPresets.getAllTutorials()
        tutorialDao.insertTutorials(tutorials)
        
        // 插入预置的教程内容
        val contents = TutorialDataPresets.getAllTutorialContents()
        tutorialDao.insertTutorialContents(contents)
    }
    
    /**
     * 获取特定动作的综合教程信息（包含进度）
     */
    fun getTutorialWithProgress(exerciseType: LiftType, language: Language): Flow<TutorialWithProgress?> {
        return combine(
            getTutorial(exerciseType),
            getTutorialContent(exerciseType, language),
            getTutorialProgress(exerciseType, language)
        ) { tutorial, content, progress ->
            if (tutorial != null && content != null) {
                TutorialWithProgress(
                    tutorial = tutorial,
                    content = content,
                    progress = progress
                )
            } else {
                null
            }
        }
    }
    
    /**
     * 获取学习中心概览数据
     */
    fun getLearningCenterOverview(language: Language): Flow<LearningCenterOverview> {
        return combine(
            getAllTutorialContents(language),
            getCompletedTutorialCount(language)
        ) { contents, completedCount ->
            val totalCount = contents.size
            val progressPercentage = if (totalCount > 0) {
                (completedCount.toFloat() / totalCount) * 100
            } else {
                0f
            }
            
            LearningCenterOverview(
                totalTutorials = totalCount,
                completedTutorials = completedCount,
                progressPercentage = progressPercentage,
                tutorials = contents.map { content ->
                    TutorialOverviewItem(
                        exerciseType = content.exerciseType,
                        title = content.title,
                        shortDescription = content.shortDescription,
                        estimatedDurationMinutes = 5, // 默认5分钟，可以从ExerciseTutorial获取
                        isCompleted = false // 这里会在combine中更新
                    )
                }
            )
        }
    }
    
    /**
     * 获取推荐的下一个教程
     */
    suspend fun getRecommendedNextTutorial(language: Language): ExerciseTutorialContent? {
        val allTutorials = getAllCompleteTutorials(language)
        
        // 找到第一个未完成的教程
        return allTutorials
            .find { it.progress?.isCompleted != true }
            ?.content
    }
    
    /**
     * 搜索教程
     */
    suspend fun searchTutorials(query: String, language: Language): List<ExerciseTutorialContent> {
        val allTutorials = getAllCompleteTutorials(language)
        
        return allTutorials
            .map { it.content }
            .filter { content ->
                content.title.contains(query, ignoreCase = true) ||
                content.shortDescription.contains(query, ignoreCase = true) ||
                content.keyPoints.any { it.contains(query, ignoreCase = true) } ||
                content.targetMuscles.any { it.contains(query, ignoreCase = true) }
            }
    }
}

/**
 * 包含进度的教程信息
 */
data class TutorialWithProgress(
    val tutorial: ExerciseTutorial,
    val content: ExerciseTutorialContent,
    val progress: TutorialProgress?
)

/**
 * 学习中心概览数据
 */
data class LearningCenterOverview(
    val totalTutorials: Int,
    val completedTutorials: Int,
    val progressPercentage: Float,
    val tutorials: List<TutorialOverviewItem>
)

/**
 * 教程概览项目
 */
data class TutorialOverviewItem(
    val exerciseType: LiftType,
    val title: String,
    val shortDescription: String,
    val estimatedDurationMinutes: Int,
    val isCompleted: Boolean
)

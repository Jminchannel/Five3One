package com.jmin.five3one.data.dao

import androidx.room.*
import com.jmin.five3one.data.model.*
import kotlinx.coroutines.flow.Flow

/**
 * 动作教程数据访问对象
 */
@Dao
interface ExerciseTutorialDao {
    
    // ExerciseTutorial CRUD operations
    @Query("SELECT * FROM exercise_tutorials")
    fun getAllTutorials(): Flow<List<ExerciseTutorial>>
    
    @Query("SELECT * FROM exercise_tutorials WHERE exerciseType = :exerciseType")
    suspend fun getTutorial(exerciseType: LiftType): ExerciseTutorial?
    
    @Query("SELECT * FROM exercise_tutorials WHERE exerciseType = :exerciseType")
    fun getTutorialFlow(exerciseType: LiftType): Flow<ExerciseTutorial?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTutorial(tutorial: ExerciseTutorial)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTutorials(tutorials: List<ExerciseTutorial>)
    
    @Update
    suspend fun updateTutorial(tutorial: ExerciseTutorial)
    
    @Delete
    suspend fun deleteTutorial(tutorial: ExerciseTutorial)
    
    // ExerciseTutorialContent CRUD operations
    @Query("SELECT * FROM exercise_tutorial_content")
    fun getAllTutorialContents(): Flow<List<ExerciseTutorialContent>>
    
    @Query("SELECT * FROM exercise_tutorial_content WHERE exerciseType = :exerciseType AND language = :language")
    suspend fun getTutorialContent(exerciseType: LiftType, language: Language): ExerciseTutorialContent?
    
    @Query("SELECT * FROM exercise_tutorial_content WHERE exerciseType = :exerciseType AND language = :language")
    fun getTutorialContentFlow(exerciseType: LiftType, language: Language): Flow<ExerciseTutorialContent?>
    
    @Query("SELECT * FROM exercise_tutorial_content WHERE exerciseType = :exerciseType")
    fun getTutorialContentsByExercise(exerciseType: LiftType): Flow<List<ExerciseTutorialContent>>
    
    @Query("SELECT * FROM exercise_tutorial_content WHERE language = :language")
    fun getTutorialContentsByLanguage(language: Language): Flow<List<ExerciseTutorialContent>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTutorialContent(content: ExerciseTutorialContent)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTutorialContents(contents: List<ExerciseTutorialContent>)
    
    @Update
    suspend fun updateTutorialContent(content: ExerciseTutorialContent)
    
    @Delete
    suspend fun deleteTutorialContent(content: ExerciseTutorialContent)
    
    // TutorialProgress CRUD operations
    @Query("SELECT * FROM tutorial_progress")
    fun getAllTutorialProgress(): Flow<List<TutorialProgress>>
    
    @Query("SELECT * FROM tutorial_progress WHERE exerciseType = :exerciseType AND language = :language")
    suspend fun getTutorialProgress(exerciseType: LiftType, language: Language): TutorialProgress?
    
    @Query("SELECT * FROM tutorial_progress WHERE exerciseType = :exerciseType AND language = :language")
    fun getTutorialProgressFlow(exerciseType: LiftType, language: Language): Flow<TutorialProgress?>
    
    @Query("SELECT * FROM tutorial_progress WHERE exerciseType = :exerciseType")
    fun getTutorialProgressByExercise(exerciseType: LiftType): Flow<List<TutorialProgress>>
    
    @Query("SELECT COUNT(*) FROM tutorial_progress WHERE isCompleted = 1 AND language = :language")
    suspend fun getCompletedTutorialCount(language: Language): Int
    
    @Query("SELECT COUNT(*) FROM tutorial_progress WHERE isCompleted = 1 AND language = :language")
    fun getCompletedTutorialCountFlow(language: Language): Flow<Int>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTutorialProgress(progress: TutorialProgress)
    
    @Update
    suspend fun updateTutorialProgress(progress: TutorialProgress)
    
    @Delete
    suspend fun deleteTutorialProgress(progress: TutorialProgress)
    
    // Combined queries for convenience
    /**
     * 获取特定动作和语言的完整教程信息
     */
    @Transaction
    suspend fun getCompleteTutorial(
        exerciseType: LiftType, 
        language: Language
    ): CompleteTutorial? {
        val tutorial = getTutorial(exerciseType)
        val content = getTutorialContent(exerciseType, language)
        val progress = getTutorialProgress(exerciseType, language)
        
        return if (tutorial != null && content != null) {
            CompleteTutorial(
                tutorial = tutorial,
                content = content,
                progress = progress
            )
        } else null
    }
    
    /**
     * 获取特定语言的所有教程信息
     */
    @Transaction
    suspend fun getAllCompleteTutorials(language: Language): List<CompleteTutorial> {
        val tutorials = mutableListOf<CompleteTutorial>()
        
        LiftType.values().forEach { exerciseType ->
            getCompleteTutorial(exerciseType, language)?.let { tutorial ->
                tutorials.add(tutorial)
            }
        }
        
        return tutorials
    }
    
    /**
     * 标记教程为已完成
     */
    @Transaction
    suspend fun markTutorialCompleted(exerciseType: LiftType, language: Language) {
        val progressId = TutorialProgress.generateId(exerciseType, language)
        val existingProgress = getTutorialProgress(exerciseType, language)
        
        if (existingProgress != null) {
            val updatedProgress = existingProgress.copy(
                isCompleted = true,
                completedAt = System.currentTimeMillis(),
                viewCount = existingProgress.viewCount + 1,
                lastViewedAt = System.currentTimeMillis()
            )
            updateTutorialProgress(updatedProgress)
        } else {
            val newProgress = TutorialProgress(
                id = progressId,
                exerciseType = exerciseType,
                language = language,
                isCompleted = true,
                completedAt = System.currentTimeMillis(),
                viewCount = 1,
                lastViewedAt = System.currentTimeMillis()
            )
            insertTutorialProgress(newProgress)
        }
    }
    
    /**
     * 记录教程查看
     */
    @Transaction
    suspend fun recordTutorialView(exerciseType: LiftType, language: Language) {
        val progressId = TutorialProgress.generateId(exerciseType, language)
        val existingProgress = getTutorialProgress(exerciseType, language)
        
        if (existingProgress != null) {
            val updatedProgress = existingProgress.copy(
                viewCount = existingProgress.viewCount + 1,
                lastViewedAt = System.currentTimeMillis()
            )
            updateTutorialProgress(updatedProgress)
        } else {
            val newProgress = TutorialProgress(
                id = progressId,
                exerciseType = exerciseType,
                language = language,
                viewCount = 1,
                lastViewedAt = System.currentTimeMillis()
            )
            insertTutorialProgress(newProgress)
        }
    }
}



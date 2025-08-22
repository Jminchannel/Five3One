package com.jmin.five3one.data.dao

import androidx.room.*
import com.jmin.five3one.data.model.TrainingMax
import kotlinx.coroutines.flow.Flow

/**
 * 训练最大重量数据访问对象
 */
@Dao
interface TrainingMaxDao {
    
    /**
     * 获取TM记录（响应式）
     */
    @Query("SELECT * FROM training_max WHERE id = 1")
    fun getTrainingMax(): Flow<TrainingMax?>
    
    /**
     * 获取TM记录（单次）
     */
    @Query("SELECT * FROM training_max WHERE id = 1")
    suspend fun getTrainingMaxOnce(): TrainingMax?
    
    /**
     * 插入或更新TM记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(trainingMax: TrainingMax)
    
    /**
     * 更新TM记录
     */
    @Update
    suspend fun update(trainingMax: TrainingMax)
    
    /**
     * 删除TM记录
     */
    @Query("DELETE FROM training_max")
    suspend fun deleteAll()
}

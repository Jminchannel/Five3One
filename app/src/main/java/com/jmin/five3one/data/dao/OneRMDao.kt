package com.jmin.five3one.data.dao

import androidx.room.*
import com.jmin.five3one.data.model.OneRM
import kotlinx.coroutines.flow.Flow

/**
 * 1RM数据访问对象
 */
@Dao
interface OneRMDao {
    
    /**
     * 获取1RM记录（响应式）
     */
    @Query("SELECT * FROM one_rm WHERE id = 1")
    fun getOneRM(): Flow<OneRM?>
    
    /**
     * 获取1RM记录（单次）
     */
    @Query("SELECT * FROM one_rm WHERE id = 1")
    suspend fun getOneRMOnce(): OneRM?
    
    /**
     * 插入或更新1RM记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(oneRM: OneRM)
    
    /**
     * 更新1RM记录
     */
    @Update
    suspend fun update(oneRM: OneRM)
    
    /**
     * 删除1RM记录
     */
    @Query("DELETE FROM one_rm")
    suspend fun deleteAll()
}

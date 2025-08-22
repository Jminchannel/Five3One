package com.jmin.five3one.data.dao

import androidx.room.*
import com.jmin.five3one.data.model.PlateConfig
import kotlinx.coroutines.flow.Flow

/**
 * 杠铃片配置数据访问对象
 */
@Dao
interface PlateConfigDao {
    
    /**
     * 获取杠铃片配置（响应式）
     */
    @Query("SELECT * FROM plate_config WHERE id = 1")
    fun getPlateConfig(): Flow<PlateConfig?>
    
    /**
     * 获取杠铃片配置（单次）
     */
    @Query("SELECT * FROM plate_config WHERE id = 1")
    suspend fun getPlateConfigOnce(): PlateConfig?
    
    /**
     * 插入或更新杠铃片配置
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(plateConfig: PlateConfig)
    
    /**
     * 更新杠铃片配置
     */
    @Update
    suspend fun update(plateConfig: PlateConfig)
    
    /**
     * 删除杠铃片配置
     */
    @Query("DELETE FROM plate_config")
    suspend fun deleteAll()
}

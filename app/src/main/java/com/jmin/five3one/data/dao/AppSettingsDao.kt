package com.jmin.five3one.data.dao

import androidx.room.*
import com.jmin.five3one.data.model.AppSettings
import kotlinx.coroutines.flow.Flow

/**
 * 应用设置数据访问对象
 */
@Dao
interface AppSettingsDao {
    
    /**
     * 获取应用设置（响应式）
     */
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun getAppSettings(): Flow<AppSettings?>
    
    /**
     * 获取应用设置（单次）
     */
    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun getAppSettingsOnce(): AppSettings?
    
    /**
     * 插入或更新应用设置
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: AppSettings)
    
    /**
     * 更新应用设置
     */
    @Update
    suspend fun update(settings: AppSettings)
    
    /**
     * 检查是否已完成初始设置
     */
    @Query("SELECT isSetupCompleted FROM app_settings WHERE id = 1")
    suspend fun isSetupCompleted(): Boolean?
    
    /**
     * 删除应用设置
     */
    @Query("DELETE FROM app_settings")
    suspend fun deleteAll()
}

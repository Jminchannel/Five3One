package com.jmin.five3one.data.repository

import com.jmin.five3one.data.dao.*
import com.jmin.five3one.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 用户数据仓库
 * 管理1RM、TM、杠铃片配置等用户核心数据
 */
@Singleton
class UserDataRepository @Inject constructor(
    private val oneRMDao: OneRMDao,
    private val trainingMaxDao: TrainingMaxDao,
    private val plateConfigDao: PlateConfigDao,
    private val appSettingsDao: AppSettingsDao
) {
    
    /**
     * 获取1RM数据流
     */
    fun getOneRM(): Flow<OneRM?> = oneRMDao.getOneRM()
    
    /**
     * 获取TM数据流
     */
    fun getTrainingMax(): Flow<TrainingMax?> = trainingMaxDao.getTrainingMax()
    
    /**
     * 获取杠铃片配置数据流
     */
    fun getPlateConfig(): Flow<PlateConfig?> = plateConfigDao.getPlateConfig()
    
    /**
     * 获取应用设置数据流
     */
    fun getAppSettings(): Flow<AppSettings?> = appSettingsDao.getAppSettings()
    
    /**
     * 获取组合的用户数据流
     */
    fun getUserData(): Flow<UserData> = combine(
        getOneRM(),
        getTrainingMax(),
        getPlateConfig(),
        getAppSettings()
    ) { oneRM, trainingMax, plateConfig, settings ->
        UserData(
            oneRM = oneRM ?: OneRM(),
            trainingMax = trainingMax ?: oneRM?.calculateTrainingMax() ?: TrainingMax(),
            plateConfig = plateConfig ?: PlateConfig(),
            appSettings = settings ?: AppSettings()
        )
    }
    
    /**
     * 保存1RM数据
     */
    suspend fun saveOneRM(oneRM: OneRM) {
        oneRMDao.insertOrUpdate(oneRM)
        // 自动更新TM为90% 1RM
        val newTM = oneRM.calculateTrainingMax().roundToHalf()
        trainingMaxDao.insertOrUpdate(newTM)
    }
    
    /**
     * 保存TM数据
     */
    suspend fun saveTrainingMax(trainingMax: TrainingMax) {
        trainingMaxDao.insertOrUpdate(trainingMax.roundToHalf())
    }
    
    /**
     * 保存杠铃片配置
     */
    suspend fun savePlateConfig(plateConfig: PlateConfig) {
        plateConfigDao.insertOrUpdate(plateConfig)
    }
    
    /**
     * 保存应用设置
     */
    suspend fun saveAppSettings(settings: AppSettings) {
        appSettingsDao.insertOrUpdate(settings)
    }
    
    /**
     * 更新指定动作的1RM
     */
    suspend fun updateOneRM(lift: LiftType, weight: Double) {
        val currentOneRM = oneRMDao.getOneRMOnce() ?: OneRM()
        val updatedOneRM = currentOneRM.updateLift(lift, weight)
        saveOneRM(updatedOneRM)
    }
    
    /**
     * 更新指定动作的TM
     */
    suspend fun updateTrainingMax(lift: LiftType, weight: Double) {
        val currentTM = trainingMaxDao.getTrainingMaxOnce() ?: TrainingMax()
        val updatedTM = currentTM.updateLift(lift, weight)
        saveTrainingMax(updatedTM)
    }
    
    /**
     * 增加周期结束后的TM
     */
    suspend fun increaseCycleTM() {
        val currentTM = trainingMaxDao.getTrainingMaxOnce() ?: TrainingMax()
        val increasedTM = currentTM.increaseCycle()
        saveTrainingMax(increasedTM)
    }
    
    /**
     * 更新杠铃杆重量
     */
    suspend fun updateBarbellWeight(weight: Double) {
        val currentConfig = plateConfigDao.getPlateConfigOnce() ?: PlateConfig()
        val updatedConfig = currentConfig.updateBarbellWeight(weight)
        savePlateConfig(updatedConfig)
    }
    
    /**
     * 切换杠铃片可用性
     */
    suspend fun togglePlate(weight: Double) {
        val currentConfig = plateConfigDao.getPlateConfigOnce() ?: PlateConfig()
        val updatedConfig = currentConfig.togglePlate(weight)
        savePlateConfig(updatedConfig)
    }
    
    /**
     * 检查是否已完成设置
     */
    suspend fun isSetupCompleted(): Boolean {
        return appSettingsDao.isSetupCompleted() ?: false
    }
    
    /**
     * 完成初始设置
     */
    suspend fun completeSetup() {
        val currentSettings = appSettingsDao.getAppSettingsOnce() ?: AppSettings()
        val updatedSettings = currentSettings.completeSetup()
        saveAppSettings(updatedSettings)
    }
    
    /**
     * 重置所有用户数据
     */
    suspend fun resetAllData() {
        oneRMDao.deleteAll()
        trainingMaxDao.deleteAll()
        plateConfigDao.deleteAll()
        appSettingsDao.deleteAll()
    }
    
    /**
     * 导出用户数据
     */
    suspend fun exportUserData(): UserDataExport {
        return UserDataExport(
            oneRM = oneRMDao.getOneRMOnce(),
            trainingMax = trainingMaxDao.getTrainingMaxOnce(),
            plateConfig = plateConfigDao.getPlateConfigOnce(),
            appSettings = appSettingsDao.getAppSettingsOnce(),
            exportTime = System.currentTimeMillis()
        )
    }
}

/**
 * 组合的用户数据
 */
data class UserData(
    val oneRM: OneRM,
    val trainingMax: TrainingMax,
    val plateConfig: PlateConfig,
    val appSettings: AppSettings
)

/**
 * 用户数据导出格式
 */
@kotlinx.serialization.Serializable
data class UserDataExport(
    val oneRM: OneRM?,
    val trainingMax: TrainingMax?,
    val plateConfig: PlateConfig?,
    val appSettings: AppSettings?,
    val exportTime: Long
)

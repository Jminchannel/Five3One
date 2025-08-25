package com.jmin.five3one.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * 应用设置
 */
@Entity(tableName = "app_settings")
@Serializable
data class AppSettings(
    @PrimaryKey
    val id: Int = 1, // 只有一条记录
    val language: Language = Language.ENGLISH,
    val currentTemplate: TemplateType = TemplateType.FIVES,
    val restTimerSeconds: Int = 180, // 默认3分钟
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val isSetupCompleted: Boolean = false,
    val isDarkMode: Boolean = false, // 深色模式
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * 更新语言设置
     */
    fun updateLanguage(language: Language): AppSettings {
        return copy(language = language, updatedAt = System.currentTimeMillis())
    }
    
    /**
     * 更新训练模板
     */
    fun updateTemplate(template: TemplateType): AppSettings {
        return copy(currentTemplate = template, updatedAt = System.currentTimeMillis())
    }
    
    /**
     * 更新休息时间
     */
    fun updateRestTimer(seconds: Int): AppSettings {
        return copy(restTimerSeconds = seconds, updatedAt = System.currentTimeMillis())
    }
    
    /**
     * 完成初始设置
     */
    fun completeSetup(): AppSettings {
        return copy(isSetupCompleted = true, updatedAt = System.currentTimeMillis())
    }
    
    /**
     * 切换声音设置
     */
    fun toggleSound(): AppSettings {
        return copy(soundEnabled = !soundEnabled, updatedAt = System.currentTimeMillis())
    }
    
    /**
     * 切换振动设置
     */
    fun toggleVibration(): AppSettings {
        return copy(vibrationEnabled = !vibrationEnabled, updatedAt = System.currentTimeMillis())
    }
    
    /**
     * 切换深色模式
     */
    fun toggleDarkMode(): AppSettings {
        return copy(isDarkMode = !isDarkMode, updatedAt = System.currentTimeMillis())
    }
}

/**
 * 支持的语言
 */
@Serializable
enum class Language(
    val code: String,
    val displayNameKey: String,
    val localeTag: String
) {
    ENGLISH("en", "language_english", "en"),
    CHINESE_SIMPLIFIED("zh-CN", "language_chinese_simplified", "zh-CN"),
    CHINESE_TRADITIONAL("zh-TW", "language_chinese_traditional", "zh-TW"),
    INDONESIAN("id", "language_indonesian", "id");
    
    companion object {
        /**
         * 根据语言代码获取语言
         */
        fun fromCode(code: String): Language {
            return values().find { it.code == code } ?: ENGLISH
        }
        
        /**
         * 获取系统默认语言
         */
        fun getSystemDefault(): Language {
            val systemLocale = java.util.Locale.getDefault().language
            return when (systemLocale) {
                "zh" -> {
                    val country = java.util.Locale.getDefault().country
                    if (country == "TW" || country == "HK" || country == "MO") {
                        CHINESE_TRADITIONAL
                    } else {
                        CHINESE_SIMPLIFIED
                    }
                }
                "id" -> INDONESIAN
                else -> ENGLISH
            }
        }
    }
}

/**
 * 计时器状态
 */
@Serializable
data class TimerState(
    val isRunning: Boolean = false,
    val timeLeftSeconds: Int = 180,
    val totalTimeSeconds: Int = 180,
    val startTime: Long = 0L
) {
    /**
     * 剩余时间百分比（0.0 - 1.0）
     */
    val progressPercentage: Float
        get() = if (totalTimeSeconds > 0) {
            timeLeftSeconds.toFloat() / totalTimeSeconds.toFloat()
        } else 0f
        
    /**
     * 是否已完成
     */
    val isCompleted: Boolean
        get() = timeLeftSeconds <= 0
        
    /**
     * 格式化时间显示 (MM:SS)
     */
    val formattedTime: String
        get() {
            val minutes = timeLeftSeconds / 60
            val seconds = timeLeftSeconds % 60
            return String.format("%d:%02d", minutes, seconds)
        }
}

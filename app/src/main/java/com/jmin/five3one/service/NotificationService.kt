package com.jmin.five3one.service

import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 通知服务 - 处理声音和振动提醒
 */
@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
    
    /**
     * 播放计时器完成提醒
     * @param soundEnabled 是否启用声音
     * @param vibrationEnabled 是否启用振动
     */
    fun playTimerCompleteNotification(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled) {
            playNotificationSound()
        }
        
        if (vibrationEnabled) {
            vibrate()
        }
    }
    
    /**
     * 播放通知声音
     */
    private fun playNotificationSound() {
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, notification)
            ringtone?.play()
        } catch (e: Exception) {
            // 如果播放失败，静默处理
            e.printStackTrace()
        }
    }
    
    /**
     * 触发振动
     */
    private fun vibrate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // 使用新的VibrationEffect API
                val vibrationPattern = longArrayOf(0, 200, 100, 200, 100, 200)
                val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
                
                val vibrationEffect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    VibrationEffect.createWaveform(vibrationPattern, amplitudes, -1)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(vibrationPattern, -1)
                    return
                }
                
                vibrator.vibrate(vibrationEffect)
            } else {
                // 兼容老版本API
                val vibrationPattern = longArrayOf(0, 200, 100, 200, 100, 200)
                @Suppress("DEPRECATION")
                vibrator.vibrate(vibrationPattern, -1)
            }
        } catch (e: Exception) {
            // 如果振动失败，静默处理
            e.printStackTrace()
        }
    }
    
    /**
     * 播放短促的反馈振动（用于按钮点击等）
     */
    fun playHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
        } catch (e: Exception) {
            // 如果振动失败，静默处理
            e.printStackTrace()
        }
    }
    
    /**
     * 检查是否支持振动
     */
    fun hasVibrator(): Boolean {
        return vibrator.hasVibrator()
    }
}

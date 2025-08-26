package com.jmin.five3one

import android.app.Application
import android.content.res.Configuration
import android.content.res.Resources
import com.jmin.five3one.data.repository.ExerciseTutorialRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * 应用程序主类
 * 使用Hilt进行依赖注入
 */
@HiltAndroidApp
class Five3OneApplication : Application() {
    
    @Inject
    lateinit var exerciseTutorialRepository: ExerciseTutorialRepository
    
    override fun onCreate() {
        super.onCreate()
        
        // 设置默认语言为英文
        setDefaultLanguage()
        
        // 在后台线程初始化教程数据
        CoroutineScope(Dispatchers.IO).launch {
            try {
                exerciseTutorialRepository.initializePresetTutorials()
            } catch (e: Exception) {
                // 静默处理初始化失败，不影响应用启动
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 设置默认语言为英文
     */
    private fun setDefaultLanguage() {
        val locale = Locale.ENGLISH
        Locale.setDefault(locale)
        
        try {
            val resources: Resources = resources
            val configuration: Configuration = resources.configuration
            configuration.setLocale(locale)
            resources.updateConfiguration(configuration, resources.displayMetrics)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
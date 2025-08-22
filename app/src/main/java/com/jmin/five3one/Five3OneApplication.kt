package com.jmin.five3one

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * 应用程序主类
 * 使用Hilt进行依赖注入
 */
@HiltAndroidApp
class Five3OneApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
    }
}

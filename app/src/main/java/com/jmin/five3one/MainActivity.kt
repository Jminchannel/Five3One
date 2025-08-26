package com.jmin.five3one

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.jmin.five3one.navigation.AppNavigation
import com.jmin.five3one.navigation.Screen
import com.jmin.five3one.ui.theme.Five3oneTheme
import com.jmin.five3one.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 确保应用启动时使用英文语言设置
        updateLanguage(com.jmin.five3one.data.model.Language.ENGLISH)
        
        enableEdgeToEdge()
        setContent {
            Five3OneApp(
                onLanguageChange = { language ->
                    updateLanguage(language)
                }
            )
        }
    }
    
    private fun updateLanguage(language: com.jmin.five3one.data.model.Language) {
        val locale = when (language) {
            com.jmin.five3one.data.model.Language.ENGLISH -> java.util.Locale.ENGLISH
            com.jmin.five3one.data.model.Language.CHINESE_SIMPLIFIED -> java.util.Locale.SIMPLIFIED_CHINESE
            com.jmin.five3one.data.model.Language.CHINESE_TRADITIONAL -> java.util.Locale.TRADITIONAL_CHINESE
            com.jmin.five3one.data.model.Language.INDONESIAN -> java.util.Locale("id", "ID")
        }
        
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}

@Composable
fun Five3OneApp(
    onLanguageChange: (com.jmin.five3one.data.model.Language) -> Unit = {},
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val uiState by mainViewModel.uiState.collectAsState()
    val userData by mainViewModel.userData.collectAsState()
    
    // 监听语言变更
    LaunchedEffect(userData.appSettings.language) {
        onLanguageChange(userData.appSettings.language)
    }
    
    // 决定是否使用深色模式
    val isDarkTheme = userData.appSettings.isDarkMode
    
    // 根据设置完成状态决定起始页面
    val startDestination = if (uiState.isSetupCompleted) {
        Screen.Dashboard.route
    } else {
        Screen.Welcome.route
    }
    
    Five3oneTheme(darkTheme = isDarkTheme) {
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            AppNavigation(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
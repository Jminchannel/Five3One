package com.jmin.five3one.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.jmin.five3one.ui.screen.AppearanceScreen
import com.jmin.five3one.ui.screen.DashboardScreen
import com.jmin.five3one.ui.screen.PlateCalculatorScreen
import com.jmin.five3one.ui.screen.SettingsScreen
import com.jmin.five3one.ui.screen.SetupScreen
import com.jmin.five3one.ui.screen.SplashScreen
import com.jmin.five3one.ui.screen.StatisticsScreen
import com.jmin.five3one.ui.screen.TimerScreen
import com.jmin.five3one.ui.screen.WelcomeScreen
import com.jmin.five3one.ui.screen.WorkoutScreen

/**
 * 应用主导航组件
 */
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // 启动画面
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToWelcome = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        // 欢迎页面
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onNavigateToSetup = {
                    navController.navigate(Screen.Setup.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }
        
        // 设置流程
        composable(
            route = "${Screen.Setup.route}?step={step}",
            arguments = listOf(
                navArgument("step") { 
                    type = NavType.IntType
                    defaultValue = 1
                }
            )
        ) { backStackEntry ->
            val initialStep = backStackEntry.arguments?.getInt("step") ?: 1
            
            SetupScreen(
                onSetupComplete = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Setup.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                initialStep = initialStep
            )
        }
        
        // 设置流程（无参数版本，保持兼容性）
        composable(Screen.Setup.route) {
            SetupScreen(
                onSetupComplete = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Setup.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // 主界面
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToWorkout = {
                    navController.navigate(Screen.Workout.route)
                },
                onNavigateToPlateCalculator = {
                    navController.navigate(Screen.PlateCalculator.route)
                },
                onNavigateToTimer = {
                    navController.navigate(Screen.Timer.route)
                },
                onNavigateToStatistics = {
                    navController.navigate(Screen.Statistics.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        
        // 训练页面
        composable(Screen.Workout.route) {
            WorkoutScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToTimer = { restTime, autoStart ->
                    navController.navigate("${Screen.Timer.route}/$restTime/$autoStart")
                },
                onNavigateToPlateCalculator = {
                    navController.navigate(Screen.PlateCalculator.route)
                }
            )
        }
        
        // 配重计算器
        composable(Screen.PlateCalculator.route) {
            PlateCalculatorScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // 计时器
        composable(
            route = "${Screen.Timer.route}/{restTime}/{autoStart}",
            arguments = listOf(
                navArgument("restTime") { 
                    type = NavType.IntType
                    defaultValue = 90
                },
                navArgument("autoStart") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val restTime = backStackEntry.arguments?.getInt("restTime") ?: 90
            val autoStart = backStackEntry.arguments?.getBoolean("autoStart") ?: false
            
            TimerScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                initialRestTime = restTime,
                autoStart = autoStart
            )
        }
        
        // 计时器（无参数版本，保持兼容性）
        composable(Screen.Timer.route) {
            TimerScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // 统计页面
        composable(Screen.Statistics.route) {
            StatisticsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // 设置页面
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToSetup = { step ->
                    navController.navigate("${Screen.Setup.route}?step=$step")
                },
                onNavigateToAppearance = {
                    navController.navigate(Screen.Appearance.route)
                }
            )
        }
        
        // 外观设置页面
        composable(Screen.Appearance.route) {
            AppearanceScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

/**
 * 应用路由定义
 */
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object Setup : Screen("setup")
    object Dashboard : Screen("dashboard")
    object Workout : Screen("workout")
    object PlateCalculator : Screen("plate_calculator")
    object Timer : Screen("timer")
    object Statistics : Screen("statistics")
    object Settings : Screen("settings")
    object Appearance : Screen("appearance")
}

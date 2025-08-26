package com.jmin.five3one.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.jmin.five3one.ui.screen.About531Screen
import com.jmin.five3one.ui.screen.AppearanceScreen
import com.jmin.five3one.ui.screen.DashboardScreen
import com.jmin.five3one.ui.screen.ExerciseTutorialScreen
import com.jmin.five3one.ui.screen.LearningCenterScreen
import com.jmin.five3one.ui.screen.PlateCalculatorScreen
import com.jmin.five3one.ui.screen.SettingsScreen
import com.jmin.five3one.ui.screen.SetupScreen
import com.jmin.five3one.ui.screen.SplashScreen
import com.jmin.five3one.ui.screen.StatisticsScreen
import com.jmin.five3one.ui.screen.TemplatePreviewScreen
import com.jmin.five3one.ui.screen.TemplateSelectionScreen
import com.jmin.five3one.ui.screen.ScheduleScreen
import com.jmin.five3one.ui.screen.TimerScreen
import com.jmin.five3one.ui.screen.WelcomeScreen
import com.jmin.five3one.ui.screen.WorkoutDetailScreen
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
                    navController.navigate(Screen.TemplateSelection.route) {
                        popUpTo(Screen.Setup.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLearningCenter = {
                    navController.navigate(Screen.LearningCenter.route)
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
                },
                onNavigateToLearningCenter = {
                    navController.navigate(Screen.LearningCenter.route)
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
                },
                onNavigateToSetup = { step ->
                    navController.navigate("${Screen.Setup.route}?step=$step")
                },
                onNavigateToLearningCenter = {
                    navController.navigate(Screen.LearningCenter.route)
                },
                onNavigateToSchedule = {
                    navController.navigate(Screen.Schedule.route)
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
                },
                onNavigateToWorkoutDetail = { workoutId ->
                    navController.navigate("${Screen.WorkoutDetail.route}/$workoutId")
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
                },
                onNavigateToAbout531 = {
                    navController.navigate(Screen.About531.route)
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
        
        // 学习中心页面
        composable(Screen.LearningCenter.route) {
            LearningCenterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToTutorial = { exerciseType ->
                    navController.navigate("${Screen.ExerciseTutorial.route}/$exerciseType")
                }
            )
        }
        
        // 动作教程页面
        composable(
            route = "${Screen.ExerciseTutorial.route}/{exerciseType}",
            arguments = listOf(navArgument("exerciseType") { type = NavType.StringType })
        ) { backStackEntry ->
            val exerciseTypeString = backStackEntry.arguments?.getString("exerciseType") ?: ""
            val exerciseType = try {
                com.jmin.five3one.data.model.LiftType.valueOf(exerciseTypeString)
            } catch (e: IllegalArgumentException) {
                com.jmin.five3one.data.model.LiftType.SQUAT // 默认值
            }
            ExerciseTutorialScreen(
                exerciseType = exerciseType,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // 训练详情页面
        composable(
            route = "${Screen.WorkoutDetail.route}/{workoutId}",
            arguments = listOf(navArgument("workoutId") { type = NavType.LongType })
        ) { backStackEntry ->
            val workoutId = backStackEntry.arguments?.getLong("workoutId") ?: 0L
            WorkoutDetailScreen(
                workoutId = workoutId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // 关于531训练法页面
        composable(Screen.About531.route) {
            About531Screen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // 训练模板选择页面
        composable(Screen.TemplateSelection.route) {
            TemplateSelectionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onTemplateSelected = { template ->
                    // 传递模板类型到预览页面
                    navController.navigate("${Screen.TemplatePreview.route}/${template.type.name}")
                }
            )
        }
        
        // 训练模板预览页面
        composable(
            route = "${Screen.TemplatePreview.route}/{templateType}",
            arguments = listOf(
                navArgument("templateType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val templateTypeName = backStackEntry.arguments?.getString("templateType") ?: ""
            val templateType = try {
                com.jmin.five3one.data.model.TrainingTemplateType.valueOf(templateTypeName)
            } catch (e: Exception) {
                com.jmin.five3one.data.model.TrainingTemplateType.CLASSIC_4_DAY
            }
            val template = com.jmin.five3one.data.model.TrainingTemplates.getTemplateByType(templateType)
                ?: com.jmin.five3one.data.model.TrainingTemplates.classic4Day
            
            TemplatePreviewScreen(
                template = template,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onGeneratePlan = { selectedTemplate ->
                    // 训练计划创建逻辑已在TemplatePreviewScreen中处理
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }
        
        // 训练日程
        composable(Screen.Schedule.route) {
            ScheduleScreen(
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
    object LearningCenter : Screen("learning_center")
    object ExerciseTutorial : Screen("exercise_tutorial")
    object WorkoutDetail : Screen("workout_detail")
    object About531 : Screen("about_531")
    object TemplateSelection : Screen("template_selection")
    object TemplatePreview : Screen("template_preview")
    object Schedule : Screen("schedule")
}

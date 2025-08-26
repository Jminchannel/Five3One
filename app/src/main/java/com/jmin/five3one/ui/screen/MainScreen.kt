package com.jmin.five3one.ui.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jmin.five3one.R
import com.jmin.five3one.ui.viewmodel.MainViewModel
import com.jmin.five3one.navigation.Screen

/**
 * 主屏幕，包含底部导航栏
 */
@Composable
fun MainScreen(
    onNavigateToWorkout: () -> Unit,
    onNavigateToPlateCalculator: () -> Unit,
    onNavigateToTimer: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSetup: (Int) -> Unit,
    onNavigateToLearningCenter: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val bottomNavController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = bottomNavController,
                onNavigateToWorkout = onNavigateToWorkout,
                onNavigateToStatistics = onNavigateToStatistics,
                onNavigateToSettings = onNavigateToSettings
            )
        }
    ) { paddingValues ->
        MainNavHost(
            navController = bottomNavController,
            paddingValues = paddingValues,
            onNavigateToWorkout = onNavigateToWorkout,
            onNavigateToPlateCalculator = onNavigateToPlateCalculator,
            onNavigateToTimer = onNavigateToTimer,
            onNavigateToStatistics = onNavigateToStatistics,
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToSetup = onNavigateToSetup,
            onNavigateToLearningCenter = onNavigateToLearningCenter,
            onNavigateToSchedule = onNavigateToSchedule,
            mainViewModel = mainViewModel
        )
    }
}

@Composable
private fun BottomNavigationBar(
    navController: NavHostController,
    onNavigateToWorkout: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val items = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Workout,
        BottomNavItem.Statistics,
        BottomNavItem.Profile
    )
    
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = null) },
                label = { Text(stringResource(item.title)) },
                selected = currentRoute == item.route,
                onClick = {
                    when (item) {
                        BottomNavItem.Dashboard -> {
                            navController.navigate(BottomNavItem.Dashboard.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                        BottomNavItem.Workout -> onNavigateToWorkout()
                        BottomNavItem.Statistics -> onNavigateToStatistics()
                        BottomNavItem.Profile -> onNavigateToSettings()
                    }
                }
            )
        }
    }
}

@Composable
private fun MainNavHost(
    navController: NavHostController,
    paddingValues: PaddingValues,
    onNavigateToWorkout: () -> Unit,
    onNavigateToPlateCalculator: () -> Unit,
    onNavigateToTimer: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSetup: (Int) -> Unit,
    onNavigateToLearningCenter: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    mainViewModel: MainViewModel
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Dashboard.route,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(BottomNavItem.Dashboard.route) {
            DashboardScreen(
                onNavigateToWorkout = onNavigateToWorkout,
                onNavigateToPlateCalculator = onNavigateToPlateCalculator,
                onNavigateToTimer = onNavigateToTimer,
                onNavigateToStatistics = onNavigateToStatistics,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToSetup = onNavigateToSetup,
                onNavigateToLearningCenter = onNavigateToLearningCenter,
                onNavigateToSchedule = onNavigateToSchedule,
                viewModel = mainViewModel
            )
        }
        
        composable(BottomNavItem.Workout.route) {
            // 处理底部导航栏中的训练页面
        }
        
        composable(BottomNavItem.Statistics.route) {
            // 处理底部导航栏中的统计页面
        }
        
        composable(BottomNavItem.Profile.route) {
            // 处理底部导航栏中的我的页面
        }
    }
}

sealed class BottomNavItem(val route: String, val title: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Dashboard : BottomNavItem("dashboard", R.string.home, Icons.Default.Home)
    object Workout : BottomNavItem(Screen.Workout.route, R.string.workout, Icons.Default.PlayArrow)
    object Statistics : BottomNavItem(Screen.Statistics.route, R.string.statistics, Icons.Default.BarChart)
    object Profile : BottomNavItem(Screen.Settings.route, R.string.profile, Icons.Default.Person)
}
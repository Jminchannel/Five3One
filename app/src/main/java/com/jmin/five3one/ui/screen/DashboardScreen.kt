package com.jmin.five3one.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jmin.five3one.R
import com.jmin.five3one.ui.viewmodel.MainViewModel

/**
 * 主界面/仪表板
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToWorkout: () -> Unit,
    onNavigateToPlateCalculator: () -> Unit,
    onNavigateToTimer: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val userData by viewModel.userData.collectAsState()
    val todayWorkout by viewModel.todayWorkout.collectAsState()
    val cycleProgress by viewModel.cycleProgress.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.dashboard_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(
                                R.string.current_week_day,
                                cycleProgress.currentWeek,
                                cycleProgress.currentDay
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text(stringResource(R.string.home)) },
                    selected = true,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                    label = { Text(stringResource(R.string.workout)) },
                    selected = false,
                    onClick = onNavigateToWorkout
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                    label = { Text(stringResource(R.string.statistics)) },
                    selected = false,
                    onClick = onNavigateToStatistics
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text(stringResource(R.string.profile)) },
                    selected = false,
                    onClick = onNavigateToSettings
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // 今日训练卡片
                TodayWorkoutCard(
                    todayWorkout = todayWorkout,
                    onStartWorkout = onNavigateToWorkout
                )
            }
            
            item {
                // 快速操作
                QuickActionsCard(
                    onNavigateToPlateCalculator = onNavigateToPlateCalculator,
                    onNavigateToTimer = onNavigateToTimer,
                    onNavigateToStatistics = onNavigateToStatistics,
                    onNavigateToSettings = onNavigateToSettings
                )
            }
            
            item {
                // TM概览
                TMOverviewCard(userData = userData)
            }
        }
    }
}

@Composable
private fun TodayWorkoutCard(
    todayWorkout: com.jmin.five3one.ui.viewmodel.TodayWorkoutInfo,
    onStartWorkout: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.today_workout),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = getLiftDisplayName(todayWorkout.lift),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "TM: ${todayWorkout.trainingMax}kg",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = stringResource(
                                R.string.current_week_day,
                                todayWorkout.week,
                                todayWorkout.day
                            )
                        )
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 显示今日训练组数
            val sets = todayWorkout.getWorkoutSets()
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.training_sets),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    sets.forEach { set ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "第${set.setNumber}组:",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "${set.weight}kg × ${set.targetReps}${if (set.isAmrap) "+" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onStartWorkout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.start_workout))
            }
        }
    }
}

@Composable
private fun getLiftDisplayName(lift: com.jmin.five3one.data.model.LiftType): String {
    return when (lift) {
        com.jmin.five3one.data.model.LiftType.BENCH_PRESS -> stringResource(R.string.lift_bench)
        com.jmin.five3one.data.model.LiftType.SQUAT -> stringResource(R.string.lift_squat)
        com.jmin.five3one.data.model.LiftType.DEADLIFT -> stringResource(R.string.lift_deadlift)
        com.jmin.five3one.data.model.LiftType.OVERHEAD_PRESS -> stringResource(R.string.lift_press)
    }
}

@Composable
private fun QuickActionsCard(
    onNavigateToPlateCalculator: () -> Unit,
    onNavigateToTimer: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.quick_actions),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionButton(
                    icon = Icons.Default.Calculate,
                    text = stringResource(R.string.plate_calculator),
                    onClick = onNavigateToPlateCalculator
                )
                QuickActionButton(
                    icon = Icons.Default.Timer,
                    text = stringResource(R.string.training_timer),
                    onClick = onNavigateToTimer
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionButton(
                    icon = Icons.Default.BarChart,
                    text = stringResource(R.string.progress_view),
                    onClick = onNavigateToStatistics
                )
                QuickActionButton(
                    icon = Icons.Default.Edit,
                    text = stringResource(R.string.adjust_plan),
                    onClick = onNavigateToSettings
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        FilledTonalButton(
            onClick = onClick,
            modifier = Modifier.size(56.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2
        )
    }
}

@Composable
private fun TMOverviewCard(
    userData: com.jmin.five3one.data.repository.UserData
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.current_tm),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TMItem(
                    name = stringResource(R.string.lift_bench),
                    weight = userData.trainingMax.benchPress,
                    color = MaterialTheme.colorScheme.error
                )
                TMItem(
                    name = stringResource(R.string.lift_squat),
                    weight = userData.trainingMax.squat,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TMItem(
                    name = stringResource(R.string.lift_deadlift),
                    weight = userData.trainingMax.deadlift,
                    color = MaterialTheme.colorScheme.secondary
                )
                TMItem(
                    name = stringResource(R.string.lift_press),
                    weight = userData.trainingMax.overheadPress,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun TMItem(
    name: String,
    weight: Double,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${weight}kg",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

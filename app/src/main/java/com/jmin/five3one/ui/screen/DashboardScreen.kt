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
    onNavigateToSetup: (Int) -> Unit,
    onNavigateToLearningCenter: () -> Unit,
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
                // TM概览
                TMOverviewCard(userData = userData)
            }
            item {
                // 快速操作
                QuickActionsCard(
                    onNavigateToPlateCalculator = onNavigateToPlateCalculator,
                    onNavigateToTimer = onNavigateToTimer,
                    onNavigateToStatistics = onNavigateToStatistics,
                    onNavigateToSetup = onNavigateToSetup,
                    onNavigateToLearningCenter = onNavigateToLearningCenter
                )
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
                                text = stringResource(R.string.set_number_format, set.setNumber),
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
    onNavigateToSetup: (Int) -> Unit,
    onNavigateToLearningCenter: () -> Unit
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
            
            // 纵向列表形式的快速操作
            QuickActionListItem(
                icon = Icons.Default.Calculate,
                title = stringResource(R.string.plate_calculator),
                subtitle = stringResource(R.string.plate_calculator_desc),
                onClick = onNavigateToPlateCalculator
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            QuickActionListItem(
                icon = Icons.Default.Timer,
                title = stringResource(R.string.training_timer),
                subtitle = stringResource(R.string.training_timer_desc),
                onClick = onNavigateToTimer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            QuickActionListItem(
                icon = Icons.Default.BarChart,
                title = stringResource(R.string.progress_view),
                subtitle = stringResource(R.string.progress_view_desc),
                onClick = onNavigateToStatistics
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            QuickActionListItem(
                icon = Icons.Default.School,
                title = stringResource(R.string.exercise_tutorials),
                subtitle = stringResource(R.string.exercise_tutorials_desc),
                onClick = onNavigateToLearningCenter
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            QuickActionListItem(
                icon = Icons.Default.Edit,
                title = stringResource(R.string.adjust_plan),
                subtitle = stringResource(R.string.adjust_plan_desc),
                onClick = { onNavigateToSetup(1) } // 直接跳转到1RM设置
            )
        }
    }
}

@Composable
private fun QuickActionListItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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

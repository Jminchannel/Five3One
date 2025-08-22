package com.jmin.five3one.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jmin.five3one.R
import com.jmin.five3one.ui.viewmodel.StatisticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val workoutStats by viewModel.workoutStats.collectAsState()
    val recentWorkouts by viewModel.recentWorkouts.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.training_stats)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
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
                // 训练概览统计
                TrainingOverviewCard(workoutStats = workoutStats)
            }
            
            item {
                // 四大项进度
                BigFourProgressCard(
                    liftProgress = workoutStats.liftProgress,
                    onLiftSelect = viewModel::selectLift
                )
            }
            
            item {
                // 最近训练记录
                RecentWorkoutsCard(
                    recentWorkouts = recentWorkouts,
                    onDeleteWorkout = viewModel::deleteWorkout
                )
            }
            
            // 错误消息
            uiState.errorMessage?.let { error ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrainingOverviewCard(
    workoutStats: com.jmin.five3one.data.repository.WorkoutStats
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
            Text(
                text = stringResource(R.string.training_overview),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    title = stringResource(R.string.completed_workouts),
                    value = workoutStats.totalWorkouts.toString(),
                    color = MaterialTheme.colorScheme.primary
                )
                
                StatItem(
                    title = stringResource(R.string.completed_cycles),
                    value = workoutStats.completedCycles.toString(),
                    color = MaterialTheme.colorScheme.secondary
                )
                
                StatItem(
                    title = stringResource(R.string.average_amrap),
                    value = String.format("%.1f", workoutStats.averageAmrapReps),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    title: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun BigFourProgressCard(
    liftProgress: Map<com.jmin.five3one.data.model.LiftType, com.jmin.five3one.data.repository.LiftProgress>,
    onLiftSelect: (com.jmin.five3one.data.model.LiftType?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.big_four_progress),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            liftProgress.forEach { (lift, progress) ->
                LiftProgressItem(
                    lift = lift,
                    progress = progress,
                    onClick = { onLiftSelect(lift) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun LiftProgressItem(
    lift: com.jmin.five3one.data.model.LiftType,
    progress: com.jmin.five3one.data.repository.LiftProgress,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getLiftDisplayName(lift),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = stringResource(R.string.progress_percentage, progress.progressPercentage),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (progress.progressPercentage > 0) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "TM: ${progress.currentTM}kg",
                    style = MaterialTheme.typography.bodySmall
                )
                
                Text(
                    text = stringResource(R.string.amrap_count, progress.bestAmrap),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun RecentWorkoutsCard(
    recentWorkouts: List<com.jmin.five3one.data.model.WorkoutHistory>,
    onDeleteWorkout: (com.jmin.five3one.data.model.WorkoutHistory) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.recent_workouts),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (recentWorkouts.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_workout_history),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            } else {
                recentWorkouts.forEach { workout ->
                    WorkoutHistoryItem(
                        workout = workout,
                        onDelete = { onDeleteWorkout(workout) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun WorkoutHistoryItem(
    workout: com.jmin.five3one.data.model.WorkoutHistory,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = getLiftDisplayName(workout.lift),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = stringResource(
                        R.string.workout_date,
                        workout.date,
                        workout.week,
                        workout.day
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (workout.amrapReps > 0) {
                    Text(
                        text = stringResource(R.string.amrap_count, workout.amrapReps),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
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

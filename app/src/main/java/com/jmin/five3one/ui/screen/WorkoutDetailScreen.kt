package com.jmin.five3one.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jmin.five3one.R
import com.jmin.five3one.data.model.*
import com.jmin.five3one.ui.viewmodel.WorkoutDetailUiState
import com.jmin.five3one.ui.viewmodel.WorkoutDetailViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    workoutId: Long,
    onNavigateBack: () -> Unit,
    viewModel: WorkoutDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(workoutId) {
        viewModel.loadWorkoutDetail(workoutId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.workout_detail)) },
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
        val currentState = uiState
        when (currentState) {
            is WorkoutDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is WorkoutDetailUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        WorkoutOverviewCard(workout = currentState.workout)
                    }
                    
                    item {
                        WorkoutSetsCard(sets = currentState.workout.sets)
                    }
                    
                    item {
                        WorkoutPerformanceCard(workout = currentState.workout)
                    }
                    
                    if (currentState.workout.notes.isNotEmpty()) {
                        item {
                            WorkoutNotesCard(notes = currentState.workout.notes)
                        }
                    }
                }
            }
            
            is WorkoutDetailUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = currentState.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadWorkoutDetail(workoutId) }) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutOverviewCard(workout: WorkoutHistory) {
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getLiftDisplayName(workout.lift),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Text(
                    text = workout.feeling.emoji,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = formatWorkoutDate(workout.date),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Text(
                text = stringResource(
                    R.string.week_day_template,
                    workout.week,
                    workout.day,
                    getTemplateDisplayName(workout.template)
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.training_max_kg, workout.trainingMax),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                if (workout.duration > 0) {
                    Text(
                        text = formatDuration(workout.duration),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkoutSetsCard(sets: List<WorkoutSet>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.workout_sets),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            sets.forEach { set ->
                WorkoutSetItem(set = set)
                if (set != sets.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun WorkoutSetItem(set: WorkoutSet) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (set.isAmrap) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 组数标识
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (set.isAmrap) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (set.isAmrap) "+" else set.setNumber.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 重量和次数信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.weight_kg, set.weight),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (set.isAmrap) {
                            stringResource(R.string.amrap_reps_format, set.targetReps, set.actualReps)
                        } else {
                            stringResource(R.string.reps_format, set.actualReps, set.targetReps)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (set.isTargetMet && set.isCompleted) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                if (set.isAmrap && set.bonusReps > 0) {
                    Text(
                        text = stringResource(R.string.bonus_reps, set.bonusReps),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkoutPerformanceCard(workout: WorkoutHistory) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.workout_performance),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.performance_rating),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = getPerformanceDisplayName(workout.performance),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = getPerformanceColor(workout.performance)
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = stringResource(R.string.training_feeling),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = workout.feeling.emoji,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = getFeelingDisplayName(workout.feeling),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            if (workout.amrapReps > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.amrap_achievement, workout.amrapReps),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutNotesCard(notes: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Notes,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.workout_notes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = notes,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// 辅助函数
@Composable
private fun getLiftDisplayName(lift: LiftType): String {
    return when (lift) {
        LiftType.BENCH_PRESS -> stringResource(R.string.lift_bench)
        LiftType.SQUAT -> stringResource(R.string.lift_squat)
        LiftType.DEADLIFT -> stringResource(R.string.lift_deadlift)
        LiftType.OVERHEAD_PRESS -> stringResource(R.string.lift_press)
    }
}

@Composable
private fun getTemplateDisplayName(template: TemplateType): String {
    return when (template) {
        TemplateType.FIVE_THREE_ONE -> "5/3/1"
        TemplateType.THREES -> "3/3/3+"
        TemplateType.FIVES -> "5/5/5+"
    }
}

@Composable
private fun getPerformanceDisplayName(performance: WorkoutPerformance): String {
    return when (performance) {
        WorkoutPerformance.EXCELLENT -> stringResource(R.string.performance_excellent)
        WorkoutPerformance.GOOD -> stringResource(R.string.performance_good)
        WorkoutPerformance.FAIR -> stringResource(R.string.performance_fair)
        WorkoutPerformance.POOR -> stringResource(R.string.performance_poor)
        WorkoutPerformance.INCOMPLETE -> stringResource(R.string.performance_incomplete)
    }
}

@Composable
private fun getFeelingDisplayName(feeling: WorkoutFeeling): String {
    return when (feeling) {
        WorkoutFeeling.EXCELLENT -> stringResource(R.string.feeling_excellent)
        WorkoutFeeling.GOOD -> stringResource(R.string.feeling_good)
        WorkoutFeeling.FAIR -> stringResource(R.string.feeling_fair)
        WorkoutFeeling.POOR -> stringResource(R.string.feeling_poor)
    }
}

@Composable
private fun getPerformanceColor(performance: WorkoutPerformance): Color {
    return when (performance) {
        WorkoutPerformance.EXCELLENT -> Color(0xFF4CAF50) // Green
        WorkoutPerformance.GOOD -> Color(0xFF2196F3) // Blue
        WorkoutPerformance.FAIR -> Color(0xFFFF9800) // Orange
        WorkoutPerformance.POOR -> Color(0xFFF44336) // Red
        WorkoutPerformance.INCOMPLETE -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

private fun formatWorkoutDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}

private fun formatDuration(duration: Long): String {
    val minutes = duration / (1000 * 60)
    val seconds = (duration % (1000 * 60)) / 1000
    return "${minutes}分${seconds}秒"
}

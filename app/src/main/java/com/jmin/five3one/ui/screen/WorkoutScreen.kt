package com.jmin.five3one.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.jmin.five3one.R
import com.jmin.five3one.ui.viewmodel.WorkoutViewModel
import com.jmin.five3one.ui.viewmodel.AmrapCelebrationData

@Composable
@Preview
fun Iwantsee() {
    WorkoutScreen(
        onNavigateBack = {},
        onNavigateToTimer = { _, _ -> },
        onNavigateToPlateCalculator = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTimer: (restTime: Int, autoStart: Boolean) -> Unit,
    onNavigateToPlateCalculator: () -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val workoutState by viewModel.workoutState.collectAsState()
    val currentWorkout by viewModel.currentWorkout.collectAsState()
    
    // 完成训练后返回
    LaunchedEffect(workoutState.isCompleted) {
        if (workoutState.isCompleted) {
            onNavigateBack()
        }
    }
    
    // AMRAP庆祝弹窗
    if (workoutState.showAmrapCelebration && workoutState.amrapCelebrationData != null) {
        AmrapCelebrationDialog(
            celebrationData = workoutState.amrapCelebrationData!!,
            onDismiss = {
                viewModel.dismissAmrapCelebration()
                // AMRAP组完成后自动开始3分钟休息计时
                onNavigateToTimer(180, true) // AMRAP组180秒，自动开始
            },
            onViewProgress = {
                viewModel.dismissAmrapCelebration()
                // 这里可以导航到统计页面
            }
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.workout_details)) },
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
                // 训练概览卡片
                WorkoutOverviewCard(
                    currentWorkout = currentWorkout,
                    workoutState = workoutState
                )
            }

            // 配重方案显示
            workoutState.currentPlateSolution?.let { solution ->
                item {
                    PlateLoadingCard(
                        solution = solution,
                        onDismiss = viewModel::clearPlateSolution
                    )
                }
            }

            item {
                // 主训练组
                MainSetsCard(
                    sets = currentWorkout.sets,
                    workoutState = workoutState,
                    onSetComplete = viewModel::completeSet,
                    onAmrapChange = viewModel::updateAmrapReps,
                    onCalculatePlate = { weight ->
                        viewModel.calculatePlateLoading(weight)
                    },
                    onNavigateToTimer = onNavigateToTimer
                )
            }

            
            item {
                // 训练控制
                WorkoutControlCard(
                    workoutState = workoutState,
                    onStartWorkout = viewModel::startWorkout,
                    onCompleteWorkout = viewModel::completeWorkout,
                    onCancelWorkout = viewModel::cancelWorkout,
                    onAbandonWorkout = viewModel::abandonWorkout,
                    onUpdateNotes = viewModel::updateNotes,
                    onSetFeeling = viewModel::setFeeling
                )
            }
        }
    }
}

@Composable
private fun WorkoutOverviewCard(
    currentWorkout: com.jmin.five3one.ui.viewmodel.CurrentWorkoutInfo,
    workoutState: com.jmin.five3one.ui.viewmodel.WorkoutUiState
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
                text = stringResource(
                    R.string.week_mode,
                    currentWorkout.week,
                    currentWorkout.template.type.id.uppercase()
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "TM: ${currentWorkout.trainingMax}kg",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 训练进度
            val totalSets = currentWorkout.sets.size
            // 只计算真正完成的组数（包括AMRAP组必须实际完成才计数）
            val completedSets = workoutState.completedSets.size
            
            LinearProgressIndicator(
                progress = { completedSets.toFloat() / totalSets.toFloat() },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(R.string.sets_progress, completedSets, totalSets),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            
            if (workoutState.isWorkoutActive) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "训练时长: ${formatDuration(workoutState.workoutDuration)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun MainSetsCard(
    sets: List<com.jmin.five3one.ui.viewmodel.WorkoutSetInfo>,
    workoutState: com.jmin.five3one.ui.viewmodel.WorkoutUiState,
    onSetComplete: (Int, Int) -> Unit,
    onAmrapChange: (Int) -> Unit,
    onCalculatePlate: (Double) -> Unit,
    onNavigateToTimer: (Int, Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.main_training),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            sets.forEachIndexed { index, set ->
                // 检查前一组是否已完成，第一组总是可操作
                val isPreviousSetCompleted = if(index == 0) {
                    true
                } else {
                    workoutState.completedSets.containsKey(sets[index - 1].setNumber)
                }
                // 当前组是否已完成
                val isCurrentSetCompleted = workoutState.completedSets.containsKey(set.setNumber)
                SetItemCard(
                    set = set,
                    isCompleted = isCurrentSetCompleted,
                    actualReps = if (set.isAmrap) {
                        // 对于AMRAP组，如果还没开始训练或amrapReps为0，显示最小次数
                        if (!workoutState.isWorkoutActive || workoutState.amrapReps == 0) {
                            set.targetReps
                        } else {
                            workoutState.amrapReps
                        }
                    } else {
                        workoutState.completedSets[set.setNumber]
                    },
                    onSetComplete = { reps -> onSetComplete(set.setNumber, reps) },
                    onAmrapChange = onAmrapChange,
                    onCalculatePlate = onCalculatePlate,
                    onNavigateToTimer = onNavigateToTimer,
                    isWorkoutActive = workoutState.isWorkoutActive,
                    isEnabled = isPreviousSetCompleted && !isCurrentSetCompleted
                )
                
                if (index < sets.size - 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun WorkoutControlCard(
    workoutState: com.jmin.five3one.ui.viewmodel.WorkoutUiState,
    onStartWorkout: () -> Unit,
    onCompleteWorkout: () -> Unit,
    onCancelWorkout: () -> Unit,
    onAbandonWorkout: () -> Unit,
    onUpdateNotes: (String) -> Unit,
    onSetFeeling: (com.jmin.five3one.data.model.WorkoutFeeling) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (!workoutState.isWorkoutActive) {
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
            } else {
                // 训练进行中的控制
                Text(
                    text = stringResource(R.string.workout_notes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = workoutState.notes,
                    onValueChange = onUpdateNotes,
                    label = { Text(stringResource(R.string.add_notes_optional)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = stringResource(R.string.workout_feeling),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(com.jmin.five3one.data.model.WorkoutFeeling.values().size) { index ->
                        val feeling = com.jmin.five3one.data.model.WorkoutFeeling.values()[index]
                        FilterChip(
                            onClick = { onSetFeeling(feeling) },
                            label = {
                                Text("${feeling.emoji} ${getFeelingString(feeling)}")
                            },
                            selected = workoutState.feeling == feeling
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 判断是否可以放弃训练：主项训练所有组都未完成
                val canAbandonWorkout = workoutState.completedSets.isEmpty()
                
                Column {
                    // 如果可以放弃训练，显示放弃按钮
                    if (canAbandonWorkout) {
                        OutlinedButton(
                            onClick = onAbandonWorkout,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("放弃训练")
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // 取消和完成训练按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onCancelWorkout,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                        
                        Button(
                            onClick = onCompleteWorkout,
                            enabled = workoutState.isAllSetsCompleted,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.complete_workout))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlateLoadingCard(
    solution: com.jmin.five3one.data.model.PlateSolution,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
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
                    text = stringResource(R.string.plate_loading_solution),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "目标: ${solution.targetWeight}kg → 实际: ${solution.actualWeight}kg",
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (!solution.isExactMatch) {
                Text(
                    text = "误差: ${if (solution.error > 0) "+" else ""}${solution.error}kg",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "每侧杠铃片:",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(solution.platesPerSide.size) { index ->
                    val plate = solution.platesPerSide[index]
                    AssistChip(
                        onClick = { },
                        label = { Text("${plate}kg") }
                    )
                }
            }
        }
    }
}

@Composable
private fun SetItemCard(
    set: com.jmin.five3one.ui.viewmodel.WorkoutSetInfo,
    isCompleted: Boolean,
    actualReps: Int?,
    onSetComplete: (Int) -> Unit,
    onAmrapChange: (Int) -> Unit,
    onCalculatePlate: (Double) -> Unit,
    onNavigateToTimer: (Int, Boolean) -> Unit,
    isWorkoutActive: Boolean,
    isEnabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
            } else if(!isEnabled) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (set.isAmrap && isEnabled) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.set_number, set.setNumber),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else if (!isEnabled) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                        
                        if (set.isAmrap && isEnabled) {
                            Spacer(modifier = Modifier.width(8.dp))
                            AssistChip(
                                onClick = { },
                                label = { Text(stringResource(R.string.amrap_label)) },
                                enabled = isEnabled
                            )
                        }
                        
                        if (isCompleted) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Completed",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    Text(
                        text = "${set.weight}kg × ${set.targetReps}${if (set.isAmrap) " +" else ""}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = stringResource(R.string.tm_percentage, (set.percentage * 100).toInt()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (isEnabled) {
                    // 配重计算按钮
                    IconButton(
                        onClick = { onCalculatePlate(set.weight) },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "Calculate Plates"
                        )
                    }
                }
            }
            
            if (set.isAmrap && isEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.amrap_instruction),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            // 显示完成状态或操作按钮
            if (isCompleted) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "恭喜您！已完成 ${if (set.isAmrap) "${actualReps ?: set.targetReps} 次" else "${set.targetReps} 次"}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else if (isWorkoutActive && isEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                if (set.isAmrap) {
                    // AMRAP 次数控制
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.actual_reps),
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    val currentReps = actualReps ?: set.targetReps
                                    val newReps = (currentReps - 1).coerceAtLeast(set.targetReps)
                                    onAmrapChange(newReps)
                                },
                                enabled = !isCompleted && (actualReps ?: set.targetReps) > set.targetReps
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease")
                            }

                            Text(
                                text = "${actualReps ?: set.targetReps}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            IconButton(
                                onClick = { onAmrapChange((actualReps ?: set.targetReps) + 1) },
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // AMRAP 完成按钮
                    Button(
                        onClick = {
                            onSetComplete(actualReps ?: set.targetReps)
                            // 先完成AMRAP组，显示庆祝弹窗后再处理计时器导航
                        },
                        enabled = !isCompleted,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("完成 ${actualReps ?: set.targetReps} 次")
                    }
                } else {
                    // 普通组完成按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onSetComplete(set.targetReps)
                                // 前两组完成后自动开始75秒休息计时
                                onNavigateToTimer(75, true) // 前两组75秒，自动开始
                            },
                            enabled = !isCompleted,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("完成 ${set.targetReps} 次")
                        }
                    }
                }
            }else if (!isEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "请先完成前一组动作",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun getFeelingString(feeling: com.jmin.five3one.data.model.WorkoutFeeling): String {
    return when (feeling) {
        com.jmin.five3one.data.model.WorkoutFeeling.EXCELLENT -> stringResource(R.string.feeling_excellent)
        com.jmin.five3one.data.model.WorkoutFeeling.GOOD -> stringResource(R.string.feeling_good)
        com.jmin.five3one.data.model.WorkoutFeeling.FAIR -> stringResource(R.string.feeling_fair)
        com.jmin.five3one.data.model.WorkoutFeeling.POOR -> stringResource(R.string.feeling_poor)
    }
}

private fun formatDuration(milliseconds: Long): String {
    val seconds = (milliseconds / 1000) % 60
    val minutes = (milliseconds / (1000 * 60)) % 60
    val hours = (milliseconds / (1000 * 60 * 60))
    
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}

@Composable
private fun AmrapCelebrationDialog(
    celebrationData: AmrapCelebrationData,
    onDismiss: () -> Unit,
    onViewProgress: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 标题和emoji
                Text(
                    text = "🎉",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = stringResource(R.string.amrap_celebration_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // 即时鼓励
                Text(
                    text = stringResource(R.string.amrap_celebration_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // 数据确认卡片
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.amrap_data_confirmation),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        // 重量显示
                        Text(
                            text = "恭喜！你刚刚用 ${celebrationData.weight}kg 完成了 ${celebrationData.actualReps} 次重复。",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.amrap_target_display, celebrationData.targetReps),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = stringResource(R.string.amrap_actual_display, celebrationData.actualReps),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        if (celebrationData.exceededBy > 0) {
                            Text(
                                text = stringResource(R.string.amrap_exceeded_target, celebrationData.exceededBy),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        
                        Text(
                            text = stringResource(R.string.amrap_form_reminder),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                
                // 解释说明部分
                Column(
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.amrap_explanation_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = stringResource(R.string.amrap_explanation_tm),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Text(
                        text = stringResource(R.string.amrap_explanation_progress),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = stringResource(R.string.amrap_next_action_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = stringResource(R.string.amrap_next_action_continue),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Text(
                        text = stringResource(R.string.amrap_next_action_automatic),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Text(
                        text = stringResource(R.string.amrap_next_action_pr),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // 行动按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onViewProgress,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.amrap_view_progress))
                    }
                    
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.amrap_save_continue))
                    }
                }
            }
        }
    }
}

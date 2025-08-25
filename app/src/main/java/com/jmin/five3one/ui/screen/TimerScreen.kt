package com.jmin.five3one.ui.screen

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jmin.five3one.R
import com.jmin.five3one.ui.viewmodel.TimerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    onNavigateBack: () -> Unit,
    initialRestTime: Int = 90,
    autoStart: Boolean = false,
    viewModel: TimerViewModel = hiltViewModel()
) {
    val timerState by viewModel.timerState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val defaultRestTime by viewModel.defaultRestTime.collectAsState()
    
    // 初始化计时器设置
    LaunchedEffect(initialRestTime) {
        viewModel.setInitialTime(initialRestTime)
        if (autoStart) {
            viewModel.startTimer()
        }
    }
    
    // 处理计时器完成对话框
    if (uiState.showCompletionDialog) {
        TimerCompletionDialog(
            onDismiss = { viewModel.acknowledgeTimerComplete() }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.training_timer_title)) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ){
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 计时器显示
                TimerDisplayCard(
                    timerState = timerState,
                    onToggleTimer = viewModel::toggleTimer,
                    onStopTimer = viewModel::stopTimer
                )
                // 快速时间选择
                QuickTimerCard(
                    onTimeSelect = viewModel::setPresetTime,
                    isRunning = timerState.isRunning
                )

                // 手动时间调整
                ManualTimeAdjustCard(
                    currentTime = timerState.timeLeftSeconds,
                    onAddTime = viewModel::addTime,
                    isRunning = timerState.isRunning
                )

                // 默认设置
                DefaultTimerSettingsCard(
                    defaultTime = defaultRestTime,
                    onUpdateDefault = viewModel::updateDefaultRestTime
                )

                // 错误消息
                uiState.errorMessage?.let { error ->
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
private fun TimerDisplayCard(
    timerState: com.jmin.five3one.data.model.TimerState,
    onToggleTimer: () -> Unit,
    onStopTimer: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (timerState.isRunning) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 圆形进度指示器
            Box(
                contentAlignment = Alignment.Center
            ) {
                val animatedProgress by animateFloatAsState(
                    targetValue = timerState.progressPercentage,
                    animationSpec = tween(
                        durationMillis = 800,
                        easing = LinearOutSlowInEasing
                    ),
                    label = "Timer Progress"
                )
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(200.dp)
                        .graphicsLayer {
                            compositingStrategy = CompositingStrategy.Offscreen
                        },
                    strokeWidth = 8.dp,
                    strokeCap = StrokeCap.Round,
                    color = if (timerState.isRunning) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                )

                Text(
                    text = timerState.formattedTime,
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (timerState.isRunning) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 控制按钮
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onToggleTimer,
                    modifier = Modifier.size(64.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = if (timerState.isRunning) {
                            Icons.Default.Pause
                        } else {
                            Icons.Default.PlayArrow
                        },
                        contentDescription = if (timerState.isRunning) "Pause" else "Start",
                        modifier = Modifier.size(32.dp)
                    )
                }

                OutlinedButton(
                    onClick = onStopTimer,
                    modifier = Modifier.size(64.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 状态文字
            Text(
                text = when {
                    timerState.isCompleted -> stringResource(R.string.timer_completed)
                    timerState.isRunning -> stringResource(R.string.timer_running)
                    else -> stringResource(R.string.timer_ready)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickTimerCard(
    onTimeSelect: (Int) -> Unit,
    isRunning: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.quick_timer_presets),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(120.dp)
            ) {
                val presetTimes = listOf(90, 120, 180, 240, 300, 360)
                items(presetTimes.size) { index ->
                    val time = presetTimes[index]
                    FilledTonalButton(
                        onClick = { onTimeSelect(time) },
                        enabled = !isRunning,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("${time / 60}:${String.format("%02d", time % 60)}")
                    }
                }
            }
        }
    }
}

@Composable
private fun ManualTimeAdjustCard(
    currentTime: Int,
    onAddTime: (Int) -> Unit,
    isRunning: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.manual_time_adjust),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onAddTime(-30) },
                    enabled = !isRunning && currentTime > 30,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("-30s")
                }
                
                OutlinedButton(
                    onClick = { onAddTime(-10) },
                    enabled = !isRunning && currentTime > 10,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("-10s")
                }
                
                OutlinedButton(
                    onClick = { onAddTime(10) },
                    enabled = !isRunning,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("+10s")
                }
                
                OutlinedButton(
                    onClick = { onAddTime(30) },
                    enabled = !isRunning,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("+30s")
                }
            }
        }
    }
}

@Composable
private fun DefaultTimerSettingsCard(
    defaultTime: Int,
    onUpdateDefault: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.default_timer_settings),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.default_rest_time),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    text = "${defaultTime / 60}:${String.format("%02d", defaultTime % 60)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun TimerCompletionDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.timer_completed_title))
        },
        text = {
            Text(stringResource(R.string.timer_completed_message))
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.ok))
            }
        },
        icon = {
            Icon(
                imageVector = Icons.Default.NotificationImportant,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    )
}

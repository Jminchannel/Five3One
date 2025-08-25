package com.jmin.five3one.ui.component

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
import androidx.compose.ui.window.Dialog
import com.jmin.five3one.R
import com.jmin.five3one.data.model.*

/**
 * 快速教程提示对话框
 * 在训练界面显示关键动作提示
 */
@Composable
fun QuickTutorialDialog(
    exerciseType: LiftType,
    tutorialContent: ExerciseTutorialContent?,
    onDismiss: () -> Unit,
    onViewFullTutorial: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // 标题
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tutorialContent?.title ?: getExerciseName(exerciseType),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (tutorialContent != null) {
                    // 关键要点（显示前3个）
                    Text(
                        text = stringResource(R.string.key_points),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    tutorialContent.keyPoints.take(3).forEach { point ->
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .align(Alignment.Top)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = point,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 心理提示（显示前2个）
                    if (tutorialContent.mentalCues.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.quick_tips),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        tutorialContent.mentalCues.take(2).forEach { cue ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                )
                            ) {
                                Text(
                                    text = "💡 $cue",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                } else {
                    // 如果没有教程数据，显示通用提示
                    Text(
                        text = stringResource(R.string.tutorial_not_available),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // 操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.close))
                    }
                    
                    Button(
                        onClick = onViewFullTutorial,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.full_guide))
                    }
                }
            }
        }
    }
}

/**
 * 教程进度指示器
 */
@Composable
fun TutorialProgressIndicator(
    exerciseType: LiftType,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isCompleted) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = stringResource(R.string.completed),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Surface(
                modifier = Modifier.size(20.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getExerciseIcon(exerciseType),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

/**
 * 教程快速入口卡片
 * 用于在设置完成后显示
 */
@Composable
fun TutorialPromptCard(
    onViewGuides: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.learn_proper_form),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(R.string.tutorial_setup_prompt),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onSkip,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text(stringResource(R.string.skip_guides))
                }
                
                Button(
                    onClick = onViewGuides,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text(stringResource(R.string.view_exercise_guides))
                }
            }
        }
    }
}

/**
 * 学习中心入口卡片
 * 用于导航页面或设置页面
 */
@Composable
fun LearningCenterCard(
    completedTutorials: Int,
    totalTutorials: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.learning_center),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = if (totalTutorials > 0) {
                        stringResource(R.string.tutorials_completed_format, completedTutorials, totalTutorials)
                    } else {
                        stringResource(R.string.master_the_movements)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 获取动作类型对应的图标
 */
private fun getExerciseIcon(exerciseType: LiftType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (exerciseType) {
        LiftType.SQUAT -> Icons.Default.FitnessCenter
        LiftType.BENCH_PRESS -> Icons.Default.FitnessCenter
        LiftType.DEADLIFT -> Icons.Default.FitnessCenter
        LiftType.OVERHEAD_PRESS -> Icons.Default.FitnessCenter
    }
}

/**
 * 获取动作类型的显示名称
 */
@Composable
private fun getExerciseName(exerciseType: LiftType): String {
    return when (exerciseType) {
        LiftType.SQUAT -> stringResource(R.string.squat)
        LiftType.BENCH_PRESS -> stringResource(R.string.bench_press)
        LiftType.DEADLIFT -> stringResource(R.string.deadlift)
        LiftType.OVERHEAD_PRESS -> stringResource(R.string.overhead_press)
    }
}

package com.jmin.five3one.ui.screen

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jmin.five3one.R
import com.jmin.five3one.data.model.*
import com.jmin.five3one.data.repository.LearningCenterOverview
import com.jmin.five3one.data.repository.TutorialOverviewItem
import com.jmin.five3one.ui.viewmodel.LearningCenterViewModel

/**
 * 学习中心/动作库页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningCenterScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTutorial: (LiftType) -> Unit,
    viewModel: LearningCenterViewModel = hiltViewModel()
) {
    val overview by viewModel.overview.collectAsState()
    val tutorialContents by viewModel.tutorialContents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadOverview()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.learning_center)) },
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
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 学习进度概览
                item {
                    LearningProgressCard(overview = overview)
                }
                
                // 推荐教程（下一个未完成的）
                overview.tutorials.find { !it.isCompleted }?.let { nextTutorial ->
                    item {
                        RecommendedTutorialCard(
                            tutorial = nextTutorial,
                            onClick = { onNavigateToTutorial(nextTutorial.exerciseType) }
                        )
                    }
                }
                
                // 教程列表标题
                item {
                    Text(
                        text = stringResource(R.string.all_tutorials),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                
                // 教程列表
                items(tutorialContents) { content ->
                    val tutorial = overview.tutorials.find { it.exerciseType == content.exerciseType }
                    TutorialListItem(
                        content = content,
                        isCompleted = tutorial?.isCompleted ?: false,
                        onClick = { onNavigateToTutorial(content.exerciseType) }
                    )
                }
                
                // 底部间距
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun LearningProgressCard(overview: LearningCenterOverview) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.learning_progress),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = stringResource(
                            R.string.tutorials_completed_format,
                            overview.completedTutorials,
                            overview.totalTutorials
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                
                // 圆形进度指示器
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { overview.progressPercentage / 100f },
                        modifier = Modifier.size(60.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 6.dp,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                    Text(
                        text = "${overview.progressPercentage.toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            if (overview.completedTutorials > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (overview.completedTutorials == overview.totalTutorials) {
                            stringResource(R.string.all_tutorials_completed)
                        } else {
                            stringResource(R.string.keep_going)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun RecommendedTutorialCard(
    tutorial: TutorialOverviewItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Recommend,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.recommended_next),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = tutorial.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = tutorial.shortDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.duration_minutes, tutorial.estimatedDurationMinutes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun TutorialListItem(
    content: ExerciseTutorialContent,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 动作图标
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = if (isCompleted) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getExerciseIcon(content.exerciseType),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (isCompleted) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = stringResource(R.string.completed),
                            modifier = Modifier
                                .size(16.dp)
                                .offset(x = 12.dp, y = (-12).dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 教程信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = content.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = content.shortDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // 目标肌群标签
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    content.targetMuscles.take(2).forEach { muscle ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = muscle,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                    
                    if (content.targetMuscles.size > 2) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "+${content.targetMuscles.size - 2}",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // 箭头图标
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 获取动作类型对应的图标
 */
@Composable
private fun getExerciseIcon(exerciseType: LiftType): ImageVector {
    return when (exerciseType) {
        LiftType.SQUAT -> Icons.Default.FitnessCenter
        LiftType.BENCH_PRESS -> Icons.Default.FitnessCenter
        LiftType.DEADLIFT -> Icons.Default.FitnessCenter
        LiftType.OVERHEAD_PRESS -> Icons.Default.FitnessCenter
    }
}

package com.jmin.five3one.ui.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jmin.five3one.R
import com.jmin.five3one.data.model.*
import com.jmin.five3one.data.repository.TutorialWithProgress
import com.jmin.five3one.ui.viewmodel.ExerciseTutorialViewModel

/**
 * 动作教程详情页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseTutorialScreen(
    exerciseType: LiftType,
    onNavigateBack: () -> Unit,
    viewModel: ExerciseTutorialViewModel = hiltViewModel()
) {
    val tutorialData by viewModel.tutorialData.collectAsState()
    val isCompleted by viewModel.isCompleted.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    LaunchedEffect(exerciseType) {
        viewModel.loadTutorial(exerciseType)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(tutorialData?.content?.title ?: stringResource(R.string.exercise_tutorial))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    if (tutorialData != null) {
                        IconButton(
                            onClick = {
                                if (isCompleted) {
                                    viewModel.markAsIncomplete()
                                } else {
                                    viewModel.markAsCompleted()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = if (isCompleted) stringResource(R.string.mark_incomplete) else stringResource(R.string.mark_completed),
                                tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
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
        } else if (tutorialData != null) {
            ExerciseTutorialContent(
                tutorialData = tutorialData!!,
                isCompleted = isCompleted,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun ExerciseTutorialContent(
    tutorialData: TutorialWithProgress,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 进度指示器
        if (isCompleted) {
            item {
                CompletionBanner()
            }
        }
        
        // 简短描述
        item {
            DescriptionCard(description = tutorialData.content.shortDescription)
        }
        
        // 视频/媒体区域（暂时用占位符）
        item {
            VideoPlaceholderCard(exerciseType = tutorialData.content.exerciseType)
        }
        
        // 关键要点
        item {
            KeyPointsCard(keyPoints = tutorialData.content.keyPoints)
        }
        
        // 心理提示
        item {
            MentalCuesCard(mentalCues = tutorialData.content.mentalCues)
        }
        
        // 目标肌群
        item {
            TargetMusclesCard(targetMuscles = tutorialData.content.targetMuscles)
        }
        
        // 常见错误
        item {
            CommonMistakesCard(commonMistakes = tutorialData.content.commonMistakes)
        }
        
        // 安全提示
        if (tutorialData.content.safetyTips.isNotEmpty()) {
            item {
                SafetyTipsCard(safetyTips = tutorialData.content.safetyTips)
            }
        }
        
        // FAQ
        if (tutorialData.content.faqItems.isNotEmpty()) {
            item {
                FAQCard(faqItems = tutorialData.content.faqItems)
            }
        }
        
        // 底部间距
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CompletionBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.tutorial_completed),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun DescriptionCard(description: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.overview),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun VideoPlaceholderCard(exerciseType: LiftType) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.demonstration_video),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            // 视频平台链接说明
            Text(
                text = stringResource(R.string.video_platforms_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 视频平台按钮
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 第一行：YouTube 和 哔哩哔哩
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // YouTube按钮
                    OutlinedButton(
                        onClick = {
                            openYouTubeSearch(context, exerciseType)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color.Red
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("YouTube")
                    }
                    
                    // 哔哩哔哩按钮
                    OutlinedButton(
                        onClick = {
                            openBilibiliSearch(context, exerciseType)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFF00A1D6) // 哔哩哔哩蓝色
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("哔哩哔哩")
                    }
                }
                
                // 第二行：TikTok
                OutlinedButton(
                    onClick = {
                        openTikTokSearch(context, exerciseType)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("TikTok")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 查看常见错误按钮
            OutlinedButton(
                onClick = { /* TODO: 打开常见错误视频 */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.common_mistakes_video))
            }
        }
    }
}

@Composable
private fun KeyPointsCard(keyPoints: List<String>) {
    TutorialSectionCard(
        title = stringResource(R.string.key_points),
        icon = Icons.Default.Star,
        items = keyPoints
    )
}

@Composable
private fun MentalCuesCard(mentalCues: List<String>) {
    TutorialSectionCard(
        title = stringResource(R.string.mental_cues),
        icon = Icons.Default.Psychology,
        items = mentalCues
    )
}

@Composable
private fun TargetMusclesCard(targetMuscles: List<String>) {
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
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.target_muscles),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 肌肉群标签
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                targetMuscles.chunked(2).forEach { row ->
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        row.forEach { muscle ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = muscle,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommonMistakesCard(commonMistakes: List<String>) {
    TutorialSectionCard(
        title = stringResource(R.string.common_mistakes),
        icon = Icons.Default.Warning,
        items = commonMistakes,
        itemColor = MaterialTheme.colorScheme.error
    )
}

@Composable
private fun SafetyTipsCard(safetyTips: List<String>) {
    TutorialSectionCard(
        title = stringResource(R.string.safety_tips),
        icon = Icons.Default.Security,
        items = safetyTips,
        itemColor = MaterialTheme.colorScheme.tertiary
    )
}

@Composable
private fun TutorialSectionCard(
    title: String,
    icon: ImageVector,
    items: List<String>,
    itemColor: Color = MaterialTheme.colorScheme.onSurface
) {
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
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(itemColor)
                            .align(Alignment.Top)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium,
                        color = itemColor,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                if (index < items.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun FAQCard(faqItems: List<FAQItem>) {
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
                    imageVector = Icons.Default.Help,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.frequently_asked_questions),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            faqItems.forEachIndexed { index, faq ->
                Column {
                    Text(
                        text = "Q: ${faq.question}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "A: ${faq.answer}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (index < faqItems.size - 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}

/**
 * 打开YouTube搜索
 */
private fun openYouTubeSearch(context: Context, exerciseType: LiftType) {
    val searchQuery = getExerciseSearchQuery(exerciseType)
    val youtubeUrl = "https://www.youtube.com/results?search_query=${searchQuery}+teaching"
    
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(youtubeUrl))
        context.startActivity(intent)
    } catch (e: Exception) {
        // 如果无法打开，可以显示错误信息或使用浏览器
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(youtubeUrl))
        context.startActivity(browserIntent)
    }
}

/**
 * 打开哔哩哔哩搜索
 */
private fun openBilibiliSearch(context: Context, exerciseType: LiftType) {
    val searchQuery = getExerciseSearchQuery(exerciseType)
    val bilibiliUrl = "https://search.bilibili.com/all?keyword=${searchQuery}+teaching"
    
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(bilibiliUrl))
        context.startActivity(intent)
    } catch (e: Exception) {
        // 如果无法打开，使用浏览器
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(bilibiliUrl))
        context.startActivity(browserIntent)
    }
}

/**
 * 打开TikTok搜索
 */
private fun openTikTokSearch(context: Context, exerciseType: LiftType) {
    val searchQuery = getExerciseSearchQuery(exerciseType)
    val tiktokUrl = "https://www.tiktok.com/search?q=${searchQuery}%20tutorial"
    
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(tiktokUrl))
        context.startActivity(intent)
    } catch (e: Exception) {
        // 如果无法打开，使用浏览器
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(tiktokUrl))
        context.startActivity(browserIntent)
    }
}

/**
 * 获取动作的搜索关键词
 */
private fun getExerciseSearchQuery(exerciseType: LiftType): String {
    return when (exerciseType) {
        LiftType.SQUAT -> "squat"
        LiftType.BENCH_PRESS -> "bench+press"
        LiftType.DEADLIFT -> "deadlift"
        LiftType.OVERHEAD_PRESS -> "overhead+press"
    }
}

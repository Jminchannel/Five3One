package com.jmin.five3one.ui.screen

import androidx.compose.foundation.clickable
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
import com.jmin.five3one.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSetup: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val userData by viewModel.userData.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
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
                // 用户信息卡片
                UserInfoCard(userData = userData)
            }
            
            item {
                // 训练设置
                TrainingSettingsCard(
                    userData = userData,
                    onNavigateToSetup = onNavigateToSetup,
                    onUpdateLanguage = viewModel::updateLanguage
                )
            }
            
            item {
                // 数据管理
                DataManagementCard(
                    onExportData = viewModel::exportData,
                    onResetData = viewModel::resetAllData
                )
            }
            
            item {
                // 关于应用
                AboutCard()
            }
            
            // 成功消息
            if (uiState.showExportSuccess) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.export_success),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
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
private fun UserInfoCard(
    userData: com.jmin.five3one.data.repository.UserData
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = stringResource(R.string.trainer),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Text(
                        text = stringResource(R.string.cycle_info, 1), // 简化处理，固定显示周期1
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 当前1RM显示
            Text(
                text = stringResource(R.string.current_1rm),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.lift_bench),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "${userData.oneRM.benchPress}kg",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Column {
                    Text(
                        text = stringResource(R.string.lift_squat),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "${userData.oneRM.squat}kg",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Column {
                    Text(
                        text = stringResource(R.string.lift_deadlift),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "${userData.oneRM.deadlift}kg",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Column {
                    Text(
                        text = stringResource(R.string.lift_press),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "${userData.oneRM.overheadPress}kg",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun TrainingSettingsCard(
    userData: com.jmin.five3one.data.repository.UserData,
    onNavigateToSetup: () -> Unit,
    onUpdateLanguage: (com.jmin.five3one.data.model.Language) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.training_settings),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 1RM设置
            SettingsItem(
                icon = Icons.Default.FitnessCenter,
                title = stringResource(R.string.rm_settings),
                subtitle = "调整你的最大单次重量",
                onClick = onNavigateToSetup
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 训练模板
            SettingsItem(
                icon = Icons.Default.Assignment,
                title = stringResource(R.string.template_settings),
                subtitle = "当前: ${userData.appSettings.currentTemplate.id.uppercase()}",
                onClick = onNavigateToSetup
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 杠铃片配置
            SettingsItem(
                icon = Icons.Default.Build,
                title = stringResource(R.string.plate_settings),
                subtitle = "杠铃杆: ${userData.plateConfig.barbellWeight}kg",
                onClick = onNavigateToSetup
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 语言设置
            LanguageSettingsItem(
                currentLanguage = userData.appSettings.language,
                onUpdateLanguage = onUpdateLanguage
            )
        }
    }
}

@Composable
private fun DataManagementCard(
    onExportData: () -> Unit,
    onResetData: () -> Unit
) {
    var showResetDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.data_management),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 导出数据
            SettingsItem(
                icon = Icons.Default.GetApp,
                title = stringResource(R.string.export_data),
                subtitle = "备份你的训练数据",
                onClick = onExportData
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 重置数据
            SettingsItem(
                icon = Icons.Default.RestartAlt,
                title = stringResource(R.string.reset_data),
                subtitle = "清除所有数据并重新开始",
                onClick = { showResetDialog = true },
                isDestructive = true
            )
        }
    }
    
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.reset_data_confirm_title)) },
            text = { Text(stringResource(R.string.reset_data_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetData()
                        showResetDialog = false
                    }
                ) {
                    Text(
                        text = stringResource(R.string.reset),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun AboutCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.about),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 应用版本
            SettingsItem(
                icon = Icons.Default.Info,
                title = stringResource(R.string.version),
                subtitle = "1.0.0",
                onClick = { }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 开发团队
            SettingsItem(
                icon = Icons.Default.Group,
                title = stringResource(R.string.developer),
                subtitle = stringResource(R.string.team_name),
                onClick = { }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 531训练法说明
            SettingsItem(
                icon = Icons.Default.Help,
                title = "关于531训练法",
                subtitle = "由Jim Wendler创建的经典力量训练方法",
                onClick = { }
            )
        }
    }
}

@Composable
private fun LanguageSettingsItem(
    currentLanguage: com.jmin.five3one.data.model.Language,
    onUpdateLanguage: (com.jmin.five3one.data.model.Language) -> Unit
) {
    var showLanguageDialog by remember { mutableStateOf(false) }
    
    SettingsItem(
        icon = Icons.Default.Language,
        title = stringResource(R.string.language_settings),
        subtitle = getLanguageDisplayName(currentLanguage),
        onClick = { showLanguageDialog = true }
    )
    
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.language_settings)) },
            text = {
                Column {
                    com.jmin.five3one.data.model.Language.values().forEach { language ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onUpdateLanguage(language)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = language == currentLanguage,
                                onClick = {
                                    onUpdateLanguage(language)
                                    showLanguageDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(getLanguageDisplayName(language))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDestructive) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
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
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (isDestructive) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = if (isDestructive) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDestructive) {
                        MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun getLanguageDisplayName(language: com.jmin.five3one.data.model.Language): String {
    return when (language) {
        com.jmin.five3one.data.model.Language.ENGLISH -> stringResource(R.string.language_english)
        com.jmin.five3one.data.model.Language.CHINESE_SIMPLIFIED -> stringResource(R.string.language_chinese_simplified)
        com.jmin.five3one.data.model.Language.CHINESE_TRADITIONAL -> stringResource(R.string.language_chinese_traditional)
        com.jmin.five3one.data.model.Language.INDONESIAN -> stringResource(R.string.language_indonesian)
    }
}

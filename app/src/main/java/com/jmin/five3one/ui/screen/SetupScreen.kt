package com.jmin.five3one.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jmin.five3one.R
import com.jmin.five3one.ui.viewmodel.SetupViewModel

/**
 * 设置流程页面
 * 引导用户完成初始设置：1RM、杠铃片配置、训练模板
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    onSetupComplete: () -> Unit,
    onNavigateBack: () -> Unit,
    initialStep: Int = 1,
    viewModel: SetupViewModel = hiltViewModel()
) {
    val setupState by viewModel.setupState.collectAsState()
    
    // 设置初始步骤
    LaunchedEffect(initialStep) {
        viewModel.setCurrentStep(initialStep)
    }
    
    // 监听设置完成
    LaunchedEffect(setupState.isCompleted) {
        if (setupState.isCompleted) {
            onSetupComplete()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (setupState.currentStep) {
                            1 -> stringResource(R.string.setup_1rm)
                            2 -> stringResource(R.string.setup_plates)
                            3 -> stringResource(R.string.setup_template)
                            else -> stringResource(R.string.setup_1rm)
                        }
                    )
                },
                navigationIcon = {
                    if (setupState.currentStep > 1) {
                        IconButton(onClick = { viewModel.previousStep() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    }
                },
                actions = {
                    Text(
                        text = stringResource(
                            R.string.setup_step_counter,
                            setupState.currentStep,
                            setupState.totalSteps
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 进度指示器
            LinearProgressIndicator(
                progress = { setupState.progressPercentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 设置步骤内容
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                when (setupState.currentStep) {
                    1 -> OneRMSetupStep(viewModel)
                    2 -> PlateConfigStep(viewModel)
                    3 -> TemplateSelectionStep(viewModel)
                    else -> OneRMSetupStep(viewModel)
                }
            }
            
            // 错误消息
            setupState.errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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

@Composable
private fun OneRMSetupStep(viewModel: SetupViewModel) {
    val oneRMInputs by viewModel.oneRMInputs.collectAsState()
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.setup_1rm_title),
            style = MaterialTheme.typography.headlineMedium
        )
        
        Text(
            text = stringResource(R.string.setup_1rm_description),
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 1RM输入表单
        OutlinedTextField(
            value = oneRMInputs.benchPress,
            onValueChange = viewModel::updateBenchPress,
            label = { Text(stringResource(R.string.lift_bench)) },
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = oneRMInputs.squat,
            onValueChange = viewModel::updateSquat,
            label = { Text(stringResource(R.string.lift_squat)) },
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = oneRMInputs.deadlift,
            onValueChange = viewModel::updateDeadlift,
            label = { Text(stringResource(R.string.lift_deadlift)) },
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = oneRMInputs.overheadPress,
            onValueChange = viewModel::updateOverheadPress,
            label = { Text(stringResource(R.string.lift_press)) },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = viewModel::save1RM,
            enabled = oneRMInputs.isValid(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.next))
        }
    }
}

@Composable
private fun PlateConfigStep(viewModel: SetupViewModel) {
    val plateConfigState by viewModel.plateConfigState.collectAsState()
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.plates_title),
            style = MaterialTheme.typography.headlineMedium
        )
        
        Text(
            text = stringResource(R.string.plates_description),
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 杠铃杆重量选择
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.barbell_weight),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BarbellWeightChip(
                        weight = 15.0,
                        isSelected = plateConfigState.barbellWeight == 15.0,
                        onClick = { viewModel.updateBarbellWeight(15.0) }
                    )
                    BarbellWeightChip(
                        weight = 20.0,
                        isSelected = plateConfigState.barbellWeight == 20.0,
                        onClick = { viewModel.updateBarbellWeight(20.0) }
                    )
                    BarbellWeightChip(
                        weight = 45.0,
                        isSelected = plateConfigState.barbellWeight == 45.0,
                        onClick = { viewModel.updateBarbellWeight(45.0) }
                    )
                }
            }
        }
        
        // 杠铃片选择
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.available_plates),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = stringResource(R.string.select_plates_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    items(plateConfigState.availablePlates.size) { index ->
                        val plate = plateConfigState.availablePlates[index]
                        PlateSelectionChip(
                            weight = plate,
                            isSelected = plateConfigState.selectedPlates.contains(plate),
                            onClick = { viewModel.togglePlate(plate) }
                        )
                    }
                }
            }
        }
        
        Button(
            onClick = viewModel::savePlateConfig,
            enabled = plateConfigState.selectedPlates.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.next))
        }
    }
}

@Composable
private fun TemplateSelectionStep(viewModel: SetupViewModel) {
    val setupState by viewModel.setupState.collectAsState()
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.template_title),
            style = MaterialTheme.typography.headlineMedium
        )
        
        Text(
            text = stringResource(R.string.template_description),
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 训练模板选择
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TemplateCard(
                templateType = com.jmin.five3one.data.model.TemplateType.FIVES,
                isSelected = setupState.selectedTemplate == com.jmin.five3one.data.model.TemplateType.FIVES,
                onClick = { viewModel.selectTemplate(com.jmin.five3one.data.model.TemplateType.FIVES) }
            )
            
            TemplateCard(
                templateType = com.jmin.five3one.data.model.TemplateType.THREES,
                isSelected = setupState.selectedTemplate == com.jmin.five3one.data.model.TemplateType.THREES,
                onClick = { viewModel.selectTemplate(com.jmin.five3one.data.model.TemplateType.THREES) }
            )
            
            TemplateCard(
                templateType = com.jmin.five3one.data.model.TemplateType.FIVE_THREE_ONE,
                isSelected = setupState.selectedTemplate == com.jmin.five3one.data.model.TemplateType.FIVE_THREE_ONE,
                onClick = { viewModel.selectTemplate(com.jmin.five3one.data.model.TemplateType.FIVE_THREE_ONE) }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = viewModel::completeSetup,
            enabled = setupState.selectedTemplate != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.complete_setup))
        }
    }
}

@Composable
private fun BarbellWeightChip(
    weight: Double,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        onClick = onClick,
        label = {
            Text(
                text = "${weight.toInt()}kg",
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        },
        selected = isSelected
    )
}

@Composable
private fun PlateSelectionChip(
    weight: Double,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        onClick = onClick,
        label = {
            Text(
                text = "${weight}kg",
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        },
        selected = isSelected,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun TemplateCard(
    templateType: com.jmin.five3one.data.model.TemplateType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            CardDefaults.outlinedCardBorder().copy(
                width = 2.dp,
                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)
            )
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
                Text(
                    text = getTemplateNameString(templateType),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = getTemplateDescriptionString(templateType),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 显示模板的重量百分比
            val template = com.jmin.five3one.data.model.WorkoutTemplate.getTemplate(templateType)
            Text(
                text = when (templateType) {
                    com.jmin.five3one.data.model.TemplateType.FIVES -> "第1周: 65%, 75%, 85%+"
                    com.jmin.five3one.data.model.TemplateType.THREES -> "第1周: 70%, 80%, 90%+"
                    com.jmin.five3one.data.model.TemplateType.FIVE_THREE_ONE -> "第1周: 75%, 85%, 95%+"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                }
            )
        }
    }
}

@Composable
private fun getTemplateNameString(templateType: com.jmin.five3one.data.model.TemplateType): String {
    return when (templateType) {
        com.jmin.five3one.data.model.TemplateType.FIVES -> stringResource(R.string.template_5s_name)
        com.jmin.five3one.data.model.TemplateType.THREES -> stringResource(R.string.template_3s_name)
        com.jmin.five3one.data.model.TemplateType.FIVE_THREE_ONE -> stringResource(R.string.template_531_name)
    }
}

@Composable
private fun getTemplateDescriptionString(templateType: com.jmin.five3one.data.model.TemplateType): String {
    return when (templateType) {
        com.jmin.five3one.data.model.TemplateType.FIVES -> stringResource(R.string.template_5s_description)
        com.jmin.five3one.data.model.TemplateType.THREES -> stringResource(R.string.template_3s_description)
        com.jmin.five3one.data.model.TemplateType.FIVE_THREE_ONE -> stringResource(R.string.template_531_description)
    }
}

package com.jmin.five3one.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jmin.five3one.R
import com.jmin.five3one.ui.viewmodel.PlateCalculatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlateCalculatorScreen(
    onNavigateBack: () -> Unit,
    viewModel: PlateCalculatorViewModel = hiltViewModel()
) {
    val calculatorState by viewModel.calculatorState.collectAsState()
    val currentSolution by viewModel.currentSolution.collectAsState()
    val plateConfig by viewModel.plateConfig.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.plate_calculator_title)) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 目标重量输入
            WeightInputCard(
                targetWeight = calculatorState.targetWeightInput,
                onWeightChange = viewModel::setTargetWeightFromString,
                onIncrease = { viewModel.increaseWeight() },
                onDecrease = { viewModel.decreaseWeight() }
            )
            
            // 快速重量选择
            QuickWeightSelectionCard(
                onWeightSelect = viewModel::selectFromAvailableWeights
            )
            
            // 配重结果
            currentSolution?.let { solution ->
                PlateResultCard(
                    solution = solution,
                    plateConfig = plateConfig
                )
            }
            
            // 当前杠铃片配置
            PlateConfigCard(plateConfig = plateConfig)
            
            // 错误消息
            calculatorState.errorMessage?.let { error ->
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

@Composable
private fun WeightInputCard(
    targetWeight: String,
    onWeightChange: (String) -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.target_weight),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDecrease) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease")
                }
                
                OutlinedTextField(
                    value = targetWeight,
                    onValueChange = onWeightChange,
                    label = { Text(stringResource(R.string.weight_kg)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                
                IconButton(onClick = onIncrease) {
                    Icon(Icons.Default.Add, contentDescription = "Increase")
                }
            }
        }
    }
}

@Composable
private fun QuickWeightSelectionCard(
    onWeightSelect: (Double) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.quick_weights),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(120.dp)
            ) {
                val commonWeights = listOf(60.0, 70.0, 80.0, 90.0, 100.0, 110.0, 120.0, 130.0)
                items(commonWeights.size) { index ->
                    val weight = commonWeights[index]
                    FilledTonalButton(
                        onClick = { onWeightSelect(weight) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("${weight.toInt()}")
                    }
                }
            }
        }
    }
}

@Composable
private fun PlateResultCard(
    solution: com.jmin.five3one.data.model.PlateSolution,
    plateConfig: com.jmin.five3one.data.model.PlateConfig
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (solution.isExactMatch) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            }
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
                    text = stringResource(R.string.recommended_solution),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            if (solution.isExactMatch) {
                                stringResource(R.string.exact_match)
                            } else {
                                stringResource(R.string.close_match)
                            }
                        )
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 总重量显示
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.total_weight),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${solution.actualWeight}kg",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // 误差显示
            if (!solution.isExactMatch) {
                Text(
                    text = if (solution.error > 0) {
                        "+${solution.error}kg"
                    } else {
                        "${solution.error}kg"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 杠铃分解
            Text(
                text = stringResource(R.string.barbell_breakdown),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "杠铃杆: ${plateConfig.barbellWeight}kg")
                Text(text = "每侧: ${solution.weightPerSide}kg")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 杠铃片显示
            if (solution.platesPerSide.isNotEmpty()) {
                Text(
                    text = "每侧杠铃片:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.height(80.dp)
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
}

@Composable
private fun PlateConfigCard(
    plateConfig: com.jmin.five3one.data.model.PlateConfig
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.current_plate_config),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "杠铃杆: ${plateConfig.barbellWeight}kg",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "可用杠铃片:",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.height(100.dp)
            ) {
                items(plateConfig.availablePlates.size) { index ->
                    val plate = plateConfig.availablePlates[index]
                    AssistChip(
                        onClick = { },
                        label = { Text("${plate}kg") }
                    )
                }
            }
        }
    }
}

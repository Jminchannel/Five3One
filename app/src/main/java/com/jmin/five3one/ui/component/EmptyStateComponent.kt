package com.jmin.five3one.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jmin.five3one.R

/**
 * 空状态组件
 */
@Composable
fun EmptyStateComponent(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionButton: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        actionButton?.let {
            Spacer(modifier = Modifier.height(24.dp))
            it()
        }
    }
}

/**
 * 预定义的空状态组件
 */
object EmptyStates {
    
    @Composable
    fun NoWorkoutHistory(
        onStartFirstWorkout: (() -> Unit)? = null
    ) {
        EmptyStateComponent(
            icon = Icons.Outlined.FitnessCenter,
            title = stringResource(R.string.no_workout_history),
            description = stringResource(R.string.no_workout_history_description),
            actionButton = onStartFirstWorkout?.let { action ->
                {
                    Button(onClick = action) {
                        Text(stringResource(R.string.start_first_workout))
                    }
                }
            }
        )
    }
    
    @Composable
    fun NoStatistics() {
        EmptyStateComponent(
            icon = Icons.Outlined.Analytics,
            title = stringResource(R.string.no_statistics),
            description = stringResource(R.string.no_statistics_description)
        )
    }
    
    @Composable
    fun NoData(
        icon: ImageVector = Icons.Outlined.DataUsage,
        title: String = stringResource(R.string.no_data),
        description: String = stringResource(R.string.no_data_description)
    ) {
        EmptyStateComponent(
            icon = icon,
            title = title,
            description = description
        )
    }
    
    @Composable
    fun LoadingError(
        onRetry: () -> Unit
    ) {
        EmptyStateComponent(
            icon = Icons.Outlined.ErrorOutline,
            title = stringResource(R.string.loading_error),
            description = stringResource(R.string.loading_error_description),
            actionButton = {
                OutlinedButton(onClick = onRetry) {
                    Text(stringResource(R.string.retry))
                }
            }
        )
    }
    
    @Composable
    fun NetworkError(
        onRetry: () -> Unit
    ) {
        EmptyStateComponent(
            icon = Icons.Outlined.CloudOff,
            title = stringResource(R.string.network_error),
            description = stringResource(R.string.network_error_description),
            actionButton = {
                OutlinedButton(onClick = onRetry) {
                    Text(stringResource(R.string.retry))
                }
            }
        )
    }
    
    @Composable
    fun MaintenanceMode() {
        EmptyStateComponent(
            icon = Icons.Outlined.Build,
            title = stringResource(R.string.maintenance_mode),
            description = stringResource(R.string.maintenance_mode_description)
        )
    }
    
    @Composable
    fun ComingSoon(
        featureName: String
    ) {
        EmptyStateComponent(
            icon = Icons.Outlined.Schedule,
            title = stringResource(R.string.coming_soon),
            description = stringResource(R.string.coming_soon_description, featureName)
        )
    }
}

/**
 * 加载状态组件
 */
@Composable
fun LoadingStateComponent(
    message: String = stringResource(R.string.loading),
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            strokeWidth = 4.dp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

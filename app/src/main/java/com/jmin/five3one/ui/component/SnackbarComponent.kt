package com.jmin.five3one.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jmin.five3one.R

/**
 * 自定义Snackbar类型
 */
enum class SnackbarType {
    Success,
    Error,
    Warning,
    Info
}

/**
 * 自定义Snackbar数据类
 */
data class CustomSnackbarData(
    val message: String,
    val type: SnackbarType = SnackbarType.Info,
    val actionLabel: String? = null,
    val duration: SnackbarDuration = SnackbarDuration.Short,
    val onAction: (() -> Unit)? = null,
    val onDismiss: (() -> Unit)? = null
)

/**
 * 自定义Snackbar组件
 */
@Composable
fun CustomSnackbar(
    snackbarData: SnackbarData,
    type: SnackbarType = SnackbarType.Info,
    modifier: Modifier = Modifier
) {
    val icon = when (type) {
        SnackbarType.Success -> Icons.Default.CheckCircle
        SnackbarType.Error -> Icons.Default.Error
        SnackbarType.Warning -> Icons.Default.Warning
        SnackbarType.Info -> Icons.Default.Info
    }
    
    val backgroundColor = when (type) {
        SnackbarType.Success -> MaterialTheme.colorScheme.primary
        SnackbarType.Error -> MaterialTheme.colorScheme.error
        SnackbarType.Warning -> MaterialTheme.colorScheme.tertiary
        SnackbarType.Info -> MaterialTheme.colorScheme.inverseSurface
    }
    
    val contentColor = when (type) {
        SnackbarType.Success -> MaterialTheme.colorScheme.onPrimary
        SnackbarType.Error -> MaterialTheme.colorScheme.onError
        SnackbarType.Warning -> MaterialTheme.colorScheme.onTertiary
        SnackbarType.Info -> MaterialTheme.colorScheme.inverseOnSurface
    }
    
    Snackbar(
        modifier = modifier.padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        containerColor = backgroundColor,
        contentColor = contentColor,
        action = snackbarData.visuals.actionLabel?.let { actionLabel ->
            {
                TextButton(
                    onClick = { snackbarData.performAction() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = contentColor
                    )
                ) {
                    Text(
                        text = actionLabel,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissAction = if (snackbarData.visuals.withDismissAction) {
            {
                IconButton(onClick = { snackbarData.dismiss() }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.dismiss),
                        tint = contentColor
                    )
                }
            }
        } else null
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = contentColor
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = snackbarData.visuals.message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Snackbar主机状态管理
 */
@Composable
fun rememberCustomSnackbarHostState(): SnackbarHostState {
    return remember { SnackbarHostState() }
}

/**
 * 显示自定义Snackbar的扩展函数
 */
suspend fun SnackbarHostState.showCustomSnackbar(
    data: CustomSnackbarData
): SnackbarResult {
    return showSnackbar(
        message = data.message,
        actionLabel = data.actionLabel,
        withDismissAction = data.onDismiss != null,
        duration = data.duration
    )
}

/**
 * 快速显示成功消息
 */
suspend fun SnackbarHostState.showSuccess(
    message: String,
    actionLabel: String? = null,
    duration: SnackbarDuration = SnackbarDuration.Short
): SnackbarResult {
    return showSnackbar(
        message = message,
        actionLabel = actionLabel,
        duration = duration
    )
}

/**
 * 快速显示错误消息
 */
suspend fun SnackbarHostState.showError(
    message: String,
    actionLabel: String? = null,
    duration: SnackbarDuration = SnackbarDuration.Long
): SnackbarResult {
    return showSnackbar(
        message = message,
        actionLabel = actionLabel,
        duration = duration
    )
}

/**
 * 自定义SnackbarHost
 */
@Composable
fun CustomSnackbarHost(
    hostState: SnackbarHostState,
    snackbarType: SnackbarType = SnackbarType.Info,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier
    ) { snackbarData ->
        CustomSnackbar(
            snackbarData = snackbarData,
            type = snackbarType
        )
    }
}

/**
 * 预定义的Snackbar消息
 */
object SnackbarMessages {
    
    @Composable
    fun WorkoutCompleted(): CustomSnackbarData {
        return CustomSnackbarData(
            message = stringResource(R.string.workout_completed_message),
            type = SnackbarType.Success,
            duration = SnackbarDuration.Short
        )
    }
    
    @Composable
    fun WorkoutSaved(): CustomSnackbarData {
        return CustomSnackbarData(
            message = stringResource(R.string.workout_saved_message),
            type = SnackbarType.Success,
            duration = SnackbarDuration.Short
        )
    }
    
    @Composable
    fun SettingsUpdated(): CustomSnackbarData {
        return CustomSnackbarData(
            message = stringResource(R.string.settings_updated_message),
            type = SnackbarType.Success,
            duration = SnackbarDuration.Short
        )
    }
    
    @Composable
    fun DataExported(): CustomSnackbarData {
        return CustomSnackbarData(
            message = stringResource(R.string.data_exported_message),
            type = SnackbarType.Success,
            duration = SnackbarDuration.Short
        )
    }
    
    @Composable
    fun NetworkError(): CustomSnackbarData {
        return CustomSnackbarData(
            message = stringResource(R.string.network_error_message),
            type = SnackbarType.Error,
            actionLabel = stringResource(R.string.retry),
            duration = SnackbarDuration.Long
        )
    }
    
    @Composable
    fun SaveError(): CustomSnackbarData {
        return CustomSnackbarData(
            message = stringResource(R.string.save_error_message),
            type = SnackbarType.Error,
            actionLabel = stringResource(R.string.retry),
            duration = SnackbarDuration.Long
        )
    }
    
    @Composable
    fun InvalidInput(): CustomSnackbarData {
        return CustomSnackbarData(
            message = stringResource(R.string.invalid_input_message),
            type = SnackbarType.Warning,
            duration = SnackbarDuration.Short
        )
    }
    
    @Composable
    fun FeatureComingSoon(): CustomSnackbarData {
        return CustomSnackbarData(
            message = stringResource(R.string.feature_coming_soon_message),
            type = SnackbarType.Info,
            duration = SnackbarDuration.Short
        )
    }
}

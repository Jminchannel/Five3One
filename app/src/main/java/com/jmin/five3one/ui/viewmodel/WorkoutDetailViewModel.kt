package com.jmin.five3one.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jmin.five3one.data.model.WorkoutHistory
import com.jmin.five3one.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutDetailViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<WorkoutDetailUiState>(WorkoutDetailUiState.Loading)
    val uiState: StateFlow<WorkoutDetailUiState> = _uiState.asStateFlow()
    
    fun loadWorkoutDetail(workoutId: Long) {
        viewModelScope.launch {
            _uiState.value = WorkoutDetailUiState.Loading
            
            try {
                val workout = workoutRepository.getWorkoutById(workoutId)
                if (workout != null) {
                    _uiState.value = WorkoutDetailUiState.Success(workout)
                } else {
                    _uiState.value = WorkoutDetailUiState.Error("训练记录不存在")
                }
            } catch (e: Exception) {
                _uiState.value = WorkoutDetailUiState.Error("加载训练记录失败: ${e.message}")
            }
        }
    }
}

sealed class WorkoutDetailUiState {
    object Loading : WorkoutDetailUiState()
    data class Success(val workout: WorkoutHistory) : WorkoutDetailUiState()
    data class Error(val message: String) : WorkoutDetailUiState()
}

package com.coworking.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coworking.api.SpaceDto
import com.coworking.data.local.entities.SpaceEntity
import com.coworking.data.repository.Result
import com.coworking.data.repository.SpaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class HomeUiState(
    val spaces: List<SpaceEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedFloor: Int? = null,
    val bookedSpaceIds: Set<Int> = emptySet()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val spaceRepository: SpaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            spaceRepository.observeSpaces().collect { spaces ->
                _uiState.update { it.copy(spaces = spaces) }
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val date = _uiState.value.selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            when (val result = spaceRepository.refreshSpaces(date = date, floor = _uiState.value.selectedFloor)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        refresh()
    }

    fun selectFloor(floor: Int?) {
        _uiState.update { it.copy(selectedFloor = floor) }
        refresh()
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}

package com.coworking.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coworking.api.SpaceCreateRequest
import com.coworking.data.local.entities.SpaceEntity
import com.coworking.data.repository.Result
import com.coworking.data.repository.SpaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUiState(
    val spaces: List<SpaceEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val spaceRepository: SpaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

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
            _uiState.update { it.copy(isLoading = true) }
            when (val r = spaceRepository.refreshSpaces()) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = r.message) }
            }
        }
    }

    fun addSpace(name: String, description: String, type: String, floor: Int, capacity: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val request = SpaceCreateRequest(name, description, type, floor, capacity)
            when (val r = spaceRepository.createSpace(request)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, successMessage = "Miejsce dodane pomyślnie") }
                    refresh()
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = r.message) }
            }
        }
    }

    fun deleteSpace(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val r = spaceRepository.deleteSpace(id)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, successMessage = "Miejsce usunięte") }
                    refresh()
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = r.message) }
            }
        }
    }

    fun clearMessages() = _uiState.update { it.copy(error = null, successMessage = null) }
}

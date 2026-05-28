package com.coworking.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coworking.data.repository.AuthRepository
import com.coworking.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val isLoggedIn = authRepository.accessTokenFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.login(email, password)) {
                is Result.Success -> _uiState.value = AuthUiState(isSuccess = true)
                is Result.Error -> _uiState.value = AuthUiState(error = result.message)
            }
        }
    }

    fun register(email: String, fullName: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.register(email, fullName, password)) {
                is Result.Success -> _uiState.value = AuthUiState(isSuccess = true)
                is Result.Error -> _uiState.value = AuthUiState(error = result.message)
            }
        }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

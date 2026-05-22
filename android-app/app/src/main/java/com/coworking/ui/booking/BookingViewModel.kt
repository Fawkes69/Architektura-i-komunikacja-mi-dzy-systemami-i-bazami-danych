package com.coworking.ui.booking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coworking.api.ReservationDto
import com.coworking.data.local.entities.ReservationEntity
import com.coworking.data.repository.ReservationRepository
import com.coworking.data.repository.Result
import com.coworking.data.repository.SpaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookingUiState(
    val reservations: List<ReservationEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val bookingSuccess: Boolean = false
)

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val spaceRepository: SpaceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            reservationRepository.observeReservations().collect { list ->
                _uiState.update { it.copy(reservations = list) }
            }
        }
        refreshReservations()
    }

    fun refreshReservations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val r = reservationRepository.refreshReservations()) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = r.message) }
            }
        }
    }

    fun createReservation(spaceId: Int, startTime: String, endTime: String, notes: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val r = reservationRepository.createReservation(spaceId, startTime, endTime, notes)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, bookingSuccess = true) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = r.message) }
            }
        }
    }

    fun cancelReservation(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val r = reservationRepository.cancelReservation(id)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = r.message) }
            }
        }
    }

    fun resetBookingSuccess() = _uiState.update { it.copy(bookingSuccess = false) }
    fun clearError() = _uiState.update { it.copy(error = null) }
}

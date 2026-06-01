package com.sanay3y.egy.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanay3y.egy.data.model.Request
import com.sanay3y.egy.data.model.RequestStatus
import com.sanay3y.egy.data.repository.RequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class RequestViewModel(
    private val repository: RequestRepository = RequestRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RequestUiState())
    val uiState: StateFlow<RequestUiState> = _uiState

    val numberFormatter: NumberFormat = NumberFormat.getNumberInstance(Locale.US)

    fun onNotesChange(newNotes: String) {
        _uiState.value = _uiState.value.copy(notes = newNotes)
    }

    fun onDateChange(newDate: String) {
        _uiState.value = _uiState.value.copy(selectedDate = newDate)
    }

    fun onTimeChange(newTime: String) {
        _uiState.value = _uiState.value.copy(selectedTime = newTime)
    }

    fun onLocationChange(newLocation: String) {
        _uiState.value = _uiState.value.copy(location = newLocation)
    }

    fun increaseFare() {
        _uiState.value = _uiState.value.copy(currentFare = _uiState.value.currentFare + 10)
    }

    fun decreaseFare() {
        if (_uiState.value.currentFare >= 10) {
            _uiState.value = _uiState.value.copy(currentFare = _uiState.value.currentFare - 10)
        }
    }
    fun createServiceRequest(userId: String, providerId: String, serviceType: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isSuccess = false)
            val currentState = _uiState.value

            val request = Request(
                userId = userId,
                providerId = providerId,
                description = currentState.notes,
                serviceType = serviceType,
                status = RequestStatus.PENDING.name,
                estimatedPrice = currentState.currentFare.toDouble(),
                date = "${currentState.selectedDate} ${currentState.selectedTime}",
                location = currentState.location,
                timestamp = System.currentTimeMillis()
            )

            val result = repository.createRequest(request)

            result.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = it.message ?: "Failed to create request"
                )
            }
        }
    }

    private fun handleResult(
        block: suspend () -> Result<List<Request>>,
        onSuccess: (List<Request>) -> RequestUiState
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = block()
            result.onSuccess { data ->
                _uiState.value = onSuccess(data).copy(isLoading = false, error = null)
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = it.message ?: "Something went wrong"
                )
            }
        }
    }

    fun loadActiveRequests(userId: String) {
        handleResult(
            block = { repository.getUserRequests(userId) },
            onSuccess = { requests ->
                _uiState.value.copy(
                    activeRequests = requests.filter { it.status != RequestStatus.COMPLETED_BY_CLIENT.name }
                )
            }
        )
    }

    fun loadCompletedRequests(userId: String) {
        handleResult(
            block = { repository.getUserRequests(userId) },
            onSuccess = { requests ->
                _uiState.value.copy(
                    completedRequests = requests.filter { it.status == RequestStatus.COMPLETED_BY_CLIENT.name }
                )
            }
        )
    }

    fun confirmJob(requestId: String) {
        viewModelScope.launch {
            repository.confirmByClient(requestId)
        }
    }
}
package com.sanay3y.egy.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanay3y.egy.data.model.Request
import com.sanay3y.egy.data.model.RequestStatus
import com.sanay3y.egy.data.repository.RequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant

class RequestViewModel(
    private val repository: RequestRepository = RequestRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RequestUiState())
    val uiState: StateFlow<RequestUiState> = _uiState

    // 🔥 helper
    private fun handleResult(
        block: suspend () -> Result<List<Request>>,
        onSuccess: (List<Request>) -> RequestUiState
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = block()

            result.onSuccess { data ->
                _uiState.value = onSuccess(data).copy(
                    isLoading = false,
                    error = null
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = it.message ?: "Something went wrong"
                )
            }
        }
    }

    // 🟢 Create request
    fun createRequest(
        userId: String,
        providerId: String,
        description: String,
        price: Double,
        lat: Double,
        lng: Double
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isSuccess = false)

            val request = Request(
                userId = userId,
                providerId = providerId,
                description = description,
                status = RequestStatus.PENDING.name,
                estimatedPrice = price,
                date = Instant.now().toString(),
                latitude = lat,
                longitude = lng
            )

            val result = repository.createRequest(request)

            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = it.message ?: "Failed to create request"
                )
            }
        }
    }

    // 🔄 Active (Tracking)
    fun loadActiveRequests(userId: String) {
        handleResult(
            block = { repository.getUserRequests(userId) },
            onSuccess = { requests ->
                _uiState.value.copy(
                    activeRequests = requests.filter {
                        it.status != RequestStatus.COMPLETED_BY_CLIENT.name
                    }
                )
            }
        )
    }

    // 📜 Completed
    fun loadCompletedRequests(userId: String) {
        handleResult(
            block = { repository.getUserRequests(userId) },
            onSuccess = { requests ->
                _uiState.value.copy(
                    completedRequests = requests.filter {
                        it.status == RequestStatus.COMPLETED_BY_CLIENT.name
                    }
                )
            }
        )
    }

    // 🟢 Confirm job
    fun confirmJob(requestId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true) // Start loading
            val result = repository.confirmByClient(requestId)

            result.onSuccess {
                // Get current state
                val currentState = _uiState.value

                // Find the request that was just confirmed
                val confirmedRequest = currentState.activeRequests.find { it.id == requestId }

                if (confirmedRequest != null) {
                    // Create the updated version of the request
                    val updatedRequest = confirmedRequest.copy(
                        status = RequestStatus.COMPLETED_BY_CLIENT.name,
                        clientConfirmed = true
                    )

                    // Update the UI State: Remove from Active, Add to Completed
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        activeRequests = currentState.activeRequests.filter { it.id != requestId },
                        completedRequests = currentState.completedRequests + updatedRequest,
                        error = null
                    )
                } else {
                    _uiState.value = currentState.copy(isLoading = false)
                }
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = it.message ?: "Failed to confirm job"
                )
            }
        }}
}
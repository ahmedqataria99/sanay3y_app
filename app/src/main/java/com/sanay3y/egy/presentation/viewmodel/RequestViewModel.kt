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
            val result = repository.confirmByClient(requestId)

            result.onSuccess {
                // ممكن تعمل reload هنا لو عايز
            }
        }
    }
}
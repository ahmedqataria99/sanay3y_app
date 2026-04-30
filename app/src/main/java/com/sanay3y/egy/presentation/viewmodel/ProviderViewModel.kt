package com.sanay3y.egy.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanay3y.egy.data.model.Request
import com.sanay3y.egy.data.repository.JobRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProviderViewModel(
    private val repository: JobRepository = JobRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProviderUiState())
    val uiState: StateFlow<ProviderUiState> = _uiState

    // 🔥 helper يقلل التكرار
    private fun handleRequest(
        block: suspend () -> List<Request>,
        onSuccess: (List<Request>) -> ProviderUiState
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = runCatching { block() }

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

    // 🟢 Available
    fun loadAvailableRequests() {
        handleRequest(
            block = { repository.getAvailableRequests() },
            onSuccess = { requests ->
                _uiState.value.copy(availableRequests = requests)
            }
        )
    }

    // 🟡 Accept
    fun acceptRequest(requestId: String, providerId: String) {
        viewModelScope.launch {
            repository.acceptRequest(requestId, providerId)
            loadAvailableRequests()
        }
    }

    // 🔵 Active
    fun loadActiveJobs(providerId: String) {
        handleRequest(
            block = { repository.getActiveJobs(providerId) },
            onSuccess = { jobs ->
                _uiState.value.copy(activeJobs = jobs)
            }
        )
    }

    // 🟢 Completed
    fun loadCompletedJobs(providerId: String) {
        handleRequest(
            block = { repository.getCompletedJobs(providerId) },
            onSuccess = { jobs ->
                _uiState.value.copy(completedJobs = jobs)
            }
        )
    }

    // 🔧 Start
    fun startJob(requestId: String) {
        viewModelScope.launch {
            repository.startJob(requestId)
        }
    }

    // 🔴 Complete
    fun completeJob(requestId: String) {
        viewModelScope.launch {
            repository.markCompletedByProvider(requestId)
        }
    }
}
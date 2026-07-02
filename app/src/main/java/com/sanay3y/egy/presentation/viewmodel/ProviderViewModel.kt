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

    private fun handleRequest(
        block: suspend () -> List<Request>,
        onSuccess: (List<Request>) -> ProviderUiState
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val data = block()
            _uiState.value = onSuccess(data).copy(isLoading = false, error = null)
        }
    }

    fun loadAvailableRequests(providerId: String) {
        handleRequest(
            block = { repository.getAvailableRequests(providerId) },
            onSuccess = { requests -> _uiState.value.copy(availableRequests = requests) }
        )
    }

    fun loadActiveJobs(providerId: String) {
        handleRequest(
            block = { repository.getActiveJobs(providerId) },
            onSuccess = { jobs -> _uiState.value.copy(activeJobs = jobs) }
        )
    }

    fun loadCompletedJobs(providerId: String) {
        handleRequest(
            block = { repository.getCompletedJobs(providerId) },
            onSuccess = { jobs -> _uiState.value.copy(completedJobs = jobs) }
        )
    }

    fun startJob(requestId: String) {
        viewModelScope.launch { repository.startJob(requestId) }
    }

    fun completeJob(requestId: String) {
        viewModelScope.launch { repository.markCompletedByProvider(requestId) }
    }
}
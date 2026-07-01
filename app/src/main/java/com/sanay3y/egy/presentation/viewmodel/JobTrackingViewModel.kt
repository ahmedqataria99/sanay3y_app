package com.sanay3y.egy.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanay3y.egy.data.model.Provider
import com.sanay3y.egy.data.model.Request
import com.sanay3y.egy.data.repository.ProviderRepository
import com.sanay3y.egy.data.repository.RequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class JobTrackingViewModel(
    private val repository: RequestRepository = RequestRepository(),
    private val providerRepository: ProviderRepository = ProviderRepository()
) : ViewModel() {

    private val _currentRequest = MutableStateFlow<Request?>(null)
    val currentRequest: StateFlow<Request?> = _currentRequest.asStateFlow()

    private val _provider = MutableStateFlow<Provider?>(null)
    val provider: StateFlow<Provider?> = _provider.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun observeRequest(requestId: String) {
        _isLoading.value = true
        repository.observeRequest(requestId) { request ->
            _currentRequest.value = request
            _isLoading.value = false
            
            // If we have a providerId, fetch provider details if not already loaded
            if (request.providerId.isNotBlank() && _provider.value?.id != request.providerId) {
                fetchProvider(request.providerId)
            }
        }
    }

    private fun fetchProvider(providerId: String) {
        viewModelScope.launch {
            providerRepository.getProviderById(providerId).onSuccess { provider ->
                _provider.value = provider
            }
        }
    }

    fun startJob(requestId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.startJob(requestId)
            _isLoading.value = false
        }
    }

    fun completeJob(requestId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.markCompletedByProvider(requestId)
            _isLoading.value = false
        }
    }

    fun confirmJob(requestId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.confirmByClient(requestId)
            _isLoading.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.stopObserving()
    }
}

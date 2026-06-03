package com.sanay3y.egy.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanay3y.egy.data.model.Provider
import com.sanay3y.egy.data.model.Request
import com.sanay3y.egy.data.repository.JobRepository
import com.sanay3y.egy.data.repository.ProviderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class JobTrackingViewModel(
    private val repository: JobRepository = JobRepository(),
    private val providerRepository: ProviderRepository = ProviderRepository() // ← أضف دا
) : ViewModel() {

    private val _currentRequest = MutableStateFlow<Request?>(null)
    val currentRequest: StateFlow<Request?> = _currentRequest

    private val _provider = MutableStateFlow<Provider?>(null) // ← أضف دا
    val provider: StateFlow<Provider?> = _provider

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun observeRequest(requestId: String) {
        repository.observeRequest(requestId) { request ->
            _currentRequest.value = request
            // لما الـ request يتحدث، جيب بيانات الـ provider ← أضف دا
            if (_provider.value == null && request.providerId.isNotBlank()) {
                loadProvider(request.providerId)
            }
        }
    }

    private fun loadProvider(providerId: String) { // ← أضف الفانكشن دي
        viewModelScope.launch {
            providerRepository.getProviderById(providerId)
                .onSuccess { _provider.value = it }
        }
    }

    fun confirmJob(requestId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.confirmByClient(requestId)
            _isLoading.value = false
        }
    }

    override fun onCleared() { // ← أضف دا عشان Memory Leak
        super.onCleared()
        repository.stopObserving()
    }
}
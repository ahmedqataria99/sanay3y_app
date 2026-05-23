package com.sanay3y.egy.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanay3y.egy.data.model.Request
import com.sanay3y.egy.data.repository.JobRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class JobTrackingViewModel(
    private val repository: JobRepository = JobRepository()
) : ViewModel() {

    private val _currentRequest = MutableStateFlow<Request?>(null)
    val currentRequest: StateFlow<Request?> = _currentRequest

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Real-time listener من Firebase
    fun observeRequest(requestId: String) {
        repository.observeRequest(requestId) { request ->
            _currentRequest.value = request
        }
    }

    // العميل يأكد إن الشغل خلص
    fun confirmJob(requestId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.confirmByClient(requestId)
            _isLoading.value = false
        }
    }
}
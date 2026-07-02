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
    private val userRepository: com.sanay3y.egy.data.repository.UserRepository = com.sanay3y.egy.data.repository.UserRepository(),
    private val providerRepository: ProviderRepository = ProviderRepository()
) : ViewModel() {

    private val _currentRequest = MutableStateFlow<Request?>(null)
    val currentRequest: StateFlow<Request?> = _currentRequest

    private val _otherPartyName = MutableStateFlow<String>("Loading...")
    val otherPartyName: StateFlow<String> = _otherPartyName

    private val _otherPartyPhone = MutableStateFlow<String>("")
    val otherPartyPhone: StateFlow<String> = _otherPartyPhone

    private val _otherPartyProvider = MutableStateFlow<Provider?>(null)
    val otherPartyProvider: StateFlow<Provider?> = _otherPartyProvider

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null

    // Real-time listener من Firebase
    fun observeRequest(requestId: String, currentUserId: String) {
        listenerRegistration?.remove()
        listenerRegistration = repository.observeRequest(requestId) { request ->
            _currentRequest.value = request
            
            // Load other party info
            val isProviderViewing = currentUserId == request.providerId
            val otherPartyId = if (isProviderViewing) request.userId else request.providerId
            
            if (otherPartyId.isNotBlank()) {
                viewModelScope.launch {
                    if (isProviderViewing) {
                        // Viewing Client info
                        userRepository.getUserByUid(otherPartyId).onSuccess { user ->
                            _otherPartyName.value = user?.name ?: "Customer"
                            _otherPartyPhone.value = user?.phone ?: ""
                        }
                    } else {
                        // Viewing Provider info
                        providerRepository.getProviderById(otherPartyId).onSuccess { provider ->
                            _otherPartyProvider.value = provider
                            _otherPartyName.value = provider?.name ?: "Expert Provider"
                            _otherPartyPhone.value = provider?.phone ?: ""
                        }
                    }
                }
            }
        }
    }

    // بدء الشغل
    fun startJob(requestId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.startJob(requestId).onFailure {
                _error.value = it.localizedMessage ?: "Failed to start job"
            }
            _isLoading.value = false
        }
    }

    // الصنايعي يخلص الشغل
    fun completeJob(requestId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.markCompletedByProvider(requestId).onFailure {
                _error.value = it.localizedMessage ?: "Failed to complete job"
            }
            _isLoading.value = false
        }
    }

    // العميل يأكد إن الشغل خلص
    fun confirmJob(requestId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.confirmByClient(requestId).onFailure {
                _error.value = it.localizedMessage ?: "Failed to confirm job"
            }
            _isLoading.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}
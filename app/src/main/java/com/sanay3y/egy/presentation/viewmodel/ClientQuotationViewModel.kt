package com.sanay3y.egy.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanay3y.egy.data.model.Request
import com.sanay3y.egy.data.repository.JobRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ClientQuotationUiState(
    val request: Request? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class ClientQuotationViewModel(
    private val repository: JobRepository = JobRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientQuotationUiState())
    val uiState: StateFlow<ClientQuotationUiState> = _uiState

    fun observeRequest(requestId: String) {
        repository.observeRequest(requestId) { request ->
            _uiState.value = _uiState.value.copy(request = request)
        }
    }

    fun acceptQuotation(requestId: String, onDone: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.acceptQuotation(requestId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onDone()
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = it.message ?: "Failed to accept quotation"
                    )
                }
        }
    }

    fun rejectQuotation(requestId: String, onDone: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.rejectQuotation(requestId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onDone()
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = it.message ?: "Failed to reject quotation"
                    )
                }
        }
    }
}
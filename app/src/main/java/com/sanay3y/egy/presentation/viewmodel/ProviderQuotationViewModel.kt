package com.sanay3y.egy.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanay3y.egy.data.model.Request
import com.sanay3y.egy.data.repository.JobRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProviderQuotationUiState(
    val request: Request? = null,
    val laborCost: String = "",
    val materialsCost: String = "",
    val isLoading: Boolean = false,
    val isSubmitted: Boolean = false,
    val error: String? = null
) {
    val totalPrice: Double
        get() = (laborCost.toDoubleOrNull() ?: 0.0) + (materialsCost.toDoubleOrNull() ?: 0.0)

    val isFormValid: Boolean
        get() = (laborCost.toDoubleOrNull() ?: 0.0) > 0.0 && materialsCost.toDoubleOrNull() != null
}

class ProviderQuotationViewModel(
    private val repository: JobRepository = JobRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProviderQuotationUiState())
    val uiState: StateFlow<ProviderQuotationUiState> = _uiState

    fun observeRequest(requestId: String) {
        repository.observeRequest(requestId) { request ->
            _uiState.value = _uiState.value.copy(request = request)
        }
    }

    fun onLaborCostChange(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
            _uiState.value = _uiState.value.copy(laborCost = value, error = null)
        }
    }

    fun onMaterialsCostChange(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
            _uiState.value = _uiState.value.copy(materialsCost = value, error = null)
        }
    }

    fun submitQuotation(requestId: String, providerId: String) {
        val state = _uiState.value
        if (!state.isFormValid) {
            _uiState.value = state.copy(error = "Please enter a valid labor cost.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repository.submitQuotation(
                requestId = requestId,
                providerId = providerId,
                laborCost = state.laborCost.toDoubleOrNull() ?: 0.0,
                materialsCost = state.materialsCost.toDoubleOrNull() ?: 0.0
            )
            result.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, isSubmitted = true)
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = it.message ?: "Failed to send quotation"
                )
            }
        }
    }
}
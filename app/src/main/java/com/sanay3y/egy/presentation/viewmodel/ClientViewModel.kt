package com.sanay3y.egy.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanay3y.egy.data.model.Provider
import com.sanay3y.egy.data.repository.ProviderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClientViewModel(
    private val repository: ProviderRepository = ProviderRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientUiState())
    val uiState = _uiState.asStateFlow()

    private val _selectedProvider = MutableStateFlow<Provider?>(null)
    val selectedProvider: StateFlow<Provider?> = _selectedProvider.asStateFlow()

    init {
        loadProviders()
    }

    private fun handleRequest(block: suspend () -> Result<List<Provider>>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = block()

            result.onSuccess { providers ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    providers = providers,
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

    fun loadProviders() {
        handleRequest { repository.getAllProviders() }
    }

    fun search(query: String) {
        if (query.isBlank()) {
            loadProviders()
            return
        }
        handleRequest { repository.searchProviders(query) }
    }

    fun filterByCategory(category: String) {
        handleRequest { repository.getProvidersByCategory(category) }
    }

    fun loadTopRated() {
        handleRequest { repository.getTopRatedProviders() }
    }

    fun selectProvider(provider: Provider) {
        _selectedProvider.value = provider
    }

    fun loadProvider(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getProviderById(id).onSuccess { provider ->
                _selectedProvider.value = provider
                _uiState.value = _uiState.value.copy(isLoading = false)
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = it.message ?: "Failed to load provider"
                )
            }
        }
    }

    // 🎯 NEW: Function for Option 3 (Direct Lookup)
    suspend fun getProviderById(id: String): Provider? {
        return repository.getProviderById(id).getOrNull()
    }
}

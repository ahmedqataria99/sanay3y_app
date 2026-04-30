package com.sanay3y.egy.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanay3y.egy.data.model.Provider
import com.sanay3y.egy.data.repository.ProviderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ClientViewModel(
    private val repository: ProviderRepository = ProviderRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientUiState())
    val uiState: StateFlow<ClientUiState> = _uiState

    init {
        loadProviders()
    }

    // 🔥 helper عشان نقلل التكرار
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

    // 🟢 Load all providers
    fun loadProviders() {
        handleRequest { repository.getAllProviders() }
    }

    // 🔍 Search
    fun search(query: String) {
        if (query.isBlank()) {
            loadProviders()
            return
        }

        handleRequest { repository.searchProviders(query) }
    }

    // 🧩 Filter
    fun filterByCategory(category: String) {
        handleRequest { repository.getProvidersByCategory(category) }
    }

    // ⭐ Top rated
    fun loadTopRated() {
        handleRequest { repository.getTopRatedProviders() }
    }
}
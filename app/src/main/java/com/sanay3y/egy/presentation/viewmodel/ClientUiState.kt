package com.sanay3y.egy.presentation.viewmodel

import com.sanay3y.egy.data.model.Provider

data class ClientUiState(
    val isLoading: Boolean = false,
    val providers: List<Provider> = emptyList(),
    val error: String? = null
)
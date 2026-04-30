package com.sanay3y.egy.presentation.viewmodel

import com.sanay3y.egy.data.model.Request

data class RequestUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val activeRequests: List<Request> = emptyList(),
    val completedRequests: List<Request> = emptyList(),
    val error: String? = null
)
package com.sanay3y.egy.presentation.viewmodel

import com.sanay3y.egy.data.model.Request

data class ProviderUiState(
    val isLoading: Boolean = false,
    val availableRequests: List<Request> = emptyList(),
    val activeJobs: List<Request> = emptyList(),
    val completedJobs: List<Request> = emptyList(),
    val error: String? = null
)
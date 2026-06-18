package com.sanay3y.egy.presentation.viewmodel

import com.sanay3y.egy.data.model.Request
import com.sanay3y.egy.data.model.User

data class ProviderUiState(
    val isLoading: Boolean = false,
    val availableRequests: List<Request> = emptyList(),
    val activeJobs: List<Request> = emptyList(),
    val completedJobs: List<Request> = emptyList(),
    val error: String? = null,
    // إضافة جديدة
    val selectedRequest: Request? = null, // بدل selectedJob
    val errorMessage: String? = null,
    val actionSuccess: String? = null,
    val clientUser: User? = null
)
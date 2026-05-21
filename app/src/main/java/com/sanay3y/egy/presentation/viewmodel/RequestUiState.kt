package com.sanay3y.egy.presentation.viewmodel

import com.sanay3y.egy.data.model.Request

data class RequestUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val activeRequests: List<Request> = emptyList(),
    val completedRequests: List<Request> = emptyList(),
    val error: String? = null,

    // 🔥 الحقول الجديدة الخاصة بشاشة عمل طلب جديد (ServiceRequestScreen)
    val notes: String = "",
    val selectedDate: String = "",
    val selectedTime: String = "",
    val location: String = "",
    val currentFare: Int = 150
) {
    // الـ Validation الـ ذكي: الزرار مش هيتفعل غير لما الأربعة دول يكونوا مليانين ومش فاضيين
    val isFormValid: Boolean
        get() = notes.isNotBlank() &&
                selectedDate.isNotBlank() &&
                selectedTime.isNotBlank() &&
                location.isNotBlank()
}
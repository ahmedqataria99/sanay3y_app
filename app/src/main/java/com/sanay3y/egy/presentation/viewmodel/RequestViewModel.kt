package com.sanay3y.egy.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanay3y.egy.data.model.Request
import com.sanay3y.egy.data.model.RequestStatus
import com.sanay3y.egy.data.repository.RequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.Instant
import java.util.Locale

class RequestViewModel(
    private val repository: RequestRepository = RequestRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RequestUiState())
    val uiState: StateFlow<RequestUiState> = _uiState

    // الـ NumberFormatter الموحد لعرض السعر في الشاشة بالشكل الصحيح
    val numberFormatter: NumberFormat = NumberFormat.getNumberInstance(Locale.US)

    // 🔥 1. فانكشنز تحديث الـ States الخاصة بـ Screen Form الجديد
    fun onNotesChange(newNotes: String) {
        _uiState.value = _uiState.value.copy(notes = newNotes)
    }

    fun onDateChange(newDate: String) {
        _uiState.value = _uiState.value.copy(selectedDate = newDate)
    }

    fun onTimeChange(newTime: String) {
        _uiState.value = _uiState.value.copy(selectedTime = newTime)
    }

    fun onLocationChange(newLocation: String) {
        _uiState.value = _uiState.value.copy(location = newLocation)
    }

    fun increaseFare() {
        _uiState.value = _uiState.value.copy(currentFare = _uiState.value.currentFare + 10)
    }

    fun decreaseFare() {
        if (_uiState.value.currentFare >= 10) {
            _uiState.value = _uiState.value.copy(currentFare = _uiState.value.currentFare - 10)
        }
    }

    // 🔥 2. تعديل ذكي: فانكشن لربط بيانات الفورم وضخها مباشرة في عملية الـ Create
    fun createServiceRequest(userId: String, providerId: String, lat: Double, lng: Double) {
        val currentState = _uiState.value
        createRequest(
            userId = userId,
            providerId = providerId,
            description = currentState.notes,       // سحبنا الوصف تلقائياً من الـ State
            price = currentState.currentFare.toDouble(), // سحبنا السعر المختار من الـ State
            lat = lat,
            lng = lng
        )
    }

    // 🔥 helper الأصلي بتاعك
    private fun handleResult(
        block: suspend () -> Result<List<Request>>,
        onSuccess: (List<Request>) -> RequestUiState
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = block()

            result.onSuccess { data ->
                _uiState.value = onSuccess(data).copy(
                    isLoading = false,
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

    // 🟢 Create request الأصلي بتاعك
    fun createRequest(
        userId: String,
        providerId: String,
        description: String,
        price: Double,
        lat: Double,
        lng: Double
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isSuccess = false)

            val request = Request(
                userId = userId,
                providerId = providerId,
                description = description,
                status = RequestStatus.PENDING.name,
                estimatedPrice = price,
                date = Instant.now().toString(),
                latitude = lat,
                longitude = lng
            )

            val result = repository.createRequest(request)

            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = it.message ?: "Failed to create request"
                )
            }
        }
    }

    // 🔄 Active (Tracking) الأصلي بتاعك
    fun loadActiveRequests(userId: String) {
        handleResult(
            block = { repository.getUserRequests(userId) },
            onSuccess = { requests ->
                _uiState.value.copy(
                    activeRequests = requests.filter {
                        it.status != RequestStatus.COMPLETED_BY_CLIENT.name
                    }
                )
            }
        )
    }

    // 📜 Completed الأصلي بتاعك
    fun loadCompletedRequests(userId: String) {
        handleResult(
            block = { repository.getUserRequests(userId) },
            onSuccess = { requests ->
                _uiState.value.copy(
                    completedRequests = requests.filter {
                        it.status == RequestStatus.COMPLETED_BY_CLIENT.name
                    }
                )
            }
        )
    }

    // 🟢 Confirm job الأصلي بتاعك
    fun confirmJob(requestId: String) {
        viewModelScope.launch {
            val result = repository.confirmByClient(requestId)

            result.onSuccess {
                // ممكن تعمل reload هنا لو عايز
            }
        }
    }
}
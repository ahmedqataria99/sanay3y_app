package com.sanay3y.egy.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanay3y.egy.data.model.User
import com.sanay3y.egy.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class ProfileViewModel(
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    val governorates = listOf("القاهرة")
    val districtsMap = mapOf(
        "القاهرة" to listOf(
            "شبرا", "مدينة نصر", "التجمع الخامس", "العاصمة الإدارية", "مصر الجديدة",
            "المعادي", "الزمالك", "وسط البلد", "المقطم", "الرحاب", "الشروق",
            "عين شمس", "حلوان", "المرج", "السلام", "النزهة", "بدر", "البساتين",
            "دار السلام", "حدائق القبة", "الزاوية الحمراء", "الزيتون", "روض الفرج", "الساحل"
        )
    )

    fun loadProfile(uid: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            userRepository.getUserByUid(uid).onSuccess { user ->
                _uiState.value = _uiState.value.copy(user = user, isLoading = false)
            }.onFailure {
                _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
            }
        }
    }

    fun updateLocation(governorate: String, district: String) {
        val user = _uiState.value.user ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, isSuccess = false)
            val updatedUser = user.copy(governorate = governorate, district = district)
            userRepository.updateUserProfile(updatedUser).onSuccess {
                _uiState.value = _uiState.value.copy(user = updatedUser, isUpdating = false, isSuccess = true)
            }.onFailure {
                _uiState.value = _uiState.value.copy(isUpdating = false, error = it.message)
            }
        }
    }
}

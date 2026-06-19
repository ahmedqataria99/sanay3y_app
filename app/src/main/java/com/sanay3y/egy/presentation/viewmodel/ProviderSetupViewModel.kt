package com.sanay3y.egy.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanay3y.egy.data.model.Provider
import com.sanay3y.egy.data.repository.ProviderRepository
import kotlinx.coroutines.launch

class ProviderSetupViewModel(
    private val repository: ProviderRepository = ProviderRepository()
) : ViewModel() {

    var name by mutableStateOf("")
    var phone by mutableStateOf("")
    var currentStep by mutableIntStateOf(0)
    var select by mutableStateOf("Plumbing")
    var price by mutableStateOf("")
    var address by mutableStateOf("")

    // حالات رفع البيانات
    var isLoading by mutableStateOf(false)
    var isSuccess by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun updateName(newName: String) { name = newName }
    fun updatePhone(newPhone: String) { phone = newPhone }
    fun nextStep() { currentStep++ }
    fun selectCategory(category: String) { select = category }
    fun updatePrice(newPrice: String) { price = newPrice }
    fun updateAddress(newAddress: String) { address = newAddress }

    // دالة حفظ البيانات والربط مع الـ Repository
    fun completeProviderSetup(uid: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val providerData = Provider(
                firebaseUid = uid,
                name = name,
                category = select,
                phone = phone,
                location = address,

                bio = "Hourly Price: $price EGP",
                isOnline = true
            )

            val result = repository.saveProviderProfile(providerData)
            isLoading = false
            result.onSuccess {
                isSuccess = true
            }.onFailure {
                errorMessage = it.message ?: "حدث خطأ أثناء حفظ البيانات"
            }
        }
    }
}
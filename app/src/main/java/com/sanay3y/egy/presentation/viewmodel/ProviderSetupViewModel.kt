package com.sanay3y.egy.presentation.viewmodel

import android.net.Uri
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

    var governorate by mutableStateOf("")

    var district by mutableStateOf("")

    // states for loading and status
    var isLoading by mutableStateOf(false)
    var isSuccess by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    // Properties for documents
    var profilePhotoUri by mutableStateOf<Uri?>(null)
        private set

    var nationalIdFrontUri by mutableStateOf<Uri?>(null)
        private set

    var nationalIdBackUri by mutableStateOf<Uri?>(null)
        private set

    var policeClearanceUri by mutableStateOf<Uri?>(null)
        private set


    fun updateName(newName: String) { name = newName }
    fun updatePhone(newPhone: String) { phone = newPhone }
    fun nextStep() { currentStep++ }
    fun selectCategory(category: String) { select = category }
    fun updatePrice(newPrice: String) { price = newPrice }

    fun updateGovernorate(newGovernorate: String) { governorate = newGovernorate }

    fun updateDistrict(newDistrict: String) { district = newDistrict }

    fun updateProfilePhoto(uri: Uri) {
        profilePhotoUri = uri
    }

    fun updateNationalIdFront(uri: Uri) {
        nationalIdFrontUri = uri
    }

    fun updateNationalIdBack(uri: Uri) {
        nationalIdBackUri = uri
    }

    fun updatePoliceClearance(uri: Uri) {
        policeClearanceUri = uri
    }


    fun completeProviderSetup(uid: String) {
        viewModelScope.launch {
            isLoading = true
            isSuccess = false
            errorMessage = null

            if (uid.isBlank()) {
                isLoading = false
                errorMessage = "User information is missing."
                return@launch
            }

            val providerData = Provider(
                id = uid,
                firebaseUid = uid,
                name = name,
                category = select,
                phone = phone,
                governorate = governorate,
                district = district,
                bio = "Hourly Price: $price EGP",
                experienceYears = 1,
                imageUrl = profilePhotoUri?.toString() ?: "",
                latitude = 0.0,
                longitude = 0.0,
                isOnline = true
            )

            val result = repository.saveProviderProfile(providerData)
            isLoading = false
            result.onSuccess {
                isSuccess = true
            }.onFailure {
                errorMessage = it.message ?: "An error occurred while saving data"
            }
        }
    }
}

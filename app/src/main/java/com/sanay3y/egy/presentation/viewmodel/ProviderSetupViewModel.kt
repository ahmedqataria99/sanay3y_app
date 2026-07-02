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
    var latitude by mutableStateOf(0.0)
        private set

    var longitude by mutableStateOf(0.0)
        private set

    val governorates = listOf(
        "القاهرة"
    )

    val districtsMap = mapOf(
        "القاهرة" to listOf(
            "شبرا",
            "مدينة نصر",
            "التجمع الخامس",
            "العاصمة الإدارية",
            "مصر الجديدة",
            "المعادي",
            "الزمالك",
            "وسط البلد",
            "المقطم",
            "الرحاب",
            "الشروق",
            "عين شمس",
            "حلوان",
            "المرج",
            "السلام",
            "النزهة",
            "بدر",
            "البساتين",
            "دار السلام",
            "حدائق القبة",
            "الزاوية الحمراء",
            "الزيتون",
            "روض الفرج",
            "الساحل"
        )
    )

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

    fun updateGovernorate(newGovernorate: String) {
        governorate = newGovernorate
        district = ""
    }

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

    fun updateLocation(
        lat: Double,
        lng: Double
    ) {
        latitude = lat
        longitude = lng
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
                latitude = latitude,
                longitude = longitude,
                isOnline = true
            )

            //jana (logs)
            val result = repository.saveProviderProfile(providerData)
            isLoading = false
            result.onSuccess {
                isSuccess = true
                android.util.Log.d("ProviderSetup", "Provider Saved Successfully")
            }.onFailure {
                errorMessage = it.message ?: "An error occurred while saving data"
                android.util.Log.e("ProviderSetup", errorMessage ?: "Unknown Error")
            }
        }
    }
}

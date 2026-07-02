package com.sanay3y.egy.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanay3y.egy.data.model.Provider
import com.sanay3y.egy.data.repository.ProviderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.Context
import com.sanay3y.egy.utils.DistanceCalculator
import com.sanay3y.egy.utils.LocationHelper


class ClientViewModel(
    private val repository: ProviderRepository = ProviderRepository(),
    private val userRepository: com.sanay3y.egy.data.repository.UserRepository = com.sanay3y.egy.data.repository.UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientUiState())
    val uiState = _uiState.asStateFlow()

    private val _selectedProvider = MutableStateFlow<Provider?>(null)
    val selectedProvider: StateFlow<Provider?> = _selectedProvider.asStateFlow()

    init {
        loadProviders()
    }


    private fun handleRequest(block: suspend () -> Result<List<Provider>>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = block()

            result.onSuccess { providers ->
                val sortedProviders = sortProviders(providers, _uiState.value.sortBy)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    providers = sortedProviders,
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

    fun setSortOption(option: SortOption) {
        _uiState.value = _uiState.value.copy(sortBy = option)
        _uiState.value = _uiState.value.copy(providers = sortProviders(_uiState.value.providers, option))
    }

    private fun sortProviders(providers: List<Provider>, option: SortOption): List<Provider> {
        return when (option) {
            SortOption.RATING -> providers.sortedByDescending { it.rating }
            SortOption.EXPERIENCE -> providers.sortedByDescending { it.experienceYears }
            SortOption.PRICE -> providers.sortedBy { it.hourlyPrice }
        }
    }

    fun loadProviders() {
        handleRequest { repository.getAllProviders() }
    }

    fun search(query: String) {
        if (query.isBlank()) {
            loadProviders()
            return
        }
        handleRequest { repository.searchAndFilterProviders(nameQuery = query) }
    }

    fun filterByCategory(category: String) {
        handleRequest { repository.searchAndFilterProviders(category = category) }
    }

    fun loadTopRated() {
        handleRequest { repository.searchAndFilterProviders(minRating = 4.0) }
    }

    fun selectProvider(provider: Provider) {
        _selectedProvider.value = provider
    }

    fun loadProvider(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getProviderById(id).onSuccess { provider ->
                _selectedProvider.value = provider
                _uiState.value = _uiState.value.copy(isLoading = false)
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = it.message ?: "Failed to load provider"
                )
            }
        }
    }


    suspend fun getProviderById(id: String): Provider? {
        return repository.getProviderById(id).getOrNull()
    }
    //jana
    fun loadNearbyProviders(context: Context, userId: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val locationHelper = LocationHelper(context)
            var location = locationHelper.getCurrentLocation()

            if (location == null && userId != null) {
                // Try fallback to profile location
                val user = userRepository.getUserByUid(userId).getOrNull()
                if (user != null && user.latitude != 0.0 && user.longitude != 0.0) {
                    location = com.sanay3y.egy.utils.UserLocation(user.latitude, user.longitude)
                }
            }

            val finalLocation = location

            if (finalLocation == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Could not get your location. Please enable GPS or set location in profile."
                )
                return@launch
            }

            val inServiceArea = DistanceCalculator.isInServiceArea(
                finalLocation.latitude, finalLocation.longitude
            )

            if (!inServiceArea) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isInServiceArea = false,
                    userLocation = finalLocation
                )
                return@launch
            }



            repository.getAllProviders().onSuccess { providers ->

                // ← مؤقتاً: لو الـ Provider معندوش إحداثيات نديله إحداثيات عشوائية جوه القاهرة
                val providersWithDistance = providers
                    .filter {
                        it.latitude != 0.0 &&
                                it.longitude != 0.0
                    }
                    .map { provider ->

                        val distance = DistanceCalculator.calculateDistance(
                            finalLocation.latitude,
                            finalLocation.longitude,
                            provider.latitude,
                            provider.longitude
                        )

                        ProviderWithDistance(
                            provider = provider,
                            distanceKm = distance,
                            formattedDistance = DistanceCalculator.formatDistance(distance)
                        )
                    }
                    .sortedBy { it.distanceKm }
                    .take(6)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    nearbyProviders = providersWithDistance,
                    userLocation = finalLocation,
                    isInServiceArea = true,
                    districtName = DistanceCalculator.getDistrictName(
                        finalLocation.latitude,
                        finalLocation.longitude
                    ) ?: "Cairo",
                    error = null
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = it.message
                )
            }
        }
    }

    fun filterByLocation(governorate: String, district: String) {
        handleRequest { repository.searchAndFilterProviders(governorate = governorate, district = district) }
    }

    fun updateLocationPermission(granted: Boolean) {
        _uiState.value = _uiState.value.copy(locationPermissionGranted = granted)
    }
}

private fun ClosedFloatingPointRange<Double>.random(): Double {
    return start + Math.random() * (endInclusive - start)
}
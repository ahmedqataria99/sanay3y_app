package com.sanay3y.egy.presentation.viewmodel

import com.sanay3y.egy.R
import com.sanay3y.egy.data.model.Provider
import com.sanay3y.egy.utils.UserLocation

data class ClientUiState(
    val isLoading: Boolean = false,
    val providers: List<Provider> = emptyList(),
    val error: String? = null,
    //jana
    val nearbyProviders: List<ProviderWithDistance> = emptyList(),
    val userLocation: UserLocation? = null,
    val isInServiceArea: Boolean = true,
    val locationPermissionGranted: Boolean = false,
    val districtName: String? = null,
    val sortBy: SortOption = SortOption.RATING
)

enum class SortOption(val labelRes: Int) {
    RATING(R.string.top_rated),
    EXPERIENCE(R.string.years_of_experience),
    PRICE(R.string.hourly_pricing)
}

data class ProviderWithDistance(
    val provider: Provider,
    val distanceKm: Double,
    val formattedDistance: String
)
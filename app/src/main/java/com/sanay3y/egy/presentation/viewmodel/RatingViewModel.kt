package com.sanay3y.egy.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanay3y.egy.data.model.Provider
import com.sanay3y.egy.data.model.Review
import com.sanay3y.egy.data.repository.ProviderRepository
import com.sanay3y.egy.data.repository.RequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RatingUiState(
    val selectedStars: Int = 0,
    val comment: String = "",
    val reviews: List<Review> = emptyList(),
    val submitted: Boolean = false,
    val provider: Provider? = null,
    val isLoading: Boolean = false
)

class RatingViewModel(
    private val requestRepository: RequestRepository = RequestRepository(),
    private val providerRepository: ProviderRepository = ProviderRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RatingUiState())
    val uiState: StateFlow<RatingUiState> = _uiState.asStateFlow()

    fun loadProvider(providerId: String) {
        viewModelScope.launch {
            providerRepository.getProviderById(providerId)
                .onSuccess { provider ->
                    _uiState.update { it.copy(provider = provider) }
                }
        }
    }

    fun onStarsChanged(stars: Int) {
        _uiState.update { it.copy(selectedStars = stars) }
    }

    fun onCommentChanged(newComment: String) {
        _uiState.update { it.copy(comment = newComment) }
    }

    fun submitFeedback(
        requestId: String,
        userId: String,
        providerId: String,
        onSuccess: () -> Unit
    ) {
        val currentState = _uiState.value
        if (currentState.selectedStars == 0) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val review = Review(
                requestId = requestId,
                userId = userId,
                providerId = providerId,
                rating = currentState.selectedStars,
                comment = currentState.comment.trim().ifEmpty {
                    "No comment provided."
                },
                timestamp = System.currentTimeMillis()
            )

            requestRepository.addReview(review)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            reviews = listOf(review) + state.reviews,
                            submitted = true,
                            selectedStars = 0,
                            comment = "",
                            isLoading = false
                        )
                    }
                    onSuccess()
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false
                        )
                    }
                }
        }
    }
}
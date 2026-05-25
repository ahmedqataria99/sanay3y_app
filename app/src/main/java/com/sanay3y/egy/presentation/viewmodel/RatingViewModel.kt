package com.sanay3y.egy.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.sanay3y.egy.data.model.Review // عملنا import للمودل بتاعك
import java.util.UUID

data class RatingUiState(
    val selectedStars: Int = 0,
    val comment: String = "",
    val reviews: List<Review> = emptyList(), // تبدأ فاضية زي ما طلبنا
    val submitted: Boolean = false
)

class RatingViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RatingUiState())
    val uiState: StateFlow<RatingUiState> = _uiState.asStateFlow()

    fun onStarsChanged(stars: Int) {
        _uiState.update { it.copy(selectedStars = stars) }
    }

    fun onCommentChanged(newComment: String) {
        _uiState.update { it.copy(comment = newComment) }
    }

    fun submitFeedback(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        if (currentState.selectedStars > 0) {
            // عملنا object جديد بناءً على الـ data class بتاعتك
            val newReview = Review(
                id = UUID.randomUUID().toString(), // توليد ID عشوائي فريد
                userId = "current_client_id",      // المفروض ييجي من الـ Auth بعدين
                providerId = "provider_ahmed",     // الـ provider الحالي
                rating = currentState.selectedStars,
                comment = currentState.comment.ifEmpty { "No comment" },
                timestamp = System.currentTimeMillis() // تسجيل الوقت الحالي بالملي ثانية
            )

            _uiState.update { state ->
                state.copy(
                    reviews = listOf(newReview) + state.reviews, // إضافة المراجعة أول القائمة
                    submitted = true,
                    selectedStars = 0,
                    comment = ""
                )
            }
            onSuccess()
        }
    }
}
package com.sanay3y.egy.presentation.screens.client

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.sanay3y.egy.data.model.Provider
import com.sanay3y.egy.data.model.Review
import com.sanay3y.egy.presentation.viewmodel.RatingViewModel
import com.sanay3y.egy.presentation.viewmodel.RatingUiState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RatingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel = mockk<RatingViewModel>(relaxed = true)
    private val uiStateFlow = MutableStateFlow(RatingUiState())

    @Before
    fun setup() {
        every { viewModel.uiState } returns uiStateFlow
    }

    @Test
    fun whenScreenLoads_showsInitialViewsAndLoading() {
        uiStateFlow.value = RatingUiState(provider = null, selectedStars = 0)

        composeTestRule.setContent {
            RatingScreen(
                requestId = "req_123",
                userId = "user_123",
                providerId = "prov_123",
                onBackToHome = {},
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Feedback").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rate Service").assertIsDisplayed()
    }

    @Test
    fun whenProviderLoaded_displaysProviderNameAndCategory() {
        val fakeProvider = Provider(id = "prov_123", name = "Ahmed Sami", category = "Plumber")
        uiStateFlow.value = RatingUiState(provider = fakeProvider)

        composeTestRule.setContent {
            RatingScreen(
                requestId = "req_123",
                userId = "user_123",
                providerId = "prov_123",
                onBackToHome = {},
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Ahmed Sami").assertIsDisplayed()
        composeTestRule.onNodeWithText("Plumber").assertIsDisplayed()
    }

    @Test
    fun whenStarsSelected_displaysCorrectLabelAndEnablesButton() {
        uiStateFlow.value = RatingUiState(selectedStars = 4)

        composeTestRule.setContent {
            RatingScreen(
                requestId = "req_123",
                userId = "user_123",
                providerId = "prov_123",
                onBackToHome = {},
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Very Good").assertIsDisplayed()
        composeTestRule.onNodeWithText("Submit Feedback").assertIsEnabled()
    }

    @Test
    fun whenCommentEntered_triggersViewModelOnCommentChanged() {
        composeTestRule.setContent {
            RatingScreen(
                requestId = "req_123",
                userId = "user_123",
                providerId = "prov_123",
                onBackToHome = {},
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Tell us about your experience").performTextInput("Excellent work!")

        verify { viewModel.onCommentChanged("Excellent work!") }
    }

    @Test
    fun whenSubmitFeedbackClicked_triggersViewModelSubmission() {
        uiStateFlow.value = RatingUiState(selectedStars = 5)

        composeTestRule.setContent {
            RatingScreen(
                requestId = "req_123",
                userId = "user_123",
                providerId = "prov_123",
                onBackToHome = {},
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Submit Feedback").performClick()

        verify { viewModel.submitFeedback("req_123", "user_123", "prov_123", any()) }
    }

    @Test
    fun whenSubmissionSuccessful_showsSuccessCard() {
        uiStateFlow.value = RatingUiState(submitted = true)

        composeTestRule.setContent {
            RatingScreen(
                requestId = "req_123",
                userId = "user_123",
                providerId = "prov_123",
                onBackToHome = {},
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Thanks for your review!").assertIsDisplayed()
    }

    @Test
    fun whenReviewsExist_displaysReviewsList() {
        val fakeReviews = listOf(
            Review(id = "r1", rating = 5, comment = "Great job", timestamp = System.currentTimeMillis()),
            Review(id = "r2", rating = 4, comment = "Good experience", timestamp = System.currentTimeMillis())
        )
        uiStateFlow.value = RatingUiState(reviews = fakeReviews)

        composeTestRule.setContent {
            RatingScreen(
                requestId = "req_123",
                userId = "user_123",
                providerId = "prov_123",
                onBackToHome = {},
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("All Reviews (2)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Great job").assertIsDisplayed()
    }
}
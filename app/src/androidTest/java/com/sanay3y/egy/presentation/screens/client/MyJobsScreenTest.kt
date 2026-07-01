package com.sanay3y.egy.presentation.screens.client

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.sanay3y.egy.data.model.Provider
import com.sanay3y.egy.data.model.Request
import com.sanay3y.egy.data.model.RequestStatus
import com.sanay3y.egy.presentation.viewmodel.ClientViewModel
import com.sanay3y.egy.presentation.viewmodel.RequestUiState
import com.sanay3y.egy.presentation.viewmodel.RequestViewModel
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MyJobsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val clientViewModel = mockk<ClientViewModel>(relaxed = true)
    private val requestViewModel = mockk<RequestViewModel>(relaxed = true)
    private val uiStateFlow = MutableStateFlow(RequestUiState())

    @Before
    fun setup() {
        every { requestViewModel.uiState } returns uiStateFlow
    }

    @Test
    fun whenScreenLoads_showsTitleAndTabs() {
        composeTestRule.setContent {
            MyJobsScreen(
                userId = "user_123",
                clientViewModel = clientViewModel,
                requestViewModel = requestViewModel
            )
        }

        composeTestRule.onNodeWithText("My Jobs").assertIsDisplayed()
        composeTestRule.onNodeWithText("Active Jobs").assertIsDisplayed()
        composeTestRule.onNodeWithText("History").assertIsDisplayed()
    }

    @Test
    fun whenActiveRequestsEmpty_showsEmptyStateMessage() {
        uiStateFlow.value = RequestUiState(isLoading = false, activeRequests = emptyList())

        composeTestRule.setContent {
            MyJobsScreen(
                userId = "user_123",
                clientViewModel = clientViewModel,
                requestViewModel = requestViewModel
            )
        }

        composeTestRule.onNodeWithText("No Active Jobs").assertIsDisplayed()
        composeTestRule.onNodeWithText("You don't have any ongoing service requests at the moment.").assertIsDisplayed()
    }

    @Test
    fun whenJobNeedsAction_showsConfirmAndPayButton() {
        val fakeRequest = Request(
            id = "req_01",
            providerId = "prov_01",
            estimatedPrice = 450.0,
            status = RequestStatus.COMPLETED_BY_PROVIDER.name
        )
        uiStateFlow.value = RequestUiState(isLoading = false, activeRequests = listOf(fakeRequest))

        composeTestRule.setContent {
            MyJobsScreen(
                userId = "user_123",
                clientViewModel = clientViewModel,
                requestViewModel = requestViewModel
            )
        }

        composeTestRule.onNodeWithText("ACTION REQUIRED").assertIsDisplayed()
        composeTestRule.onNodeWithText("Confirm & Pay").assertIsDisplayed()
    }

    @Test
    fun whenConfirmAndPayClicked_triggersViewModelAction() {
        val fakeRequest = Request(
            id = "req_01",
            providerId = "prov_01",
            estimatedPrice = 450.0,
            status = RequestStatus.COMPLETED_BY_PROVIDER.name
        )
        uiStateFlow.value = RequestUiState(isLoading = false, activeRequests = listOf(fakeRequest))

        composeTestRule.setContent {
            MyJobsScreen(
                userId = "user_123",
                clientViewModel = clientViewModel,
                requestViewModel = requestViewModel
            )
        }

        composeTestRule.onNodeWithText("Confirm & Pay").performClick()

        verify { requestViewModel.confirmJob("req_01") }
    }
}
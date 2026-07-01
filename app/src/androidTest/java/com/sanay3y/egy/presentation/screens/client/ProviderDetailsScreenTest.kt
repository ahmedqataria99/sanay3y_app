package com.sanay3y.egy.presentation.screens.client

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.sanay3y.egy.data.model.Provider
import com.sanay3y.egy.presentation.viewmodel.ClientViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ProviderDetailsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel = mockk<ClientViewModel>(relaxed = true)
    private val providerFlow = MutableStateFlow<Provider?>(null)

    @Before
    fun setup() {
        every { viewModel.selectedProvider } returns providerFlow
    }

    @Test
    fun whenScreenLoadsWithProviderData_showsAllDetails() {
        val fakeProvider = Provider(
            id = "p_99",
            name = "Mohamed Ali",
            category = "Electrical",
            rating = 4.9,
            experienceYears = 8,
            bio = "Expert electrician with 8 years of experience."
        )
        providerFlow.value = fakeProvider

        composeTestRule.setContent {
            ProviderDetailsScreen(
                providerId = "p_99",
                viewModel = viewModel,
                onStartRequest = { _, _ -> },
                onNavigateBack = {}
            )
        }

        composeTestRule.onNodeWithText("Provider Details").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mohamed Ali").assertIsDisplayed()
        composeTestRule.onNodeWithText("Electrical").assertIsDisplayed()
        composeTestRule.onNodeWithText("4.9").assertIsDisplayed()
        composeTestRule.onNodeWithText("8 yrs exp").assertIsDisplayed()
        composeTestRule.onNodeWithText("Expert electrician with 8 years of experience.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Book Service Now").assertIsDisplayed()
    }

    @Test
    fun whenBackClicked_triggersNavigationCallback() {
        var isBackClicked = false

        composeTestRule.setContent {
            ProviderDetailsScreen(
                providerId = "p_99",
                viewModel = viewModel,
                onStartRequest = { _, _ -> },
                onNavigateBack = { isBackClicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        assert(isBackClicked)
    }

    @Test
    fun whenBookServiceClicked_triggersOnStartRequestWithCorrectData() {
        val fakeProvider = Provider(
            id = "p_99",
            name = "Mohamed Ali",
            category = "Electrical",
            rating = 4.9,
            experienceYears = 8,
            bio = "Expert electrician."
        )
        providerFlow.value = fakeProvider
        var receivedId = ""
        var receivedCategory = ""

        composeTestRule.setContent {
            ProviderDetailsScreen(
                providerId = "p_99",
                viewModel = viewModel,
                onStartRequest = { id, category ->
                    receivedId = id
                    receivedCategory = category
                },
                onNavigateBack = {}
            )
        }

        composeTestRule.onNodeWithText("Book Service Now").performClick()

        assertEquals("p_99", receivedId)
        assertEquals("Electrical", receivedCategory)
    }

    @Test
    fun whenLaunched_callsViewModelToLoadProvider() {
        composeTestRule.setContent {
            ProviderDetailsScreen(
                providerId = "p_99",
                viewModel = viewModel,
                onStartRequest = { _, _ -> },
                onNavigateBack = {}
            )
        }

        verify { viewModel.loadProvider("p_99") }
    }
}
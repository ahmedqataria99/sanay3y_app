package com.sanay3y.egy.presentation.screens.client

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

class RequestConfirmationScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val onNavigateToHome: () -> Unit = mockk(relaxed = true)
    private val onNavigateToJobs: () -> Unit = mockk(relaxed = true)

    @Test
    fun whenScreenLoads_displaysSuccessMessageAndButtons() {
        composeTestRule.setContent {
            RequestConfirmationScreen(
                onNavigateToHome = onNavigateToHome,
                onNavigateToJobs = onNavigateToJobs
            )
        }

        composeTestRule.onNodeWithText("Request Sent Successfully!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Your service request has been sent to the provider. You can track the status in your jobs list.").assertIsDisplayed()
        composeTestRule.onNodeWithText("View My Jobs").assertIsDisplayed()
        composeTestRule.onNodeWithText("Back to Home").assertIsDisplayed()
    }

    @Test
    fun whenViewMyJobsClicked_triggersNavigationToJobs() {
        composeTestRule.setContent {
            RequestConfirmationScreen(
                onNavigateToHome = onNavigateToHome,
                onNavigateToJobs = onNavigateToJobs
            )
        }

        composeTestRule.onNodeWithText("View My Jobs").performClick()

        verify { onNavigateToJobs() }
    }

    @Test
    fun whenBackToHomeClicked_triggersNavigationToHome() {
        composeTestRule.setContent {
            RequestConfirmationScreen(
                onNavigateToHome = onNavigateToHome,
                onNavigateToJobs = onNavigateToJobs
            )
        }

        composeTestRule.onNodeWithText("Back to Home").performClick()

        verify { onNavigateToHome() }
    }
}
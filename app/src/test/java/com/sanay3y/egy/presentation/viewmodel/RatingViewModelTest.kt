package com.sanay3y.egy.presentation.viewmodel

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.sanay3y.egy.data.model.Provider
import com.sanay3y.egy.data.model.Review
import com.sanay3y.egy.data.repository.ProviderRepository
import com.sanay3y.egy.data.repository.RequestRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RatingViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val requestRepository: RequestRepository = mockk()
    private val providerRepository: ProviderRepository = mockk()
    private lateinit var viewModel: RatingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = RatingViewModel(requestRepository, providerRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun loadProvider_success_updatesUiState() {
        val fakeProvider = Provider(id = "p123", name = "الأسطى صابر")
        coEvery { providerRepository.getProviderById("p123") } returns Result.success(fakeProvider)

        viewModel.loadProvider("p123")

        assertEquals(fakeProvider, viewModel.uiState.value.provider)
    }

    @Test
    fun onStarsChanged_updatesSelectedStars() {
        viewModel.onStarsChanged(4)

        assertEquals(4, viewModel.uiState.value.selectedStars)
    }

    @Test
    fun onCommentChanged_updatesComment() {
        viewModel.onCommentChanged("خدمة ممتازة وسريعة")

        assertEquals("خدمة ممتازة وسريعة", viewModel.uiState.value.comment)
    }

    @Test
    fun submitFeedback_zeroStars_returnsEarlyWithoutAction() {
        var isLambdaCalled = false

        viewModel.submitFeedback("req_1", "user_1", "p123") { isLambdaCalled = true }

        assertFalse(isLambdaCalled)
        coVerify(exactly = 0) { requestRepository.addReview(any()) }
    }

    @Test
    fun submitFeedback_success_updatesStateAndTriggersCallback() {
        var isLambdaCalled = false
        val reviewSlot = slot<Review>()
        coEvery { requestRepository.addReview(capture(reviewSlot)) } returns Result.success(Unit)

        viewModel.onStarsChanged(5)
        viewModel.onCommentChanged("الله ينور يا هندسة")
        viewModel.submitFeedback("req_1", "user_1", "p123") { isLambdaCalled = true }

        assertTrue(isLambdaCalled)
        assertTrue(viewModel.uiState.value.submitted)
        assertEquals(0, viewModel.uiState.value.selectedStars)
        assertEquals("", viewModel.uiState.value.comment)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(1, viewModel.uiState.value.reviews.size)
        assertEquals("الله ينور يا هندسة", reviewSlot.captured.comment)
    }

    @Test
    fun submitFeedback_emptyComment_usesDefaultFallbackText() {
        val reviewSlot = slot<Review>()
        coEvery { requestRepository.addReview(capture(reviewSlot)) } returns Result.success(Unit)

        viewModel.onStarsChanged(3)
        viewModel.onCommentChanged("   ")
        viewModel.submitFeedback("req_1", "user_1", "p123") {}

        assertEquals("No comment provided.", reviewSlot.captured.comment)
    }

    @Test
    fun submitFeedback_failure_resetsLoadingFlagAndKeepsInputs() {
        coEvery { requestRepository.addReview(any()) } returns Result.failure(Exception("Firestore Error"))

        viewModel.onStarsChanged(5)
        viewModel.onCommentChanged("تجربة سيئة")
        viewModel.submitFeedback("req_1", "user_1", "p123") {}

        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.submitted)
        assertEquals(5, viewModel.uiState.value.selectedStars)
        assertEquals("تجربة سيئة", viewModel.uiState.value.comment)
    }
}
package com.sanay3y.egy.presentation.viewmodel

import com.sanay3y.egy.data.model.Provider
import com.sanay3y.egy.data.model.Request
import com.sanay3y.egy.data.model.RequestStatus
import com.sanay3y.egy.data.model.User // تأكد من استيراد كائن الـ User الصحيح بمشروعك
import com.sanay3y.egy.data.repository.JobRepository
import com.sanay3y.egy.data.repository.ProviderRepository
import com.sanay3y.egy.data.repository.UserRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JobTrackingViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    // تجهيز الـ Mocks للـ Repositories الثلاثة المستخدمة
    private val jobRepository: JobRepository = mockk()
    private val userRepository: UserRepository = mockk()
    private val providerRepository: ProviderRepository = mockk()

    private lateinit var viewModel: JobTrackingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = JobTrackingViewModel(jobRepository, userRepository, providerRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // ================== TESTS: observeRequest ==================

    @Test
    fun observeRequest_asProvider_loadsClientInfoSuccessfully() {
        // Given
        val requestId = "req_123"
        val providerId = "provider_current"
        val clientId = "client_other"

        val callbackSlot = slot<(Request) -> Unit>()
        val fakeRequest = Request(id = requestId, userId = clientId, providerId = providerId)
        val fakeUser = User(name = "Ahmed Client", phone = "01000000000") // تعديل حسب الـ Model عندك

        every { jobRepository.observeRequest(requestId, capture(callbackSlot)) } returns mockk()
        coEvery { userRepository.getUserByUid(clientId) } returns Result.success(fakeUser)

        // When
        viewModel.observeRequest(requestId, providerId)
        callbackSlot.captured.invoke(fakeRequest) // محاكاة وصول تحديث من فايربيز

        // Then
        assertEquals(fakeRequest, viewModel.currentRequest.value)
        assertEquals("Ahmed Client", viewModel.otherPartyName.value)
        assertEquals("01000000000", viewModel.otherPartyPhone.value)
    }

    @Test
    fun observeRequest_asClient_loadsProviderInfoSuccessfully() {
        // Given
        val requestId = "req_123"
        val clientId = "client_current"
        val providerId = "provider_other"

        val callbackSlot = slot<(Request) -> Unit>()
        val fakeRequest = Request(id = requestId, userId = clientId, providerId = providerId)
        val fakeProvider = Provider(id = providerId, name = "Mohamed Plumber", phone = "01111111111", category = "Plumber")

        every { jobRepository.observeRequest(requestId, capture(callbackSlot)) } returns mockk()
        coEvery { providerRepository.getProviderById(providerId) } returns Result.success(fakeProvider)

        // When
        viewModel.observeRequest(requestId, clientId)
        callbackSlot.captured.invoke(fakeRequest)

        // Then
        assertEquals(fakeRequest, viewModel.currentRequest.value)
        assertEquals(fakeProvider, viewModel.otherPartyProvider.value)
        assertEquals("Mohamed Plumber", viewModel.otherPartyName.value)
        assertEquals("01111111111", viewModel.otherPartyPhone.value)
    }

    // ================== TESTS: startJob ==================

    @Test
    fun startJob_success_managesLoadingState() {
        // Given
        val requestId = "req_123"
        coEvery { jobRepository.startJob(requestId) } returns Result.success(Unit)

        // When
        viewModel.startJob(requestId)

        // Then
        coVerify(exactly = 1) { jobRepository.startJob(requestId) }
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun startJob_failure_updatesErrorState() {
        // Given
        val requestId = "req_123"
        val errorMessage = "Network Error"
        coEvery { jobRepository.startJob(requestId) } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.startJob(requestId)

        // Then
        coVerify(exactly = 1) { jobRepository.startJob(requestId) }
        assertFalse(viewModel.isLoading.value)
        assertEquals(errorMessage, viewModel.error.value)
    }

    // ================== TESTS: completeJob ==================

    @Test
    fun completeJob_success_managesLoadingState() {
        // Given
        val requestId = "req_123"
        coEvery { jobRepository.markCompletedByProvider(requestId) } returns Result.success(Unit)

        // When
        viewModel.completeJob(requestId)

        // Then
        coVerify(exactly = 1) { jobRepository.markCompletedByProvider(requestId) }
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun completeJob_failure_updatesErrorState() {
        // Given
        val requestId = "req_123"
        coEvery { jobRepository.markCompletedByProvider(requestId) } returns Result.failure(Exception("Failed to complete"))

        // When
        viewModel.completeJob(requestId)

        // Then
        assertFalse(viewModel.isLoading.value)
        assertEquals("Failed to complete", viewModel.error.value)
    }

    // ================== TESTS: confirmJob ==================

    @Test
    fun confirmJob_success_managesLoadingState() {
        // Given
        val requestId = "req_123"
        coEvery { jobRepository.confirmByClient(requestId) } returns Result.success(Unit)

        // When
        viewModel.confirmJob(requestId)

        // Then
        coVerify(exactly = 1) { jobRepository.confirmByClient(requestId) }
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun confirmJob_failure_updatesErrorState() {
        // Given
        val requestId = "req_123"
        coEvery { jobRepository.confirmByClient(requestId) } returns Result.failure(Exception("Confirmation error"))

        // When
        viewModel.confirmJob(requestId)

        // Then
        assertFalse(viewModel.isLoading.value)
        assertEquals("Confirmation error", viewModel.error.value)
    }

    // ================== TESTS: Initial States ==================

    @Test
    fun initialStates_areCorrectlySet() {
        assertNull(viewModel.currentRequest.value)
        assertEquals("Loading...", viewModel.otherPartyName.value)
        assertEquals("", viewModel.otherPartyPhone.value)
        assertNull(viewModel.otherPartyProvider.value)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
    }
}
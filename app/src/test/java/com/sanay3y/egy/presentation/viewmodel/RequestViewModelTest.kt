package com.sanay3y.egy.presentation.viewmodel

import com.sanay3y.egy.data.model.Request
import com.sanay3y.egy.data.model.RequestStatus
import com.sanay3y.egy.data.repository.RequestRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

// كلاس وهمي لبناء الـ State المتوقع داخل الـ Test إذا لم يكن متاحاً في نفس الـ Package
// (تأكد من مطابقة أسماء الحقول للـ RequestUiState الأصلي لديك)
data class RequestUiState(
    val notes: String = "",
    val selectedDate: String = "",
    val selectedTime: String = "",
    val location: String = "",
    val activeRequests: List<Request> = emptyList(),
    val completedRequests: List<Request> = emptyList(),
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
) {
    val isFormValid: Boolean get() = notes.isNotBlank() && selectedDate.isNotBlank() && selectedTime.isNotBlank() && location.isNotBlank()
}

@OptIn(ExperimentalCoroutinesApi::class)
class RequestViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val repository: RequestRepository = mockk()
    private lateinit var viewModel: RequestViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = RequestViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // ================== TESTS: Input Form Changes ==================

    @Test
    fun onFormChanges_updatesStateAndClearsError() {
        viewModel.onNotesChange("Fix plumbing leaking")
        viewModel.onDateChange("2026-07-05")
        viewModel.onTimeChange("14:00")
        viewModel.onLocationChange("Cairo, Nasr City")

        val state = viewModel.uiState.value
        assertEquals("Fix plumbing leaking", state.notes)
        assertEquals("2026-07-05", state.selectedDate)
        assertEquals("14:00", state.selectedTime)
        assertEquals("Cairo, Nasr City", state.location)
        assertNull(state.error)
    }

    @Test
    fun resetSuccessState_resetsFlagToFalse() {
        viewModel.resetSuccessState()
        assertFalse(viewModel.uiState.value.isSuccess)
    }

    // ================== TESTS: createServiceRequest ==================

    @Test
    fun createServiceRequest_invalidForm_setsValidationError() {
        // الـ Form فارغ حالياً
        viewModel.createServiceRequest("user_1", "provider_1", "Plumbing")

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertEquals("Please fill in all required fields.", state.error)
        coVerify(exactly = 0) { repository.createRequest(any()) }
    }

    @Test
    fun createServiceRequest_success_updatesStateToSuccess() {
        // Given
        viewModel.onNotesChange("Need urgent AC repair")
        viewModel.onDateChange("2026-07-10")
        viewModel.onTimeChange("10:00")
        viewModel.onLocationChange("Cairo, Shobra") // موقع نصي يدوي معتمد

        coEvery { repository.createRequest(any()) } returns Result.success(Unit)

        // When
        viewModel.createServiceRequest("user_1", "provider_1", "AC Repair")

        // Then
        coVerify(exactly = 1) { repository.createRequest(any()) }
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isSuccess)
        assertNull(state.error)
    }

    @Test
    fun createServiceRequest_failure_updatesStateWithError() {
        // Given
        viewModel.onNotesChange("Carpentry work")
        viewModel.onDateChange("2026-07-12")
        viewModel.onTimeChange("11:00")
        viewModel.onLocationChange("Maadi")

        val networkErrorMessage = "Firestore connection lost"
        coEvery { repository.createRequest(any()) } returns Result.failure(Exception(networkErrorMessage))

        // When
        viewModel.createServiceRequest("user_1", "provider_1", "Carpentry")

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertEquals(networkErrorMessage, state.error)
    }

    // ================== TESTS: loadRequests (Active & Completed) ==================

    @Test
    fun loadActiveRequests_filtersOutCompletedRequests() {
        // Given
        val userId = "user_1"
        val activeReq = Request(id = "req_active", status = RequestStatus.PENDING.name)
        val completedReq = Request(id = "req_done", status = RequestStatus.COMPLETED_BY_CLIENT.name)

        coEvery { repository.getUserRequests(userId) } returns Result.success(listOf(activeReq, completedReq))

        // When
        viewModel.loadActiveRequests(userId)

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.activeRequests.size)
        assertEquals("req_active", state.activeRequests[0].id)
        assertNull(state.error)
    }

    @Test
    fun loadCompletedRequests_filtersOnlyCompletedRequests() {
        // Given
        val userId = "user_1"
        val activeReq = Request(id = "req_active", status = RequestStatus.IN_PROGRESS.name)
        val completedReq = Request(id = "req_done", status = RequestStatus.COMPLETED_BY_CLIENT.name)

        coEvery { repository.getUserRequests(userId) } returns Result.success(listOf(activeReq, completedReq))

        // When
        viewModel.loadCompletedRequests(userId)

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.completedRequests.size)
        assertEquals("req_done", state.completedRequests[0].id)
        assertNull(state.error)
    }

    // ================== TESTS: confirmJob ==================

    @Test
    fun confirmJob_success_movesRequestFromActiveToCompleted() {
        // Given
        val requestId = "req_to_confirm"
        val initialActiveRequest = Request(id = requestId, status = RequestStatus.COMPLETED_BY_PROVIDER.name)

        // تجهيز الـ State بقائمة نشطة تحتوي الطلب مسبقاً قبل التأكيد
        coEvery { repository.getUserRequests("user_1") } returns Result.success(listOf(initialActiveRequest))
        viewModel.loadActiveRequests("user_1")

        coEvery { repository.confirmByClient(requestId) } returns Result.success(Unit)

        // When
        viewModel.confirmJob(requestId)

        // Then
        coVerify(exactly = 1) { repository.confirmByClient(requestId) }
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.activeRequests.none { it.id == requestId }) // تم حذفه من النشطة
        assertTrue(state.completedRequests.any { it.id == requestId }) // تم نقله للمكتملة

        val verifiedRequest = state.completedRequests.find { it.id == requestId }
        assertEquals(RequestStatus.COMPLETED_BY_CLIENT.name, verifiedRequest?.status)
        assertTrue(verifiedRequest?.clientConfirmed == true)
    }

    @Test
    fun confirmJob_failure_updatesErrorStateAndDoesNotMoveRequest() {
        // Given
        val requestId = "req_to_confirm"
        val initialActiveRequest = Request(id = requestId, status = RequestStatus.COMPLETED_BY_PROVIDER.name)

        coEvery { repository.getUserRequests("user_1") } returns Result.success(listOf(initialActiveRequest))
        viewModel.loadActiveRequests("user_1")

        coEvery { repository.confirmByClient(requestId) } returns Result.failure(Exception("Network timeout"))

        // When
        viewModel.confirmJob(requestId)

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Network timeout", state.error)
        assertEquals(1, state.activeRequests.size) // الطلب ما زال موجوداً في النشطة ولم ينتقل
        assertTrue(state.completedRequests.isEmpty())
    }
}
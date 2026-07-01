package com.sanay3y.egy.presentation.viewmodel

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.sanay3y.egy.data.model.Provider
import com.sanay3y.egy.data.model.Request
import com.sanay3y.egy.data.model.RequestStatus
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

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

    @Test
    fun onNotesChange_updatesStateCorrectly() {
        viewModel.onNotesChange("Test Notes")

        assertEquals("Test Notes", viewModel.uiState.value.notes)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun onDateChange_updatesStateCorrectly() {
        viewModel.onDateChange("2026-07-01")

        assertEquals("2026-07-01", viewModel.uiState.value.selectedDate)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun onTimeChange_updatesStateCorrectly() {
        viewModel.onTimeChange("10:00 AM")

        assertEquals("10:00 AM", viewModel.uiState.value.selectedTime)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun onLocationChange_updatesStateCorrectly() {
        viewModel.onLocationChange("Cairo, Egypt")

        assertEquals("Cairo, Egypt", viewModel.uiState.value.location)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun increaseFare_incrementsCurrentFareByTen() {
        val initialFare = viewModel.uiState.value.currentFare
        viewModel.increaseFare()

        assertEquals(initialFare + 10, viewModel.uiState.value.currentFare)
    }

    @Test
    fun decreaseFare_fareIsTenOrMore_decrementsByTen() {
        viewModel.increaseFare()
        viewModel.increaseFare()
        val currentFare = viewModel.uiState.value.currentFare

        viewModel.decreaseFare()

        assertEquals(currentFare - 10, viewModel.uiState.value.currentFare)
    }

    @Test
    fun decreaseFare_fareIsLessThanTen_doesNotDecrement() {
        while (viewModel.uiState.value.currentFare >= 10) {
            viewModel.decreaseFare()
        }
        val currentFare = viewModel.uiState.value.currentFare

        viewModel.decreaseFare()

        assertEquals(currentFare, viewModel.uiState.value.currentFare)
    }

    @Test
    fun createServiceRequest_invalidForm_setsErrorAndReturnsEarly() {
        viewModel.createServiceRequest("u1", "p1", "Plumbing")

        assertEquals("Please fill in all required fields.", viewModel.uiState.value.error)
        coVerify(exactly = 0) { repository.createRequest(any()) }
    }

    @Test
    fun createServiceRequest_success_updatesStateToSuccess() {
        viewModel.onNotesChange("Fix leak")
        viewModel.onDateChange("2026-07-01")
        viewModel.onTimeChange("12:00 PM")
        viewModel.onLocationChange("Giza")

        val requestSlot = slot<Request>()
        coEvery { repository.createRequest(capture(requestSlot)) } returns Result.success(Unit)

        viewModel.createServiceRequest("u123", "p456", "Plumbing")

        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.isSuccess)
        assertNull(viewModel.uiState.value.error)
        assertEquals("u123", requestSlot.captured.userId)
        assertEquals("p456", requestSlot.captured.providerId)
        assertEquals("Fix leak", requestSlot.captured.description)
        assertEquals("Plumbing", requestSlot.captured.serviceType)
        assertEquals(RequestStatus.PENDING.name, requestSlot.captured.status)
        assertEquals("2026-07-01 12:00 PM", requestSlot.captured.date)
        assertEquals("Giza", requestSlot.captured.location)
    }

    @Test
    fun createServiceRequest_failure_setsErrorMessage() {
        viewModel.onNotesChange("Fix leak")
        viewModel.onDateChange("2026-07-01")
        viewModel.onTimeChange("12:00 PM")
        viewModel.onLocationChange("Giza")

        coEvery { repository.createRequest(any()) } returns Result.failure(Exception("Database error"))

        viewModel.createServiceRequest("u123", "p456", "Plumbing")

        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isSuccess)
        assertEquals("Database error", viewModel.uiState.value.error)
    }

    @Test
    fun loadActiveRequests_success_filtersOutCompletedByClient() {
        val mixedRequests = listOf(
            Request(id = "1", status = RequestStatus.PENDING.name),
            Request(id = "2", status = RequestStatus.COMPLETED_BY_CLIENT.name),
            Request(id = "3", status = "IN_PROGRESS")
        )
        coEvery { repository.getUserRequests("u123") } returns Result.success(mixedRequests)

        viewModel.loadActiveRequests("u123")

        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.error)
        assertEquals(2, viewModel.uiState.value.activeRequests.size)
        assertEquals("1", viewModel.uiState.value.activeRequests[0].id)
        assertEquals("3", viewModel.uiState.value.activeRequests[1].id)
    }

    @Test
    fun loadActiveRequests_failure_setsErrorMessage() {
        coEvery { repository.getUserRequests("u123") } returns Result.failure(Exception("Network error"))

        viewModel.loadActiveRequests("u123")

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Network error", viewModel.uiState.value.error)
    }

    @Test
    fun loadCompletedRequests_success_filtersForCompletedByClientOnly() {
        val mixedRequests = listOf(
            Request(id = "1", status = RequestStatus.PENDING.name),
            Request(id = "2", status = RequestStatus.COMPLETED_BY_CLIENT.name)
        )
        coEvery { repository.getUserRequests("u123") } returns Result.success(mixedRequests)

        viewModel.loadCompletedRequests("u123")

        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.error)
        assertEquals(1, viewModel.uiState.value.completedRequests.size)
        assertEquals("2", viewModel.uiState.value.completedRequests[0].id)
    }

    @Test
    fun confirmJob_success_movesRequestFromActiveToCompleted() {
        val activeList = listOf(Request(id = "req_1", status = "IN_PROGRESS"))
        coEvery { repository.getUserRequests("u123") } returns Result.success(activeList)
        viewModel.loadActiveRequests("u123")

        coEvery { repository.confirmByClient("req_1") } returns Result.success(Unit)

        viewModel.confirmJob("req_1")

        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.activeRequests.isEmpty())
        assertEquals(1, viewModel.uiState.value.completedRequests.size)
        assertEquals("req_1", viewModel.uiState.value.completedRequests[0].id)
        assertEquals(RequestStatus.COMPLETED_BY_CLIENT.name, viewModel.uiState.value.completedRequests[0].status)
        assertTrue(viewModel.uiState.value.completedRequests[0].clientConfirmed)
    }

    @Test
    fun confirmJob_requestNotFound_justStopsLoading() {
        val activeList = listOf(Request(id = "req_1", status = "IN_PROGRESS"))
        coEvery { repository.getUserRequests("u123") } returns Result.success(activeList)
        viewModel.loadActiveRequests("u123")

        coEvery { repository.confirmByClient("req_different") } returns Result.success(Unit)

        viewModel.confirmJob("req_different")

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(1, viewModel.uiState.value.activeRequests.size)
        assertTrue(viewModel.uiState.value.completedRequests.isEmpty())
    }

    @Test
    fun confirmJob_failure_setsErrorMessage() {
        coEvery { repository.confirmByClient("req_1") } returns Result.failure(Exception("Confirmation failed"))

        viewModel.confirmJob("req_1")

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Confirmation failed", viewModel.uiState.value.error)
    }
}
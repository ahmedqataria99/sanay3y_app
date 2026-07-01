package com.sanay3y.egy.presentation.viewmodel

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.sanay3y.egy.data.model.Provider
import com.sanay3y.egy.data.model.Request
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

class ProviderViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val repository: RequestRepository = mockk()
    private lateinit var viewModel: ProviderViewModel

    private val fakeRequests = listOf(
        Request(id = "r1"),
        Request(id = "r2")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ProviderViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun loadAvailableRequests_success_updatesUiState() {
        coEvery { repository.getAvailableRequests() } returns Result.success(fakeRequests)

        viewModel.loadAvailableRequests()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(fakeRequests, viewModel.uiState.value.availableRequests)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun loadAvailableRequests_failure_setsErrorInUiState() {
        coEvery { repository.getAvailableRequests() } returns Result.failure(Exception("Network Error"))

        viewModel.loadAvailableRequests()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Network Error", viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.availableRequests.isEmpty())
    }

    @Test
    fun acceptRequest_success_triggersReloads() {
        coEvery { repository.acceptRequest("r1", "p123") } returns Result.success(Unit)
        coEvery { repository.getAvailableRequests() } returns Result.success(fakeRequests)
        coEvery { repository.getActiveJobs("p123") } returns Result.success(fakeRequests)

        viewModel.acceptRequest("r1", "p123")

        coVerify(exactly = 1) { repository.acceptRequest("r1", "p123") }
        coVerify(exactly = 1) { repository.getAvailableRequests() }
        coVerify(exactly = 1) { repository.getActiveJobs("p123") }
    }

    @Test
    fun acceptRequest_failure_setsErrorInUiState() {
        coEvery { repository.acceptRequest("r1", "p123") } returns Result.failure(Exception("Action Failed"))

        viewModel.acceptRequest("r1", "p123")

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Action Failed", viewModel.uiState.value.error)
    }

    @Test
    fun loadActiveJobs_success_updatesUiState() {
        coEvery { repository.getActiveJobs("p123") } returns Result.success(fakeRequests)

        viewModel.loadActiveJobs("p123")

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(fakeRequests, viewModel.uiState.value.activeJobs)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun loadActiveJobs_failure_setsErrorInUiState() {
        coEvery { repository.getActiveJobs("p123") } returns Result.failure(Exception("Fetch Failed"))

        viewModel.loadActiveJobs("p123")

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Fetch Failed", viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.activeJobs.isEmpty())
    }

    @Test
    fun loadCompletedJobs_success_updatesUiState() {
        coEvery { repository.getCompletedJobs("p123") } returns Result.success(fakeRequests)

        viewModel.loadCompletedJobs("p123")

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(fakeRequests, viewModel.uiState.value.completedJobs)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun loadCompletedJobs_failure_setsErrorInUiState() {
        coEvery { repository.getCompletedJobs("p123") } returns Result.failure(Exception("Failed to load completed"))

        viewModel.loadCompletedJobs("p123")

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Failed to load completed", viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.completedJobs.isEmpty())
    }

    @Test
    fun startJob_callsRepositoryMethod() {
        coEvery { repository.startJob("r1") } returns Result.success(Unit)

        viewModel.startJob("r1")

        coVerify(exactly = 1) { repository.startJob("r1") }
    }

    @Test
    fun completeJob_callsRepositoryMethod() {
        coEvery { repository.markCompletedByProvider("r1") } returns Result.success(Unit)

        viewModel.completeJob("r1")

        coVerify(exactly = 1) { repository.markCompletedByProvider("r1") }
    }
}
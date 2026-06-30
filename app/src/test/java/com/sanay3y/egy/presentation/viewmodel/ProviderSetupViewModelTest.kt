package com.sanay3y.egy.presentation.viewmodel

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.sanay3y.egy.data.model.Provider
import com.sanay3y.egy.data.repository.ProviderRepository
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

class ProviderSetupViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val repository: ProviderRepository = mockk()
    private lateinit var viewModel: ProviderSetupViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ProviderSetupViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun updateMethods_correctlyUpdateStateProperties() {
        viewModel.updateName("essam")
        viewModel.updatePhone("01112345678")
        viewModel.selectCategory("Electrical")
        viewModel.updatePrice("120")
        viewModel.updateAddress("alex")
        viewModel.nextStep()

        assertEquals("essam", viewModel.name)
        assertEquals("01112345678", viewModel.phone)
        assertEquals("Electrical", viewModel.select)
        assertEquals("120", viewModel.price)
        assertEquals("alex", viewModel.address)
        assertEquals(1, viewModel.currentStep)
    }

    @Test
    fun completeProviderSetup_blankUid_setsErrorAndReturns() {
        viewModel.completeProviderSetup("")

        assertFalse(viewModel.isLoading)
        assertFalse(viewModel.isSuccess)
        assertEquals("User information is missing.", viewModel.errorMessage)
        coVerify(exactly = 0) { repository.saveProviderProfile(any()) }
    }

    @Test
    fun completeProviderSetup_success_updatesStateToSuccess() {
        viewModel.updateName("mahmoud")
        viewModel.updatePhone("01200000000")
        viewModel.selectCategory("Plumbing")
        viewModel.updatePrice("200")
        viewModel.updateAddress("giza")

        val expectedProvider = Provider(
            id = "uid_123",
            firebaseUid = "uid_123",
            name = "mahmoud",
            category = "Plumbing",
            phone = "01200000000",
            location = "giza",
            bio = "Hourly Price: 200 EGP",
            experienceYears = 1,
            imageUrl = "",
            latitude = 0.0,
            longitude = 0.0,
            isOnline = true
        )

        coEvery { repository.saveProviderProfile(expectedProvider) } returns Result.success(Unit)

        viewModel.completeProviderSetup("uid_123")

        assertFalse(viewModel.isLoading)
        assertTrue(viewModel.isSuccess)
        assertNull(viewModel.errorMessage)
    }

    @Test
    fun completeProviderSetup_failure_setsErrorMessage() {
        val serverError = "Connection timeout"
        coEvery { repository.saveProviderProfile(any()) } returns Result.failure(Exception(serverError))

        viewModel.completeProviderSetup("uid_123")

        assertFalse(viewModel.isLoading)
        assertFalse(viewModel.isSuccess)
        assertEquals(serverError, viewModel.errorMessage)
    }
}

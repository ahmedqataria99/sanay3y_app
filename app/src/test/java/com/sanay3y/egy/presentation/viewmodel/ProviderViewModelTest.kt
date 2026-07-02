package com.sanay3y.egy.presentation.viewmodel

import android.net.Uri
import com.sanay3y.egy.data.model.Provider
import com.sanay3y.egy.data.repository.ProviderRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProviderSetupViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val repository: ProviderRepository = mockk()
    private lateinit var viewModel: ProviderSetupViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Uri::class) // عمل Mock للـ Android Uri ليعمل داخل الـ JVM
        viewModel = ProviderSetupViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun initialStates_areCorrect() {
        assertEquals("", viewModel.name)
        assertEquals("", viewModel.phone)
        assertEquals(0, viewModel.currentStep)
        assertEquals("Plumbing", viewModel.select)
        assertEquals("", viewModel.price)
        assertEquals("", viewModel.experienceYears)
        assertEquals("", viewModel.governorate)
        assertEquals("", viewModel.district)
        assertFalse(viewModel.isLoading)
        assertFalse(viewModel.isSuccess)
        assertNull(viewModel.errorMessage)
    }

    @Test
    fun updateMethods_correctlyUpdateStates() {
        viewModel.updateName("Mustafa")
        viewModel.updatePhone("01012345678")
        viewModel.selectCategory("Electrician")
        viewModel.updatePrice("250.0")
        viewModel.updateExperience("5")
        viewModel.updateGovernorate("القاهرة")
        viewModel.updateDistrict("شبرا")
        viewModel.nextStep()

        assertEquals("Mustafa", viewModel.name)
        assertEquals("01012345678", viewModel.phone)
        assertEquals("Electrician", viewModel.select)
        assertEquals("250.0", viewModel.price)
        assertEquals("5", viewModel.experienceYears)
        assertEquals("القاهرة", viewModel.governorate)
        assertEquals("شبرا", viewModel.district)
        assertEquals(1, viewModel.currentStep)
    }

    @Test
    fun completeProviderSetup_blankUid_setsErrorMessage() {
        viewModel.completeProviderSetup("")

        assertFalse(viewModel.isLoading)
        assertFalse(viewModel.isSuccess)
        assertEquals("User information is missing.", viewModel.errorMessage)
    }

    @Test
    fun completeProviderSetup_success_uploadsFilesAndSavesProfile() {
        // Given
        val uid = "user_abc_123"
        val mockUri = mockk<Uri>()
        val expectedUrl = "https://firebase.storage/photo.jpg"

        viewModel.updateName("Hassan")
        viewModel.updatePhone("0123456789")
        viewModel.updateGovernorate("القاهرة")
        viewModel.updateDistrict("مدينة نصر")
        viewModel.updateProfilePhoto(mockUri)

        // محاكاة رفع الملفات بنجاح من الـ Repository
        coEvery { repository.uploadFile(any(), any()) } returns expectedUrl
        coEvery { repository.saveProviderProfile(any()) } returns Result.success(Unit)

        // When
        viewModel.completeProviderSetup(uid)

        // Then
        coVerify(exactly = 1) { repository.uploadFile(mockUri, "providers/$uid/profile_photo.jpg") }
        coVerify(exactly = 1) { repository.saveProviderProfile(any()) }
        assertFalse(viewModel.isLoading)
        assertTrue(viewModel.isSuccess)
        assertNull(viewModel.errorMessage)
    }

    @Test
    fun completeProviderSetup_repositoryFailure_setsErrorMessage() {
        // Given
        val uid = "user_abc_123"
        coEvery { repository.saveProviderProfile(any()) } returns Result.failure(Exception("Firestore Error"))

        // When
        viewModel.completeProviderSetup(uid)

        // Then
        assertFalse(viewModel.isLoading)
        assertFalse(viewModel.isSuccess)
        assertEquals("Firestore Error", viewModel.errorMessage)
    }

    @Test
    fun completeProviderSetup_uploadThrowsException_catchesAndSetsError() {
        // Given
        val uid = "user_abc_123"
        val mockUri = mockk<Uri>()
        viewModel.updateProfilePhoto(mockUri)

        coEvery { repository.uploadFile(any(), any()) } throws RuntimeException("Storage Full")

        // When
        viewModel.completeProviderSetup(uid)

        // Then
        assertFalse(viewModel.isLoading)
        assertFalse(viewModel.isSuccess)
        assertEquals("Storage Full", viewModel.errorMessage)
    }
}
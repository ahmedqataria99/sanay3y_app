package com.sanay3y.egy.presentation.viewmodel

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.sanay3y.egy.data.model.Provider
import com.sanay3y.egy.data.model.Request
import com.sanay3y.egy.data.model.RequestStatus
import com.sanay3y.egy.data.repository.JobRepository
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
import kotlin.Result.Companion.success

class JobTrackingViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val repository: JobRepository = mockk()
    private lateinit var viewModel: JobTrackingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        // بنعمل نسخة من الـ ViewModel في البداية لأن ماعندوش دالة init بتكلم السيرفر فوراً
        viewModel = JobTrackingViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun observeRequest_firebaseEmitsNewData_updatesCurrentRequestFlow() {
        // Given: بنعمل Slot عشان نصطاد الكود (الـ lambda) اللي الـ ViewModel باعته للـ Repository
        val callbackSlot = slot<(Request) -> Unit>()
        val fakeRequest = Request(id = "req_999", status = RequestStatus.IN_PROGRESS.name)

        // بنفهم الموك إنه لما يتنادى عليه، يلقط الـ callback جوة الـ slot
        every { repository.observeRequest("req_999", capture(callbackSlot)) } returns Unit

        // When: بنشغل دالة المراقبة في الـ ViewModel
        viewModel.observeRequest("req_999")

        // هنا بنحاكي إن الفايربيز رجع داتا جديدة الآن، فبننادي الـ callback يدويًا بالطلب الوهمي
        callbackSlot.captured.invoke(fakeRequest)

        // Then: نتأكد إن الـ Flow استقبل الطلب الجديد بنجاح
        assertEquals(fakeRequest, viewModel.currentRequest.value)
    }

    @Test
    fun startJob_callsRepositoryAndManagesLoading() {
        coEvery { repository.startJob("req_123") } returns Unit

        viewModel.startJob("req_123")

        coVerify(exactly = 1) { repository.startJob("req_123") }
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun completeJob_callsRepositoryAndManagesLoading() {
        // Given
        coEvery { repository.markCompletedByProvider("req_123") } returns Unit

        // When
        viewModel.completeJob("req_123")

        // Then
        coVerify(exactly = 1) { repository.markCompletedByProvider("req_123") }
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun confirmJob_callsRepositoryAndManagesLoading() {
        // Given
        coEvery { repository.confirmByClient("req_123") } returns Unit

        // When
        viewModel.confirmJob("req_123")

        // Then
        coVerify(exactly = 1) { repository.confirmByClient("req_123") }
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun initialStates_areCorrectlySetToNullOrFalse() {
        assertNull(viewModel.currentRequest.value)
        assertNull(viewModel.provider.value)
        assertNull(viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
    }
}
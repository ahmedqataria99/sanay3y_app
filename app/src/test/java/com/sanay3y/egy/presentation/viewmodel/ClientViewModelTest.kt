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

class ClientViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val repository: ProviderRepository = mockk()
    private lateinit var viewModel: ClientViewModel

    // قائمة وهمية من الصنايعية لاستخدامها في التيستات
    private val fakeProviders = listOf(
        Provider(id = "p1", name = "الأسطى حسن", category = "سباكة"),
        Provider(id = "p2", name = "الأسطى علي", category = "نجارة")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // =================================================================
    // 1. اختبار الـ Initialization ودالة loadProviders
    // =================================================================

    @Test
    fun init_loadsProvidersSuccessfully_updatesUiState() {
        // Given: السيرفر هيرجع لستة الصنايعية بنجاح
        coEvery { repository.getAllProviders() } returns Result.success(fakeProviders)

        // When: ننشئ الـ ViewModel (الـ init بيشتغل تلقائياً)
        viewModel = ClientViewModel(repository)

        // Then: نتأكد إن الداتا وصلت والـ Loading قفل ومفيش أخطاء
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(fakeProviders, viewModel.uiState.value.providers)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun init_loadsProvidersFailure_setsErrorInUiState() {
        // Given: السيرفر هيرجع فشل
        coEvery { repository.getAllProviders() } returns Result.failure(Exception("عطل في السيرفر"))

        // When
        viewModel = ClientViewModel(repository)

        // Then: الـ Loading يقفل والخطأ يظهر في الـ UI
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("عطل في السيرفر", viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.providers.isEmpty())
    }

    // =================================================================
    // 2. اختبار دالة البحث (search)
    // =================================================================

    @Test
    fun search_withBlankQuery_reloadsAllProviders() {
        // Given: هنجهز الرد للـ getAllProviders والـ search
        coEvery { repository.getAllProviders() } returns Result.success(fakeProviders)

        viewModel = ClientViewModel(repository)

        // When: نبحث بكلمة فاضية أو مسافات
        viewModel.search("   ")

        // Then: نتأكد إن الكود راح جاب كل الصنايعية تاني وما عملش بحث بالكلمة الفاضية
        coVerify(exactly = 2) { repository.getAllProviders() } // مرة في الـ init ومرة في البحث
    }

    @Test
    fun search_withValidQuery_success_updatesFilteredProviders() {
        coEvery { repository.getAllProviders() } returns Result.success(fakeProviders)
        viewModel = ClientViewModel(repository)

        // Given: نتيجة البحث بكلمة "حسن"
        val searchResult = listOf(fakeProviders[0])
        coEvery { repository.searchAndFilterProviders("حسن") } returns Result.success(searchResult)

        // When: نبحث عن حسن
        viewModel.search("حسن")

        // Then: الحالة تتحدث بنتيجة البحث فقط
        assertEquals(searchResult, viewModel.uiState.value.providers)
    }

    // =================================================================
    // 3. اختبار الفلترة بالقسم (filterByCategory)
    // =================================================================

    @Test
    fun filterByCategory_success_updatesCategoryProviders() {
        coEvery { repository.getAllProviders() } returns Result.success(fakeProviders)
        viewModel = ClientViewModel(repository)

        val plumbingProviders = listOf(fakeProviders[0])
        coEvery { repository.getProvidersByCategory("سباكة") } returns Result.success(plumbingProviders)

        viewModel.filterByCategory("سباكة")

        assertEquals(plumbingProviders, viewModel.uiState.value.providers)
    }

    // =================================================================
    // 4. اختبار الأعلى تقييماً (loadTopRated)
    // =================================================================

    @Test
    fun loadTopRated_success_updatesTopRatedProviders() {
        coEvery { repository.getAllProviders() } returns Result.success(fakeProviders)
        viewModel = ClientViewModel(repository)

        coEvery { repository.getTopRatedProviders() } returns Result.success(fakeProviders)

        viewModel.loadTopRated()

        assertEquals(fakeProviders, viewModel.uiState.value.providers)
    }

    // =================================================================
    // 5. اختبار اختيار صنايعي ومراقبته (selectProvider & loadProvider)
    // =================================================================

    @Test
    fun selectProvider_synchronous_updatesSelectedProviderFlow() {
        coEvery { repository.getAllProviders() } returns Result.success(fakeProviders)
        viewModel = ClientViewModel(repository)

        // When: نختار صنايعي بشكل مباشر من اللستة
        viewModel.selectProvider(fakeProviders[0])

        // Then: الـ Flow يتحدث فوراً بالصنايعي اللي اخترناه
        assertEquals(fakeProviders[0], viewModel.selectedProvider.value)
    }

    @Test
    fun loadProvider_fromRepository_success_updatesSelectedProvider() {
        coEvery { repository.getAllProviders() } returns Result.success(fakeProviders)
        viewModel = ClientViewModel(repository)

        // Given: لما نطلب صنايعي بـ ID معين يرجعلنا البيانات بتاعته
        coEvery { repository.getProviderById("p1") } returns Result.success(fakeProviders[0])

        // When
        viewModel.loadProvider("p1")

        // Then
        assertEquals(fakeProviders[0], viewModel.selectedProvider.value)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    // =================================================================
    // 6. اختبار دالة الجلب المباشر (getProviderById) - Option 3
    // =================================================================

    @Test
    fun getProviderById_suspendDirectLookup_returnsProviderCorrectly() = runTest {
        coEvery { repository.getAllProviders() } returns Result.success(fakeProviders)
        viewModel = ClientViewModel(repository)

        coEvery { repository.getProviderById("p2") } returns Result.success(fakeProviders[1])

        // When: بننادي الدالة الـ suspend مباشرة جوة runTest
        val result = viewModel.getProviderById("p2")

        // Then: الدالة بترجع الكائن نفسه مش بتعدل في الـ state
        assertEquals(fakeProviders[1], result)
    }
}
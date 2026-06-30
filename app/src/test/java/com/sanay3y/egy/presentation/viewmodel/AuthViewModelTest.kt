package com.sanay3y.egy.presentation.viewmodel

import android.app.Application
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.sanay3y.egy.data.auth.AuthRepository
import com.sanay3y.egy.data.auth.AuthResult
import com.sanay3y.egy.data.model.Provider
import com.sanay3y.egy.data.model.User
import com.sanay3y.egy.data.model.UserRole
import com.sanay3y.egy.data.repository.UserRepository
import com.sanay3y.egy.utils.PreferenceManager
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import net.bytebuddy.matcher.ElementMatchers.any
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthViewModelTest {
    // الـ Dispatcher البديل لتشغيل الكوروتينز فوراً في التيست
    private val testDispatcher = UnconfinedTestDispatcher()

    private val application: Application = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk()
    private val userRepository: UserRepository = mockk()

    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        // 1. تحويل الـ Main Dispatcher لـ Test Dispatcher عشان الـ viewModelScope يشتغل
        Dispatchers.setMain(testDispatcher)

        // 2. محاكاة كلاس الـ Log بتاع الأندرويد عشان ما يعملش كراش أثناء التيست
        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0

        // 3. محاكاة الـ Constructor بتاع الـ PreferenceManager لأنه بيتكون داخلياً
        mockkConstructor(PreferenceManager::class)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // دالة مساعدة لتجهيز الـ PreferenceManager المزيف قبل إنشاء الـ ViewModel
    private fun setupDefaultPreferences(
        uid: String? = null,
        isLoggedIn: Boolean = false,
        role: UserRole? = null
    ) {
        every { anyConstructed<PreferenceManager>().getUserUid() } returns uid
        every { anyConstructed<PreferenceManager>().isLoggedIn() } returns isLoggedIn
        every { anyConstructed<PreferenceManager>().getUserRole() } returns role
        every { anyConstructed<PreferenceManager>().clearSession() } returns Unit
        every { anyConstructed<PreferenceManager>().saveUserSession(any(), any()) } returns Unit
    }

    // =================================================================
    // 1. اختبار فحص الجلسة تلقائياً (checkSession / Init)
    // =================================================================

    @Test
    fun init_sessionValid_setsSuccessState() {
        // التجهيز: المستخدم مسجل بالفعل والبيانات متطابقة
        val mockFirebaseUser = mockk<FirebaseUser>()
        every { mockFirebaseUser.uid } returns "user_123"
        every { authRepository.getCurrentUser() } returns mockFirebaseUser
        setupDefaultPreferences(uid = "user_123", isLoggedIn = true, role = UserRole.CLIENT)

        viewModel = AuthViewModel(application, authRepository, userRepository)

        // التأكيد: الحالة تحولت لـ Success مباشرة
        assertEquals(AuthState.Success("user_123", true, UserRole.CLIENT), viewModel.authState.value)
    }

    @Test
    fun init_sessionInvalid_clearsSessionAndSetsIdleState() {
        // التجهيز: الجلسة منتهية أو البيانات غير صحيحة
        every { authRepository.getCurrentUser() } returns null
        setupDefaultPreferences(uid = null, isLoggedIn = false)

        viewModel = AuthViewModel(application, authRepository, userRepository)

        // التأكيد: الجلسة اتمسحت والحالة رجعت Idle
        assertEquals(AuthState.Idle, viewModel.authState.value)
        verify { anyConstructed<PreferenceManager>().clearSession() }
    }

    // =================================================================
    // 2. اختبار دالة تسجيل الدخول (login)
    // =================================================================

    @Test
    fun login_success_userExistsInFirestore_emitsSuccess() {
        // التجهيز الافتراضي للـ init لتكون الحالة Idle
        every { authRepository.getCurrentUser() } returns null
        setupDefaultPreferences()
        viewModel = AuthViewModel(application, authRepository, userRepository)

        // تجهيز بيانات تسجيل الدخول
        val fakeUser = User(id = "user_123", firebaseUid = "user_123", role = UserRole.PROVIDER)
        coEvery { authRepository.login("test@test.com", "123456") } returns AuthResult.Success("user_123")
        coEvery { userRepository.getUserByUid("user_123") } returns Result.success(fakeUser)

        viewModel.login("test@test.com", "123456")

        // التأكيد: تم الحفظ بنجاح والحالة اصبحت نجاح مع الدور (Provider)
        verify { anyConstructed<PreferenceManager>().saveUserSession("user_123", UserRole.PROVIDER) }
        assertEquals(AuthState.Success("user_123", true, UserRole.PROVIDER), viewModel.authState.value)
    }

    @Test
    fun login_success_userMissingInFirestore_syncsSuccessfully() {
        every { authRepository.getCurrentUser() } returns null
        setupDefaultPreferences()
        viewModel = AuthViewModel(application, authRepository, userRepository)

        // اليوزر نجح في الـ Auth بس مش موجود في السيرفر (Firestore)
        coEvery { authRepository.login("test@test.com", "123456") } returns AuthResult.Success("user_123")
        coEvery { userRepository.getUserByUid("user_123") } returns Result.success(null) // مش موجود
        coEvery { userRepository.syncUser("user_123", "", "test@test.com") } returns Result.success(Unit) // نجاح المزامنة

        viewModel.login("test@test.com", "123456")

        // التأكيد: نجح لكن بدون دور (hasRole = false) عشان يختار الدور بعد كدة
        assertEquals(AuthState.Success("user_123", false, null), viewModel.authState.value)
    }

    @Test
    fun login_failed_emitsErrorState() {
        every { authRepository.getCurrentUser() } returns null
        setupDefaultPreferences()
        viewModel = AuthViewModel(application, authRepository, userRepository)

        coEvery { authRepository.login("any@test.com", "wrong") } returns AuthResult.Error("Wrong Password")

        viewModel.login("any@test.com", "wrong")

        assertEquals(AuthState.Error("Wrong Password"), viewModel.authState.value)
    }

    // =================================================================
    // 3. اختبار دالة إنشاء حساب جديد (register)
    // =================================================================

    @Test
    fun register_success_createsUserAndEmitsSuccessWithoutRole() {
        every { authRepository.getCurrentUser() } returns null
        setupDefaultPreferences()
        viewModel = AuthViewModel(application, authRepository, userRepository)

        coEvery { authRepository.register("new@test.com", "123456") } returns AuthResult.Success("new_uid")
        coEvery { userRepository.createUser(any()) } returns Result.success(Unit)

        viewModel.register("new@test.com", "Abdo", "123456")

        verify { anyConstructed<PreferenceManager>().saveUserSession("new_uid", null) }
        assertEquals(AuthState.Success("new_uid", false, null), viewModel.authState.value)
    }

    @Test
    fun register_authFails_emitsError() {
        every { authRepository.getCurrentUser() } returns null
        setupDefaultPreferences()
        viewModel = AuthViewModel(application, authRepository, userRepository)

        coEvery { authRepository.register("bad@email.com", "123") } returns AuthResult.Error("Email already in use")

        viewModel.register("bad@email.com", "Abdo", "123")

        assertEquals(AuthState.Error("Email already in use"), viewModel.authState.value)
    }

    // =================================================================
    // 4. اختبار دالة اختيار الدور وتحديثه (selectRole)
    // =================================================================

    @Test
    fun selectRole_success_updatesFirestoreAndPreferences() {
        every { authRepository.getCurrentUser() } returns null
        setupDefaultPreferences()
        viewModel = AuthViewModel(application, authRepository, userRepository)

        coEvery { userRepository.updateRole("user_123", UserRole.CLIENT) } returns Result.success(Unit)

        viewModel.selectRole("user_123", UserRole.CLIENT)

        verify { anyConstructed<PreferenceManager>().saveUserSession("user_123", UserRole.CLIENT) }
        assertEquals(AuthState.Success("user_123", true, UserRole.CLIENT), viewModel.authState.value)
    }

    // =================================================================
    // 5. اختبار دالة تسجيل الخروج (logout)
    // =================================================================

    @Test
    fun logout_clearsAuthAndPreferencesAndResetsToIdle() {
        every { authRepository.getCurrentUser() } returns null
        setupDefaultPreferences()
        viewModel = AuthViewModel(application, authRepository, userRepository)

        every { authRepository.logout() } returns Unit

        viewModel.logout()

        verify { authRepository.logout() }
        verify { anyConstructed<PreferenceManager>().clearSession() }
        assertEquals(AuthState.Idle, viewModel.authState.value)
    }
}
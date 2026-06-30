package com.sanay3y.egy.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.sanay3y.egy.data.model.Provider
import com.sanay3y.egy.data.model.User
import com.sanay3y.egy.data.model.UserRole
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UserRepositoryTest {
    private lateinit var repository: UserRepository
    private val firestore: FirebaseFirestore = mockk()
    private val usersCollection: CollectionReference = mockk()
    private val docRef: DocumentReference = mockk()
    private val snapshot: DocumentSnapshot = mockk()

    @Before
    fun setUp() {
        // تجهيز الفايربيز المزيف قبل كل تيست
        mockkStatic(FirebaseFirestore::class)
        every { FirebaseFirestore.getInstance() } returns firestore
        every { firestore.collection("users") } returns usersCollection

        repository = UserRepository()
    }

    @After
    fun tearDown() {
        unmockkStatic(FirebaseFirestore::class)
    }

    // دالة مساعدة لعمل Task وهمي (ناجح أو فاشل)
    private fun <T> mockTask(result: T?, exception: Exception? = null): Task<T> {
        val task = mockk<Task<T>>()
        every { task.isComplete } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns exception
        every { task.result } returns result
        return task
    }

    // =================================================================
    // 1. اختبار دالة createUser
    // =================================================================
    @Test
    fun createUser_success_returnsSuccess() = runTest {
        val fakeUser = User(firebaseUid = "uid_123", name = "احمد")

        every { usersCollection.document("uid_123") } returns docRef
        every { docRef.set(any()) } returns mockTask(null)

        val result = repository.createUser(fakeUser)

        assertTrue(result.isSuccess)
        verify { docRef.set(any()) }
    }

    @Test
    fun createUser_failure_returnsFailure() = runTest {
        val fakeUser = User(firebaseUid = "uid_123")

        every { usersCollection.document("uid_123") } returns docRef
        every { docRef.set(any()) } returns mockTask(null, Exception("Network Error"))

        val result = repository.createUser(fakeUser)

        assertTrue(result.isFailure)
    }

    // =================================================================
    // 2. اختبار دالة getUserByUid
    // =================================================================
    @Test
    fun getUserByUid_userExists_returnsUser() = runTest {
        val fakeUser = User(firebaseUid = "uid_123", name = "محمد")

        every { usersCollection.document("uid_123") } returns docRef
        every { docRef.get() } returns mockTask(snapshot)
        every { snapshot.id } returns "uid_123"
        every { snapshot.toObject(User::class.java) } returns fakeUser

        val result = repository.getUserByUid("uid_123")

        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertEquals("uid_123", user?.id)
        assertEquals("محمد", user?.name)
    }

    @Test
    fun getUserByUid_userDoesNotExist_returnsNull() = runTest {
        every { usersCollection.document("uid_not_found") } returns docRef
        every { docRef.get() } returns mockTask(snapshot)
        every { snapshot.id } returns "uid_not_found"
        every { snapshot.toObject(User::class.java) } returns null // السيرفر مرجعش داتا

        val result = repository.getUserByUid("uid_not_found")

        assertTrue(result.isSuccess)
        assertNull(result.getOrNull()) // النتيجة نجاح بس اليوزر بـ null
    }

    // =================================================================
    // 3. اختبار دالة syncUser
    // =================================================================
    @Test
    fun syncUser_userDoesNotExist_createsNewUser() = runTest {
        every { usersCollection.document("uid_new") } returns docRef
        every { docRef.get() } returns mockTask(snapshot)
        every { snapshot.exists() } returns false // اليوزر مش موجود في الداتا بيز
        every { docRef.set(any(), any<SetOptions>()) } returns mockTask(null)

        val result = repository.syncUser("uid_new", "علي", "ali@test.com")

        assertTrue(result.isSuccess)
        // بنتأكد إن أمر الـ set اشتغل عشان اليوزر جديد
        verify { docRef.set(any(), SetOptions.merge()) }
    }

    @Test
    fun syncUser_userAlreadyExists_doesNotCreateUser() = runTest {
        every { usersCollection.document("uid_existing") } returns docRef
        every { docRef.get() } returns mockTask(snapshot)
        every { snapshot.exists() } returns true // اليوزر موجود بالفعل!

        val result = repository.syncUser("uid_existing", "علي", "ali@test.com")

        assertTrue(result.isSuccess)
        // بنتأكد إن الكود ما عملش set ومفيش داتا جديدة كتبت
        verify(exactly = 0) { docRef.set(any(), any<SetOptions>()) }
    }

    // =================================================================
    // 4. اختبار دالة updateRole
    // =================================================================
    @Test
    fun updateRole_success_updatesOnlyRoleField() = runTest {
        val updateMap = mapOf("role" to UserRole.PROVIDER.name)

        every { usersCollection.document("uid_123") } returns docRef
        every { docRef.set(updateMap, SetOptions.merge()) } returns mockTask(null)

        val result = repository.updateRole("uid_123", UserRole.PROVIDER)

        assertTrue(result.isSuccess)
        verify { docRef.set(updateMap, SetOptions.merge()) }
    }

}
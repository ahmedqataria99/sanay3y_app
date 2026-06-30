package com.sanay3y.egy.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.sanay3y.egy.data.model.Provider
import com.sanay3y.egy.data.model.Request
import com.sanay3y.egy.data.model.RequestStatus
import com.sanay3y.egy.data.model.Review
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RequestRepositoryTest {
    private lateinit var repository: RequestRepository
    private val firestore: FirebaseFirestore = mockk()
    private val requestsCollection: CollectionReference = mockk()
    private val reviewsCollection: CollectionReference = mockk()

    @Before
    fun setUp() {
        mockkStatic(FirebaseFirestore::class)
        every { FirebaseFirestore.getInstance() } returns firestore
        every { firestore.collection("requests") } returns requestsCollection
        every { firestore.collection("reviews") } returns reviewsCollection

        repository = RequestRepository()
    }

    @After
    fun tearDown() {
        unmockkStatic(FirebaseFirestore::class)
    }

    // دالة مساعدة لمحاكاة الـ Task سواء نجاح أو فشل
    private fun <T> mockTask(result: T?, exception: Exception? = null): Task<T> {
        val task = mockk<Task<T>>()
        every { task.isComplete } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns exception
        every { task.result } returns result
        return task
    }

    // =================================================================
    // 1. اختبار دالة createRequest
    // =================================================================
    @Test
    fun createRequest_success_returnsSuccess() = runTest {
        val docRef = mockk<DocumentReference>()
        val fakeRequest = Request(userId = "user_123")

        every { requestsCollection.document() } returns docRef
        every { docRef.id } returns "new_req_id"
        every { docRef.set(any()) } returns mockTask(null)

        val result = repository.createRequest(fakeRequest)

        assertTrue(result.isSuccess)
        verify { docRef.set(any()) }
    }

    @Test
    fun createRequest_failure_returnsFailure() = runTest {
        val docRef = mockk<DocumentReference>()
        every { requestsCollection.document() } returns docRef
        every { docRef.id } returns "new_req_id"
        every { docRef.set(any()) } returns mockTask(null, Exception("Firestore Error"))

        val result = repository.createRequest(Request())

        assertTrue(result.isFailure)
    }

    // =================================================================
    // 2. اختبار دالة getUserRequests
    // =================================================================
    @Test
    fun getUserRequests_success_returnsList() = runTest {
        val query = mockk<Query>()
        val snapshot = mockk<QuerySnapshot>()
        val document = mockk<DocumentSnapshot>()
        val fakeRequest = Request(userId = "user_1")

        every { requestsCollection.whereEqualTo("userId", "user_1") } returns query
        every { query.get() } returns mockTask(snapshot)
        every { snapshot.documents } returns listOf(document)
        every { document.id } returns "req_1"
        every { document.toObject(Request::class.java) } returns fakeRequest

        val result = repository.getUserRequests("user_1")

        assertTrue(result.isSuccess)
        val list = result.getOrNull()
        assertEquals(1, list?.size)
        assertEquals("req_1", list?.get(0)?.id)
    }

    // =================================================================
    // 3. اختبار دالة getAvailableRequests
    // =================================================================
    @Test
    fun getAvailableRequests_success_returnsFilteredList() = runTest {
        val query = mockk<Query>()
        val snapshot = mockk<QuerySnapshot>()
        val document = mockk<DocumentSnapshot>()

        every { requestsCollection.whereEqualTo("status", RequestStatus.PENDING.name) } returns query
        every { query.get() } returns mockTask(snapshot)
        every { snapshot.documents } returns listOf(document)
        every { document.id } returns "req_pending"
        every { document.toObject(Request::class.java) } returns Request(status = RequestStatus.PENDING.name)

        val result = repository.getAvailableRequests()

        assertTrue(result.isSuccess)
        assertEquals("req_pending", result.getOrNull()?.get(0)?.id)
    }

    // =================================================================
    // 4. اختبار دالة acceptRequest
    // =================================================================
    @Test
    fun acceptRequest_success_updatesFields() = runTest {
        val docRef = mockk<DocumentReference>()
        val updateMap = mapOf("providerId" to "provider_1", "status" to RequestStatus.ACCEPTED.name)

        every { requestsCollection.document("req_1") } returns docRef
        every { docRef.update(updateMap) } returns mockTask(null)

        val result = repository.acceptRequest("req_1", "provider_1")

        assertTrue(result.isSuccess)
        verify { docRef.update(updateMap) }
    }

    // =================================================================
    // 5. اختبار دالة startJob
    // =================================================================
    @Test
    fun startJob_success_updatesStatus() = runTest {
        val docRef = mockk<DocumentReference>()

        every { requestsCollection.document("req_1") } returns docRef
        every { docRef.update("status", RequestStatus.IN_PROGRESS.name) } returns mockTask(null)

        val result = repository.startJob("req_1")

        assertTrue(result.isSuccess)
        verify { docRef.update("status", RequestStatus.IN_PROGRESS.name) }
    }

    // =================================================================
    // 6. اختبار دالة markCompletedByProvider
    // =================================================================
    @Test
    fun markCompletedByProvider_success_updatesStatusAndFlag() = runTest {
        val docRef = mockk<DocumentReference>()
        val updateMap = mapOf("status" to RequestStatus.COMPLETED_BY_PROVIDER.name, "providerCompleted" to true)

        every { requestsCollection.document("req_1") } returns docRef
        every { docRef.update(updateMap) } returns mockTask(null)

        val result = repository.markCompletedByProvider("req_1")

        assertTrue(result.isSuccess)
        verify { docRef.update(updateMap) }
    }

    // =================================================================
    // 7. اختبار دالة updateRequestStatus
    // =================================================================
    @Test
    fun updateRequestStatus_success_updatesStatusDynamic() = runTest {
        val docRef = mockk<DocumentReference>()

        every { requestsCollection.document("req_1") } returns docRef
        every { docRef.update("status", RequestStatus.ACCEPTED.name) } returns mockTask(null)

        val result = repository.updateRequestStatus("req_1", RequestStatus.ACCEPTED)

        assertTrue(result.isSuccess)
        verify { docRef.update("status", RequestStatus.ACCEPTED.name) }
    }

    // =================================================================
    // 8. اختبار دالة confirmByClient
    // =================================================================
    @Test
    fun confirmByClient_success_updatesStatusAndFlag() = runTest {
        val docRef = mockk<DocumentReference>()
        val updateMap = mapOf("status" to RequestStatus.COMPLETED_BY_CLIENT.name, "clientConfirmed" to true)

        every { requestsCollection.document("req_1") } returns docRef
        every { docRef.update(updateMap) } returns mockTask(null)

        val result = repository.confirmByClient("req_1")

        assertTrue(result.isSuccess)
        verify { docRef.update(updateMap) }
    }

    // =================================================================
    // 9. اختبار دالة getActiveJobs (تستخدم whereIn)
    // =================================================================
    @Test
    fun getActiveJobs_success_returnsFilteredList() = runTest {
        val query1 = mockk<Query>()
        val query2 = mockk<Query>()
        val snapshot = mockk<QuerySnapshot>()
        val document = mockk<DocumentSnapshot>()
        val statuses = listOf(RequestStatus.ACCEPTED.name, RequestStatus.IN_PROGRESS.name, RequestStatus.COMPLETED_BY_PROVIDER.name)

        every { requestsCollection.whereEqualTo("providerId", "provider_1") } returns query1
        every { query1.whereIn("status", statuses) } returns query2
        every { query2.get() } returns mockTask(snapshot)
        every { snapshot.documents } returns listOf(document)
        every { document.id } returns "active_req"
        every { document.toObject(Request::class.java) } returns Request()

        val result = repository.getActiveJobs("provider_1")

        assertTrue(result.isSuccess)
        assertEquals("active_req", result.getOrNull()?.get(0)?.id)
    }

    // =================================================================
    // 10. اختبار دالة getCompletedJobs
    // =================================================================
    @Test
    fun getCompletedJobs_success_returnsFilteredList() = runTest {
        val query = mockk<Query>()
        val snapshot = mockk<QuerySnapshot>()
        val document = mockk<DocumentSnapshot>()

        every { requestsCollection.whereEqualTo("providerId", "provider_1") } returns query
        every { query.whereEqualTo("status", RequestStatus.COMPLETED_BY_CLIENT.name) } returns mockk<Query>().apply {
            every { get() } returns mockTask(snapshot)
        }
        every { snapshot.documents } returns listOf(document)
        every { document.id } returns "completed_req"
        every { document.toObject(Request::class.java) } returns Request()

        val result = repository.getCompletedJobs("provider_1")

        assertTrue(result.isSuccess)
    }

    // =================================================================
    // 11. اختبار دالة observeRequest و stopObserving
    // =================================================================
    @Test
    fun observeRequest_and_stopObserving_worksCorrectly() {
        val docRef = mockk<DocumentReference>()
        val snapshot = mockk<DocumentSnapshot>()
        val registration = mockk<ListenerRegistration>()
        val listenerSlot = slot<EventListener<DocumentSnapshot>>()

        // 🟢 التعديل هنا: استخدمنا any() بدل "req_1" عشان يقبل أي ID
        every { requestsCollection.document(any()) } returns docRef
        every { docRef.addSnapshotListener(capture(listenerSlot)) } returns registration
        every { snapshot.id } returns "req_1"
        every { snapshot.toObject(Request::class.java) } returns Request()
        every { registration.remove() } returns Unit

        // 1. شغل المراقبة لأول مرة لـ req_1
        repository.observeRequest("req_1") {}

        // 2. شغل المراقبة لطلب تاني req_2 (عشان نتأكد إنه بيقفل القديم الأول)
        repository.observeRequest("req_2") {}
        verify { registration.remove() }

        // 3. اختبار دالة إيقاف المراقبة
        repository.stopObserving()
        verify(exactly = 2) { registration.remove() }
    }

    // =================================================================
    // 12. اختبار دالة addReview
    // =================================================================
    @Test
    fun addReview_success_createsReviewDocument() = runTest {
        val docRef = mockk<DocumentReference>()
        val fakeReview = Review(comment = "ممتاز")

        every { reviewsCollection.document() } returns docRef
        every { docRef.id } returns "rev_123"
        every { docRef.set(any()) } returns mockTask(null)

        val result = repository.addReview(fakeReview)

        assertTrue(result.isSuccess)
        verify { docRef.set(any()) }
    }
}
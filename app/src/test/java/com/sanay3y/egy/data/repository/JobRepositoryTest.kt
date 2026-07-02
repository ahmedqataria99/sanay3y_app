package com.sanay3y.egy.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.sanay3y.egy.data.model.Request
import com.sanay3y.egy.data.model.RequestStatus
import io.mockk.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class JobRepositoryTest {

    private lateinit var repository: JobRepository
    private val firestore: FirebaseFirestore = mockk()
    private val collectionRef: CollectionReference = mockk()

    @Before
    fun setUp() {
        mockkStatic(FirebaseFirestore::class)
        every { FirebaseFirestore.getInstance() } returns firestore
        every { firestore.collection("requests") } returns collectionRef

        repository = JobRepository()
    }

    @After
    fun tearDown() {
        unmockkStatic(FirebaseFirestore::class)
    }

    private fun <T> mockTask(result: T): Task<T> {
        val task = mockk<Task<T>>()
        every { task.isComplete } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns null
        every { task.result } returns result
        return task
    }

    // ================== TESTS ==================

    @Test
    fun getAvailableRequests_success_returnsList() = runTest {
        val query = mockk<Query>()
        val snapshot = mockk<QuerySnapshot>()
        val document = mockk<DocumentSnapshot>()
        val fakeRequest = Request(status = RequestStatus.PENDING.name)

        every { collectionRef.whereEqualTo("status", "PENDING") } returns query
        every { query.get() } returns mockTask(snapshot)
        every { snapshot.documents } returns listOf(document)
        every { document.id } returns "doc_123"
        every { document.toObject(Request::class.java) } returns fakeRequest

        val result = repository.getAvailableRequests(providerId = "")

        assertEquals(1, result.size)
        assertEquals("doc_123", result[0].id)
    }

    @Test
    fun getProviderJobs_success_returnsList() = runTest {
        val query = mockk<Query>()
        val snapshot = mockk<QuerySnapshot>()
        val document = mockk<DocumentSnapshot>()
        val fakeRequest = Request(providerId = "provider_1")

        every { collectionRef.whereEqualTo("providerId", "provider_1") } returns query
        every { query.get() } returns mockTask(snapshot)
        every { snapshot.documents } returns listOf(document)
        every { document.id } returns "doc_456"
        every { document.toObject(Request::class.java) } returns fakeRequest

        val result = repository.getProviderJobs("provider_1")

        assertEquals(1, result.size)
        assertEquals("doc_456", result[0].id)
    }

    @Test
    fun submitQuotation_updatesFirestoreWithCosts() = runTest {
        val docRef = mockk<DocumentReference>()
        val updateMap = mapOf(
            "providerId" to "provider_1",
            "laborCost" to 200.0,
            "materialsCost" to 100.0,
            "totalPrice" to 300.0,
            "status" to RequestStatus.QUOTED.name
        )

        every { collectionRef.document("req_1") } returns docRef
        every { docRef.update(updateMap) } returns mockTask(null)

        val result = repository.submitQuotation("req_1", "provider_1", 200.0, 100.0)

        assertTrue(result.isSuccess)
        verify { docRef.update(updateMap) }
    }

    @Test
    fun acceptQuotation_updatesStatusToAccepted() = runTest {
        val docRef = mockk<DocumentReference>()

        every { collectionRef.document("req_1") } returns docRef
        every { docRef.update("status", RequestStatus.ACCEPTED.name) } returns mockTask(null)

        val result = repository.acceptQuotation("req_1")

        assertTrue(result.isSuccess)
        verify { docRef.update("status", RequestStatus.ACCEPTED.name) }
    }

    @Test
    fun rejectQuotation_resetsRequestToPending() = runTest {
        val docRef = mockk<DocumentReference>()
        val updateMap = mapOf(
            "status" to RequestStatus.PENDING.name,
            "providerId" to "",
            "laborCost" to 0.0,
            "materialsCost" to 0.0,
            "totalPrice" to 0.0
        )

        every { collectionRef.document("req_1") } returns docRef
        every { docRef.update(updateMap) } returns mockTask(null)

        val result = repository.rejectQuotation("req_1")

        assertTrue(result.isSuccess)
        verify { docRef.update(updateMap) }
    }

    @Test
    fun startJob_updatesStatusToInProgress() = runTest {
        val docRef = mockk<DocumentReference>()

        every { collectionRef.document("req_1") } returns docRef
        every { docRef.update("status", RequestStatus.IN_PROGRESS.name) } returns mockTask(null)

        repository.startJob("req_1")

        verify { docRef.update("status", RequestStatus.IN_PROGRESS.name) }
    }

    @Test
    fun markCompletedByProvider_updatesStatusAndFlag() = runTest {
        val docRef = mockk<DocumentReference>()
        val updateMap = mapOf(
            "status" to RequestStatus.COMPLETED_BY_PROVIDER.name,
            "providerCompleted" to true
        )

        every { collectionRef.document("req_1") } returns docRef
        every { docRef.update(updateMap) } returns mockTask(null)

        repository.markCompletedByProvider("req_1")

        verify { docRef.update(updateMap) }
    }

    @Test
    fun confirmByClient_updatesStatusAndFlag() = runTest {
        val docRef = mockk<DocumentReference>()
        val updateMap = mapOf(
            "status" to RequestStatus.COMPLETED_BY_CLIENT.name,
            "clientConfirmed" to true
        )

        every { collectionRef.document("req_1") } returns docRef
        every { docRef.update(updateMap) } returns mockTask(null)

        repository.confirmByClient("req_1")

        verify { docRef.update(updateMap) }
    }

    @Test
    fun getActiveJobs_returnsFilteredList() = runTest {
        val query1 = mockk<Query>()
        val query2 = mockk<Query>()
        val snapshot = mockk<QuerySnapshot>()
        val document = mockk<DocumentSnapshot>()

        every { collectionRef.whereEqualTo("providerId", "provider_1") } returns query1
        every { query1.whereNotEqualTo("status", RequestStatus.COMPLETED_BY_CLIENT.name) } returns query2
        every { query2.get() } returns mockTask(snapshot)
        every { snapshot.documents } returns listOf(document)
        every { document.id } returns "active_1"
        every { document.toObject(Request::class.java) } returns Request()

        val result = repository.getActiveJobs("provider_1")

        assertEquals(1, result.size)
        assertEquals("active_1", result[0].id)
    }

    @Test
    fun getCompletedJobs_returnsFilteredList() = runTest {
        val query1 = mockk<Query>()
        val query2 = mockk<Query>()
        val snapshot = mockk<QuerySnapshot>()
        val document = mockk<DocumentSnapshot>()

        every { collectionRef.whereEqualTo("providerId", "provider_1") } returns query1
        every { query1.whereEqualTo("status", RequestStatus.COMPLETED_BY_CLIENT.name) } returns query2
        every { query2.get() } returns mockTask(snapshot)
        every { snapshot.documents } returns listOf(document)
        every { document.id } returns "completed_1"
        every { document.toObject(Request::class.java) } returns Request()

        val result = repository.getCompletedJobs("provider_1")

        assertEquals(1, result.size)
        assertEquals("completed_1", result[0].id)
    }

    @Test
    fun observeRequest_triggersCallbackOnUpdate() {
        val docRef = mockk<DocumentReference>()
        val snapshot = mockk<DocumentSnapshot>()
        val registration = mockk<ListenerRegistration>()

        val listenerSlot = slot<EventListener<DocumentSnapshot>>()

        every { collectionRef.document("req_1") } returns docRef
        every { docRef.addSnapshotListener(capture(listenerSlot)) } returns registration
        every { snapshot.id } returns "req_1"
        every { snapshot.toObject(Request::class.java) } returns Request()

        var callbackCalled = false
        var resultRequest: Request? = null

        repository.observeRequest("req_1") { request ->
            callbackCalled = true
            resultRequest = request
        }

        listenerSlot.captured.onEvent(snapshot, null)

        assertTrue(callbackCalled)
        assertNotNull(resultRequest)
        assertEquals("req_1", resultRequest?.id)
    }
}
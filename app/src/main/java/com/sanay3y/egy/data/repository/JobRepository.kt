package com.sanay3y.egy.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.sanay3y.egy.data.model.Request
import com.sanay3y.egy.data.model.RequestStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuth

class JobRepository {

    private val firestore: FirebaseFirestore get() = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    private val requestsRef get() = firestore.collection("requests")
    private var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null

    // 🟢 كل الطلبات المتاحة (لسه محدش قبلها)
    suspend fun getAvailableRequests(): List<Request> {
        return try {
            val snapshot = requestsRef
                .whereEqualTo("status", RequestStatus.PENDING.name)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Request::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // 🟢 الطلبات الخاصة بالصنايعي
    suspend fun getProviderJobs(providerId: String): List<Request> {
        return try {
            val snapshot = requestsRef
                .whereEqualTo("providerId", providerId)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Request::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // 🔵 الصنايعي يقبل الطلب
    suspend fun acceptRequest(requestId: String, providerId: String) {
        try {
            requestsRef.document(requestId).update(
                mapOf(
                    "providerId" to providerId,
                    "status" to RequestStatus.ACCEPTED.name
                )
            ).await()
        } catch (_: Exception) {
        }
    }

    // 🟡 بدء الشغل
    suspend fun startJob(requestId: String) {
        try {
            requestsRef.document(requestId).update(
                "status", RequestStatus.IN_PROGRESS.name
            ).await()
        } catch (_: Exception) {
        }
    }

    // 🔴 الصنايعي يخلص
    suspend fun markCompletedByProvider(requestId: String) {
        try {
            requestsRef.document(requestId).update(
                mapOf(
                    "status" to RequestStatus.COMPLETED_BY_PROVIDER.name,
                    "providerCompleted" to true
                )
            ).await()
        } catch (_: Exception) {
        }
    }

    // 🟢 العميل يأكد
    suspend fun confirmByClient(requestId: String) {
        try {
            requestsRef.document(requestId).update(
                mapOf(
                    "status" to RequestStatus.COMPLETED_BY_CLIENT.name,
                    "clientConfirmed" to true
                )
            ).await()
        } catch (_: Exception) {
        }
    }

    // 📦 الطلبات النشطة
    suspend fun getActiveJobs(providerId: String): List<Request> {
        return try {
            val snapshot = requestsRef
                .whereEqualTo("providerId", providerId)
                .whereNotEqualTo("status", RequestStatus.COMPLETED_BY_CLIENT.name)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Request::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // 📜 الطلبات المنتهية
    suspend fun getCompletedJobs(providerId: String): List<Request> {
        return try {
            val snapshot = requestsRef
                .whereEqualTo("providerId", providerId)
                .whereEqualTo("status", RequestStatus.COMPLETED_BY_CLIENT.name)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Request::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun observeRequest(requestId: String, onUpdate: (Request) -> Unit) {
        listenerRegistration?.remove()
        listenerRegistration = requestsRef.document(requestId)
            .addSnapshotListener { snapshot, _ ->
                val request = snapshot
                    ?.toObject(Request::class.java)
                    ?.copy(id = snapshot.id)
                if (request != null) onUpdate(request)
            }
    }

    fun stopObserving() {
        listenerRegistration?.remove()
    }

    // ── Real-time Flow للشاشات ──────────────────────────

    // Available Requests - real time (اسم مختلف عن getAvailableRequests)
    fun availableRequests(): Flow<List<Request>> = callbackFlow {
        val listener = requestsRef
            .whereEqualTo("status", RequestStatus.PENDING.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val requests = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Request::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(requests)
            }
        awaitClose { listener.remove() }
    }

    // Active Jobs - real time (اسم مختلف عن getActiveJobs)
    fun activeJobs(providerId: String): Flow<List<Request>> = callbackFlow {
        val listener = requestsRef
            .whereEqualTo("providerId", providerId)
            .whereNotEqualTo("status", RequestStatus.COMPLETED_BY_CLIENT.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val requests = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Request::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(requests)
            }
        awaitClose { listener.remove() }
    }

    // Reject Request
    suspend fun rejectJob(requestId: String): Result<Unit> = runCatching {
        requestsRef.document(requestId)
            .update("status", RequestStatus.PENDING.name)
            .await()
    }
}
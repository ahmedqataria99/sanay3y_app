package com.sanay3y.egy.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.sanay3y.egy.data.model.Request
import com.sanay3y.egy.data.model.RequestStatus
import kotlinx.coroutines.tasks.await

class JobRepository {

    private val firestore: FirebaseFirestore get() = FirebaseFirestore.getInstance()
    private val requestsRef get() = firestore.collection("requests")

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
        } catch (_: Exception) {}
    }

    // 🟡 بدء الشغل
    suspend fun startJob(requestId: String) {
        try {
            requestsRef.document(requestId).update(
                "status", RequestStatus.IN_PROGRESS.name
            ).await()
        } catch (_: Exception) {}
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
        } catch (_: Exception) {}
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
        } catch (_: Exception) {}
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
        requestsRef.document(requestId)
            .addSnapshotListener { snapshot, _ ->
                val request = snapshot
                    ?.toObject(Request::class.java)
                    ?.copy(id = snapshot.id)
                if (request != null) onUpdate(request)
            }
    }
}
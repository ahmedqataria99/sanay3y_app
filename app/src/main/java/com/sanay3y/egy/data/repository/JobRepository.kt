package com.sanay3y.egy.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.sanay3y.egy.data.model.Request
import com.sanay3y.egy.data.model.RequestStatus
import kotlinx.coroutines.tasks.await

class JobRepository {

    private val firestore: FirebaseFirestore get() = FirebaseFirestore.getInstance()
    private val requestsRef get() = firestore.collection("requests")

    // 🟢 كل الطلبات المتاحة (لسه محدش قدملها عرض)
    // 🟢 الطلبات المتاحة للصنايعي ده تحديدًا (Direct Booking: العميل بيختار الصنايعي بنفسه)
    suspend fun getAvailableRequests(providerId: String): List<Request> {
        return try {
            val snapshot = requestsRef
                .whereEqualTo("providerId", providerId)
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

    // 🟣 البروفايدر يبعت عرض سعر (Labor + Materials)
    suspend fun submitQuotation(
        requestId: String,
        providerId: String,
        laborCost: Double,
        materialsCost: Double
    ): Result<Unit> {
        return try {
            requestsRef.document(requestId).update(
                mapOf(
                    "providerId" to providerId,
                    "laborCost" to laborCost,
                    "materialsCost" to materialsCost,
                    "totalPrice" to (laborCost + materialsCost),
                    "status" to RequestStatus.QUOTED.name
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🟢 العميل يقبل عرض السعر
    suspend fun acceptQuotation(requestId: String): Result<Unit> {
        return try {
            requestsRef.document(requestId).update(
                "status", RequestStatus.ACCEPTED.name
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔴 العميل يرفض عرض السعر (الطلب يرجع متاح لصنايعية تانيين)
    suspend fun rejectQuotation(requestId: String): Result<Unit> {
        return try {
            requestsRef.document(requestId).update(
                mapOf(
                    "status" to RequestStatus.PENDING.name,
                    "providerId" to "",
                    "laborCost" to 0.0,
                    "materialsCost" to 0.0,
                    "totalPrice" to 0.0
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🟡 بدء الشغل
    suspend fun startJob(requestId: String): Result<Unit> {
        return try {
            requestsRef.document(requestId).update(
                "status", RequestStatus.IN_PROGRESS.name
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔴 الصنايعي يخلص
    suspend fun markCompletedByProvider(requestId: String): Result<Unit> {
        return try {
            requestsRef.document(requestId).update(
                mapOf(
                    "status" to RequestStatus.COMPLETED_BY_PROVIDER.name,
                    "providerCompleted" to true
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🟢 العميل يأكد
    suspend fun confirmByClient(requestId: String): Result<Unit> {
        return try {
            requestsRef.document(requestId).update(
                mapOf(
                    "status" to RequestStatus.COMPLETED_BY_CLIENT.name,
                    "clientConfirmed" to true
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 📦 الطلبات النشطة
    suspend fun getActiveJobs(providerId: String): List<Request> {
        return try {
            val snapshot = requestsRef
                .whereEqualTo("providerId", providerId)
                .whereIn("status", listOf(
                    RequestStatus.ACCEPTED.name,
                    RequestStatus.IN_PROGRESS.name,
                    RequestStatus.COMPLETED_BY_PROVIDER.name
                ))
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

    fun observeRequest(requestId: String, onUpdate: (Request) -> Unit): com.google.firebase.firestore.ListenerRegistration {
        return requestsRef.document(requestId)
            .addSnapshotListener { snapshot, _ ->
                val request = snapshot
                    ?.toObject(Request::class.java)
                    ?.copy(id = snapshot.id)
                if (request != null) onUpdate(request)
            }
    }
}
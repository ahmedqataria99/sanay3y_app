package com.sanay3y.egy.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.sanay3y.egy.data.model.Request
import com.sanay3y.egy.data.model.RequestStatus
import com.sanay3y.egy.data.model.Review
import kotlinx.coroutines.tasks.await

class RequestRepository {

    private val firestore: FirebaseFirestore get() = FirebaseFirestore.getInstance()
    private val requestsCollection get() = firestore.collection("requests")
    private val reviewsCollection get() = firestore.collection("reviews")

    // 🟢 إنشاء طلب
    suspend fun createRequest(request: Request): Result<Unit> {
        return try {
            val docRef = requestsCollection.document()

            val newRequest = request.copy(
                id = docRef.id,
                timestamp = System.currentTimeMillis(),
                status = RequestStatus.PENDING.name
            )

            docRef.set(newRequest).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🟢 طلبات المستخدم
    suspend fun getUserRequests(userId: String): Result<List<Request>> {
        return try {
            val snapshot = requestsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val requests = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Request::class.java)?.copy(id = doc.id)
            }

            Result.success(requests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔄 تحديث الحالة
    suspend fun updateRequestStatus(requestId: String, status: RequestStatus): Result<Unit> {
        return try {
            requestsCollection.document(requestId)
                .update("status", status.name)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ تأكيد العميل
    suspend fun confirmCompletion(requestId: String): Result<Unit> {
        return try {
            requestsCollection.document(requestId)
                .update(
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

    // ⭐ إضافة Review
    suspend fun addReview(review: Review): Result<Unit> {
        return try {
            val docRef = reviewsCollection.document()

            val newReview = review.copy(
                id = docRef.id,
                timestamp = System.currentTimeMillis()
            )

            docRef.set(newReview).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun confirmByClient(requestId: String): Result<Unit> {
        return try {
            requestsCollection.document(requestId).update(
                mapOf(
                    "status" to "COMPLETED_BY_CLIENT",
                    "clientConfirmed" to true
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
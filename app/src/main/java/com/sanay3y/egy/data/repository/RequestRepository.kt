package com.sanay3y.egy.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.sanay3y.egy.data.model.Request
import com.sanay3y.egy.data.model.RequestStatus
import com.sanay3y.egy.data.model.Review
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RequestRepository {

    private val firestore: FirebaseFirestore get() = FirebaseFirestore.getInstance()
    private val requestsCollection get() = firestore.collection("requests")
    private val reviewsCollection get() = firestore.collection("reviews")
    private var listenerRegistration: ListenerRegistration? = null

    suspend fun createRequest(request: Request): Result<Unit> = withContext(Dispatchers.IO) {
        try {
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

    suspend fun getUserRequests(userId: String): Result<List<Request>> = withContext(Dispatchers.IO) {
        try {
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

    // 🟢 كل الطلبات المتاحة (PENDING)
    suspend fun getAvailableRequests(): Result<List<Request>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = requestsCollection
                .whereEqualTo("status", RequestStatus.PENDING.name)
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

    // 🔵 الصنايعي يقبل الطلب
    suspend fun acceptRequest(requestId: String, providerId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            requestsCollection.document(requestId).update(
                mapOf(
                    "providerId" to providerId,
                    "status" to RequestStatus.ACCEPTED.name
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🟡 بدء الشغل
    suspend fun startJob(requestId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            requestsCollection.document(requestId).update(
                "status", RequestStatus.IN_PROGRESS.name
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔴 الصنايعي يخلص
    suspend fun markCompletedByProvider(requestId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            requestsCollection.document(requestId).update(
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

    suspend fun updateRequestStatus(requestId: String, status: RequestStatus): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            requestsCollection.document(requestId)
                .update("status", status.name)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun confirmByClient(requestId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            requestsCollection.document(requestId).update(
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

    // 📦 الطلبات النشطة للصنايعي
    suspend fun getActiveJobs(providerId: String): Result<List<Request>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = requestsCollection
                .whereEqualTo("providerId", providerId)
                .whereIn("status", listOf(
                    RequestStatus.ACCEPTED.name,
                    RequestStatus.IN_PROGRESS.name,
                    RequestStatus.COMPLETED_BY_PROVIDER.name
                ))
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

    // 📜 الطلبات المنتهية للصنايعي
    suspend fun getCompletedJobs(providerId: String): Result<List<Request>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = requestsCollection
                .whereEqualTo("providerId", providerId)
                .whereEqualTo("status", RequestStatus.COMPLETED_BY_CLIENT.name)
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

    fun observeRequest(requestId: String, onUpdate: (Request) -> Unit) {
        listenerRegistration?.remove()
        listenerRegistration = requestsCollection.document(requestId)
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

    suspend fun addReview(review: Review): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val docRef = reviewsCollection.document()
            val newReview = review.copy(
                id = docRef.id,
                timestamp = System.currentTimeMillis()
            )
            docRef.set(newReview).await()

            // Update Provider Rating
            val reviewsSnapshot = reviewsCollection
                .whereEqualTo("providerId", review.providerId)
                .get()
                .await()

            val reviews = reviewsSnapshot.documents.mapNotNull { it.toObject(Review::class.java) }
            val count = reviews.size
            val average = reviews.map { it.rating }.average()

            firestore.collection("users").document(review.providerId)
                .update(
                    mapOf(
                        "rating" to average,
                        "reviewCount" to count
                    )
                ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

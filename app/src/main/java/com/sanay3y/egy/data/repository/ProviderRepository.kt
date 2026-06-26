package com.sanay3y.egy.data.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.sanay3y.egy.data.model.Provider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProviderRepository {

    private val firestore: FirebaseFirestore get() = FirebaseFirestore.getInstance()
    private val usersCollection get() = firestore.collection("users")

    private fun DocumentSnapshot.toProvider(): Provider {
        return Provider(
            id = this.id,
            firebaseUid = this.getString("firebaseUid") ?: this.getString("id") ?: this.id,
            name = this.getString("name") ?: "Expert Provider",
            category = this.getString("category") ?: "General",
            rating = this.getDouble("rating") ?: 0.0,
            reviewCount = this.getLong("reviewCount")?.toInt() ?: 0,
            phone = this.getString("phone") ?: "No Phone",
            imageUrl = this.getString("imageUrl") ?: "",
            bio = this.getString("bio") ?: "Professional service provider available for hire.",
            experienceYears = this.getLong("experienceYears")?.toInt() ?: 0,
            isOnline = this.getBoolean("isOnline") ?: false,
            location = this.getString("location") ?: "Not specified",
            latitude = this.getDouble("latitude") ?: 0.0,
            longitude = this.getDouble("longitude") ?: 0.0
        )
    }

    // 🟢 Get All Providers
    suspend fun getAllProviders(): Result<List<Provider>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = usersCollection
                .whereEqualTo("role", "PROVIDER")
                .get()
                .await()

            val providers = snapshot.documents.map { it.toProvider() }
            Result.success(providers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🟢 Filter by Category
    suspend fun getProvidersByCategory(category: String): Result<List<Provider>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = usersCollection
                .whereEqualTo("role", "PROVIDER")
                .whereEqualTo("category", category)
                .get()
                .await()

            val providers = snapshot.documents.map { it.toProvider() }
            Result.success(providers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🟢 Get Single Provider
    suspend fun getProviderById(id: String): Result<Provider?> = withContext(Dispatchers.IO) {
        try {
            val directDoc = usersCollection.document(id).get().await()
            if (directDoc.exists() && directDoc.getString("role") == "PROVIDER") {
                return@withContext Result.success(directDoc.toProvider())
            }

            val snapshot = usersCollection
                .whereEqualTo("firebaseUid", id)
                .whereEqualTo("role", "PROVIDER")
                .limit(1)
                .get()
                .await()

            val providerDoc = snapshot.documents.firstOrNull()
            if (providerDoc == null) {
                return@withContext Result.success(null)
            }

            Result.success(providerDoc.toProvider())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔍 Search by Name
    suspend fun searchProviders(query: String): Result<List<Provider>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = usersCollection
                .whereEqualTo("role", "PROVIDER")
                .orderBy("name")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .await()

            val providers = snapshot.documents.map { it.toProvider() }
            Result.success(providers)
        } catch (e: Exception) {
            try {
                val snapshot = usersCollection.whereEqualTo("role", "PROVIDER").get().await()
                val providers = snapshot.documents.map { it.toProvider() }.filter {
                    it.name.contains(query, ignoreCase = true)
                }
                Result.success(providers)
            } catch (inner: Exception) {
                Result.failure(e)
            }
        }
    }

    // ⭐ Top Rated
    suspend fun getTopRatedProviders(): Result<List<Provider>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = usersCollection
                .whereEqualTo("role", "PROVIDER")
                .orderBy("rating", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .await()

            val providers = snapshot.documents.map { it.toProvider() }
            Result.success(providers)
        } catch (e: Exception) {
            try {
                val snapshot = usersCollection.whereEqualTo("role", "PROVIDER").get().await()
                val providers = snapshot.documents.map { it.toProvider() }
                    .sortedByDescending { it.rating }
                    .take(10)
                Result.success(providers)
            } catch (inner: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun saveProviderProfile(provider: Provider): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val docId = provider.firebaseUid.takeIf { it.isNotBlank() } ?: provider.id
            val payload = mapOf(
                "id" to docId,
                "firebaseUid" to docId,
                "role" to "PROVIDER",
                "name" to provider.name,
                "category" to provider.category,
                "phone" to provider.phone,
                "bio" to provider.bio,
                "location" to provider.location,
                "isOnline" to provider.isOnline,
                "imageUrl" to provider.imageUrl,
                "experienceYears" to provider.experienceYears,
                "latitude" to provider.latitude,
                "longitude" to provider.longitude,
                "rating" to provider.rating,
                "reviewCount" to provider.reviewCount
            )

            usersCollection.document(docId)
                .set(payload, SetOptions.merge())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

package com.sanay3y.egy.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.sanay3y.egy.data.model.Provider
import kotlinx.coroutines.tasks.await

class ProviderRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val providersCollection = firestore.collection("providers")

    // 🟢 كل الصنايعية
    suspend fun getAllProviders(): Result<List<Provider>> {
        return try {
            val snapshot = providersCollection.get().await()

            val providers = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Provider::class.java)?.copy(id = doc.id)
            }

            Result.success(providers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🟢 حسب الكاتيجوري
    suspend fun getProvidersByCategory(category: String): Result<List<Provider>> {
        return try {
            val snapshot = providersCollection
                .whereEqualTo("category", category)
                .get()
                .await()

            val providers = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Provider::class.java)?.copy(id = doc.id)
            }

            Result.success(providers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🟢 Provider واحد
    suspend fun getProviderById(id: String): Result<Provider?> {
        return try {
            val doc = providersCollection.document(id).get().await()

            val provider = doc.toObject(Provider::class.java)?.copy(id = doc.id)

            Result.success(provider)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔍 Search
    suspend fun searchProviders(query: String): Result<List<Provider>> {
        return try {
            val snapshot = providersCollection
                .orderBy("name")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .await()

            val providers = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Provider::class.java)?.copy(id = doc.id)
            }

            Result.success(providers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ⭐ Top Rated
    suspend fun getTopRatedProviders(): Result<List<Provider>> {
        return try {
            val snapshot = providersCollection
                .orderBy("rating", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .await()

            val providers = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Provider::class.java)?.copy(id = doc.id)
            }

            Result.success(providers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
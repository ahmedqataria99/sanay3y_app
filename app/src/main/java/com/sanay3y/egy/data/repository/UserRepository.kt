package com.sanay3y.egy.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.sanay3y.egy.data.model.User
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    // 🟢 إنشاء مستخدم
    suspend fun createUser(user: User): Result<Unit> {
        return try {
            val userWithId = user.copy(id = user.firebaseUid)

            usersCollection.document(user.firebaseUid)
                .set(userWithId)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🟢 جلب المستخدم
    suspend fun getUserByUid(uid: String): Result<User?> {
        return try {
            val snapshot = usersCollection.document(uid).get().await()

            val user = snapshot.toObject(User::class.java)?.copy(id = snapshot.id)

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔄 مزامنة المستخدم
    suspend fun syncUser(firebaseUid: String, name: String, email: String): Result<Unit> {
        return try {
            val doc = usersCollection.document(firebaseUid).get().await()

            if (!doc.exists()) {
                val user = User(
                    id = firebaseUid,
                    firebaseUid = firebaseUid,
                    name = name,
                    email = email
                )

                usersCollection.document(firebaseUid)
                    .set(user)
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
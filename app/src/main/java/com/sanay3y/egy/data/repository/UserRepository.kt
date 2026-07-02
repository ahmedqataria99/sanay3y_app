package com.sanay3y.egy.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.sanay3y.egy.data.model.User
import com.sanay3y.egy.data.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserRepository {

    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val usersCollection by lazy { firestore.collection("users") }


    suspend fun createUser(user: User): Result<Unit> = withContext(Dispatchers.IO) {
        try {
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
    suspend fun getUserByUid(uid: String): Result<User?> = withContext(Dispatchers.IO) {
        try {
            val snapshot = usersCollection.document(uid).get().await()

            val user = snapshot.toObject(User::class.java)?.copy(id = snapshot.id)

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔄 مزامنة المستخدم
    suspend fun syncUser(firebaseUid: String, name: String, email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val doc = usersCollection.document(firebaseUid).get().await()

            if (!doc.exists()) {
                val user = User(
                    id = firebaseUid,
                    firebaseUid = firebaseUid,
                    name = name,
                    email = email
                )

                usersCollection.document(firebaseUid)
                    .set(user, SetOptions.merge())
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🛠 تحديث بيانات المستخدم
    suspend fun updateUserProfile(user: User): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            usersCollection.document(user.firebaseUid)
                .set(user, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🛠 تحديث الدور
    suspend fun updateRole(uid: String, role: UserRole): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            usersCollection.document(uid)
                .set(mapOf("role" to role.name), SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
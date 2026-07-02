package com.sanay3y.egy.utils

import android.content.Context
import android.content.SharedPreferences
import com.sanay3y.egy.data.model.UserRole

class PreferenceManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("sanay3y_prefs", Context.MODE_PRIVATE)

    fun saveUserSession(uid: String, role: UserRole?) {
        sharedPreferences.edit().apply {
            putString("user_uid", uid)
            putString("user_role", role?.name)
            putBoolean("is_logged_in", true)
            apply()
        }
    }

    fun getUserUid(): String? = sharedPreferences.getString("user_uid", null)

    fun getUserRole(): UserRole? {
        val roleString = sharedPreferences.getString("user_role", null)
        return if (roleString != null) UserRole.valueOf(roleString) else null
    }

    fun isLoggedIn(): Boolean = sharedPreferences.getBoolean("is_logged_in", false)

    fun setSelectedLanguage(languageCode: String) {
        sharedPreferences.edit().putString("selected_language", languageCode).apply()
    }

    fun getSelectedLanguage(): String? = sharedPreferences.getString("selected_language", null)

    fun clearSession() {
        sharedPreferences.edit().clear().apply()
    }
}

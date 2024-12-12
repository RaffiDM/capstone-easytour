package com.dicoding.easytour.data.pref

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class UserPreference(private val dataStore: DataStore<Preferences>) {

    private val TAG = "UserPreference"

    suspend fun saveSession(user: UserModel) {
        dataStore.edit { preferences ->
            preferences[EMAIL_KEY] = user.email
            preferences[TOKEN_KEY] = user.token
            preferences[IS_LOGIN_KEY] = user.isLogin
            preferences[USER_ID_KEY] = user.userId
            preferences[USERNAME_KEY] = user.username// Always store userId as a String
        }
    }
    fun getSession(): Flow<UserModel> {
        return dataStore.data.map { preferences ->
            // Handle USER_ID_KEY safely
            val userId: String = try {
                preferences[USER_ID_KEY] ?: ""
            } catch (e: ClassCastException) {
                // If stored value is not a String, convert to String (e.g., from Int)
                val rawUserId = preferences.asMap().filterKeys { it.name == USER_ID_KEY.name }.values.firstOrNull()
                if (rawUserId is Int) rawUserId.toString() else ""
            }
            UserModel(
                email = preferences[EMAIL_KEY] ?: "",
                token = preferences[TOKEN_KEY] ?: "",
                userId = userId,
                username = preferences[USERNAME_KEY] ?: "",
                isLogin = preferences[IS_LOGIN_KEY] ?: false
            )
        }
    }
    suspend fun logout() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: UserPreference? = null

        private val EMAIL_KEY = stringPreferencesKey("email")
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val IS_LOGIN_KEY = booleanPreferencesKey("isLogin")
        private val HAS_SEEN_CATEGORY_KEY = booleanPreferencesKey("has_seen_category")
        private val USER_ID_KEY = stringPreferencesKey("user_id")

        fun getInstance(dataStore: DataStore<Preferences>): UserPreference {
            return INSTANCE ?: synchronized(this) {
                val instance = UserPreference(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}

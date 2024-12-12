package com.dicoding.easytour.data.pref

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingPreference private constructor(context: Context) {

    private val dataStore = context.dataStore

    companion object {
        private val THEME_KEY = booleanPreferencesKey("theme_setting")
        private val NOTIF_KEY = booleanPreferencesKey("notif_setting")

        @Volatile
        private var INSTANCE: SettingPreference? = null

        fun getInstance(context: Context): SettingPreference {
            return INSTANCE ?: synchronized(this) {
                val instance = SettingPreference(context)
                INSTANCE = instance
                instance
            }
        }
    }

    fun getThemeSetting(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[THEME_KEY] ?: false
        }
    }

    suspend fun saveThemeSetting(isDarkMode: Boolean) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = isDarkMode
        }
    }
    fun getNotifSetting(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[NOTIF_KEY] ?: false
        }
    }

    suspend fun saveNotifSetting(isNotifOn: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIF_KEY] = isNotifOn
        }
    }

}
package com.dicoding.easytour.di

import android.content.Context
import com.dicoding.easytour.data.EasyTourRepository
import com.dicoding.easytour.data.pref.SettingPreference
import com.dicoding.easytour.data.pref.UserPreference
import com.dicoding.easytour.data.pref.dataStore
import com.dicoding.easytour.retrofit.ApiConfig
import com.dicoding.easytour.room.EasyTourDatabase

object Injection {

    fun provideRepository(context: Context): EasyTourRepository {
        val pref = provideUserPreference(context)
        val apiService = ApiConfig.getApiService(context)
        val database = EasyTourDatabase.getInstance(context)
        val dao = database.easytourDao()
        val userDao = database.userDao()
        return EasyTourRepository.getInstance(pref, apiService, dao, userDao)
    }

    fun provideUserPreference(context: Context): UserPreference {
        return UserPreference.getInstance(context.dataStore)
    }
    fun provideSettingPreference(context: Context): SettingPreference {
        return SettingPreference.getInstance(context)  // Use context instead of context.dataStore
    }
}


package com.dicoding.easytour

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.easytour.data.EasyTourRepository
import com.dicoding.easytour.data.pref.SettingPreference
import com.dicoding.easytour.di.Injection
import com.dicoding.easytour.ui.setting.SettingViewModel

class SettingViewModelFactory(
    private val repository: EasyTourRepository,
    private val pref: SettingPreference
) : ViewModelProvider.NewInstanceFactory() {

    companion object {
        @Volatile
        private var INSTANCE: SettingViewModelFactory? = null

        fun getInstance(context: Context): SettingViewModelFactory {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingViewModelFactory(
                    Injection.provideRepository(context),
                    Injection.provideSettingPreference(context) // Assuming you have this method in Injection
                )
            }.also { INSTANCE = it }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SettingViewModel::class.java) -> {
                SettingViewModel(repository, pref) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

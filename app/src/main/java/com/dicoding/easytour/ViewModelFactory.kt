package com.dicoding.easytour

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.easytour.data.EasyTourRepository
import com.dicoding.easytour.data.pref.UserPreference
import com.dicoding.easytour.di.Injection
import com.dicoding.easytour.ui.category.CategoryViewModel
import com.dicoding.easytour.ui.dashboard.DashboardViewModel
import com.dicoding.easytour.ui.detail.DetailViewModel
import com.dicoding.easytour.ui.home.HomeViewModel
import com.dicoding.easytour.ui.login.LoginViewModel
import com.dicoding.easytour.ui.notifications.NotificationsViewModel
import com.dicoding.easytour.ui.register.RegisterViewModel
import com.dicoding.easytour.ui.setting.SettingViewModel

class ViewModelFactory(
    private val repository: EasyTourRepository
) : ViewModelProvider.NewInstanceFactory() {

    companion object {
        @Volatile
        private var INSTANCE: ViewModelFactory? = null

        fun getInstance(context: Context): ViewModelFactory {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ViewModelFactory(Injection.provideRepository(context))
            }.also { INSTANCE = it }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(repository) as T
            }
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel(repository) as T
            }
            modelClass.isAssignableFrom(CategoryViewModel::class.java) -> {
                CategoryViewModel(repository) as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(repository) as T
            }
            modelClass.isAssignableFrom(DetailViewModel::class.java) -> {
                DetailViewModel(repository) as T
            }
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel(repository) as T
            }
            modelClass.isAssignableFrom(NotificationsViewModel::class.java) -> {
                NotificationsViewModel(repository) as T
            }
//            modelClass.isAssignableFrom(SettingViewModel::class.java) -> {
//                SettingViewModel(repository) as T
//            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}


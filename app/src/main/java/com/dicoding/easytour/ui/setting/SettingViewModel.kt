package com.dicoding.easytour.ui.setting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.dicoding.easytour.data.EasyTourRepository
import com.dicoding.easytour.data.ResultState
import com.dicoding.easytour.data.pref.SettingPreference

import com.dicoding.easytour.data.pref.UserModel
import kotlinx.coroutines.launch

class SettingViewModel(private val repository: EasyTourRepository,private val pref: SettingPreference ) : ViewModel() {

    private val _logoutStatus = MutableLiveData<Boolean>()
    val logoutStatus: LiveData<Boolean> get() = _logoutStatus

    val user: LiveData<ResultState<UserModel>> = liveData {
        emit(ResultState.Loading)
        try {
            repository.getSession().collect { userModel ->
                emit(ResultState.Success(userModel))
            }
        } catch (exception: Exception) {
            emit(ResultState.Error(exception.localizedMessage ?: "Unknown error"))
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _logoutStatus.postValue(true) // Notify that logout is successful
        }
    }
    fun getThemeSettings(): LiveData<Boolean> {
        return pref.getThemeSetting().asLiveData()
    }
    fun saveThemeSetting(isDarkModeActive: Boolean) {
        viewModelScope.launch {
            pref.saveThemeSetting(isDarkModeActive)
        }
    }

    fun getNotificationSetting(): LiveData<Boolean> {
        return pref.getNotifSetting().asLiveData()
    }

    fun saveNotificationSetting(isNotifOn: Boolean) {
        viewModelScope.launch {
            pref.saveNotifSetting(isNotifOn)
        }
    }
}


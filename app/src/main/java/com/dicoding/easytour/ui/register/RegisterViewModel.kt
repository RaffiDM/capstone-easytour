package com.dicoding.easytour.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.easytour.data.EasyTourRepository
import com.dicoding.easytour.data.ResultState
import com.dicoding.easytour.entity.UserEntity
import com.dicoding.easytour.response.RegisterResponse
import kotlinx.coroutines.launch

class RegisterViewModel(private val repository: EasyTourRepository) : ViewModel() {
    private val _registerResult = MutableLiveData<ResultState<RegisterResponse>>()
    val registerResult: LiveData<ResultState<RegisterResponse>> = _registerResult
    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _registerResult.value = repository.register(name, email, password)
        }
    }

    fun getUserById(id: String): LiveData<UserEntity?> {
        val userLiveData = MutableLiveData<UserEntity?>()
        viewModelScope.launch {
            val user = repository.userDao.getUserById(id) // Ensure that this method can accept a String ID
            userLiveData.postValue(user)
        }
        return userLiveData
    }

}
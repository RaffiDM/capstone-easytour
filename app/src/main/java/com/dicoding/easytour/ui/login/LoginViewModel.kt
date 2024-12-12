package com.dicoding.easytour.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.easytour.data.EasyTourRepository
import com.dicoding.easytour.data.ResultState
import com.dicoding.easytour.data.pref.UserModel
import com.dicoding.easytour.response.LoginResponse
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: EasyTourRepository) : ViewModel() {

    private val _loginResult = MutableLiveData<ResultState<LoginResponse>>()
    val loginResult: LiveData<ResultState<LoginResponse>> = _loginResult

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginResult.value = ResultState.Loading

            when (val result = repository.login(email, password)) {
                is ResultState.Success -> {
                    // Setelah login berhasil, dapatkan userID dan simpan session

                    val loginResponse = result.data
                    loginResponse.userID?.let { userId ->
                        val username = loginResponse.username ?: email.split("@")[0]
                        // Simpan session dengan userID
                        val userModel = UserModel(
                            email = email,
                            token = loginResponse.username ?: "",
                            userId = userId.toString(), // Menyimpan userId
                            isLogin = true,
                            username = username // Unresolved reference: username

                        )
                        repository.saveSession(userModel)
                    }
                    _loginResult.value = ResultState.Success(result.data)
                }
                is ResultState.Error -> {
                    _loginResult.value = result
                }
                ResultState.Loading -> {}
            }
        }
    }
}

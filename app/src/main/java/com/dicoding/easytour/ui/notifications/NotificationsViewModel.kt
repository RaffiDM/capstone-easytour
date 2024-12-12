package com.dicoding.easytour.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.easytour.data.EasyTourRepository
import com.dicoding.easytour.data.ResultState
import com.dicoding.easytour.entity.HomeEntity
import com.dicoding.easytour.request.FilterRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

import kotlinx.coroutines.launch

class NotificationsViewModel(private val repository: EasyTourRepository) : ViewModel() {

    private val _state = MutableLiveData<ResultState<List<HomeEntity>>>()
    val state: LiveData<ResultState<List<HomeEntity>>> = _state

    private var debounceJob: Job? = null

    fun searchPlace(query: String) {
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(300)
            _state.value = ResultState.Loading
            try {
                val results = repository.searchAndSaveResults(query)
                _state.value = ResultState.Success(results)
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    _state.value = ResultState.Error(e.localizedMessage ?: "An error occurred")
                }
            }
        }
    }

    fun filterPlace(request: FilterRequest) {
        viewModelScope.launch {
            _state.value = ResultState.Loading
            try {
                val results = repository.filter(request)
                _state.value = ResultState.Success(results)
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    _state.value = ResultState.Error(e.localizedMessage ?: "An error occurred")
                }
            }
        }
    }
}

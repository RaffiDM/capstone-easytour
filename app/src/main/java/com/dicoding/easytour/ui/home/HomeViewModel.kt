package com.dicoding.easytour.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.easytour.data.EasyTourRepository
import com.dicoding.easytour.entity.HomeEntity
import com.dicoding.easytour.data.ResultState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class HomeViewModel(private val repository: EasyTourRepository) : ViewModel() {

    private val _homeData = MutableLiveData<ResultState<List<HomeEntity>>>()
    val homeData: LiveData<ResultState<List<HomeEntity>>> get() = _homeData
    private val _state = MutableLiveData<ResultState<List<HomeEntity>>>()
    val state: LiveData<ResultState<List<HomeEntity>>> = _state
    private var debounceJob: Job? = null

    private var cachedData: List<HomeEntity>? = null

    fun getHomeData(email: String) {
        if (cachedData != null) {
            // If data is already cached, return it immediately
            _homeData.value = ResultState.Success(cachedData!!)
            return
        }

        viewModelScope.launch {
            _homeData.value = ResultState.Loading
            repository.getHome(email).observeForever { result ->
                _homeData.value = result
                if (result is ResultState.Success) {
                    cachedData = result.data // Cache the data
                }
            }
        }
    }
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


}
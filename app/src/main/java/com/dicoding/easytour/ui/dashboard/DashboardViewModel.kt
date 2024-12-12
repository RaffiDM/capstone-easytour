package com.dicoding.easytour.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.easytour.data.EasyTourRepository
import com.dicoding.easytour.data.ResultState
import com.dicoding.easytour.entity.HomeEntity
import kotlinx.coroutines.launch

class DashboardViewModel(private val repository: EasyTourRepository) : ViewModel() {

    private val _favoritePlaces = MutableLiveData<ResultState<List<HomeEntity>>>()
    val favoritePlaces: LiveData<ResultState<List<HomeEntity>>> = _favoritePlaces

    fun fetchFavoritePlaces() {
        _favoritePlaces.value = ResultState.Loading
        viewModelScope.launch {
            try {
                repository.getFavoritePlaces().observeForever { favoritePlacesData ->
                    _favoritePlaces.value = ResultState.Success(favoritePlacesData)
                }
            } catch (e: Exception) {
                _favoritePlaces.value = ResultState.Error(e.localizedMessage ?: "An error occurred during fetch")
            }
        }
    }

    // Method to remove a favorite
    suspend fun removeFavorite(homeEntity: HomeEntity) {
        repository.removeFavorite(homeEntity)
    }

    fun getHomeData(userId: String): LiveData<ResultState<List<HomeEntity>>> {
        return repository.getHome(userId)
    }
}

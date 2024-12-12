package com.dicoding.easytour.ui.detail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.dicoding.easytour.data.EasyTourRepository
import com.dicoding.easytour.data.ResultState

import com.dicoding.easytour.entity.HomeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailViewModel(private val repository: EasyTourRepository) : ViewModel() {

    fun getHomeData(userID: String): LiveData<ResultState<List<HomeEntity>>> {
        return repository.getHome(userID)
    }
    fun updateFavoriteStatus(name: String, isFavorite: Int) {
        if (name.isEmpty()) {
            Log.e("DetailViewModel", "Error: Name is empty. Cannot update favorite status.")
            return
        }
        viewModelScope.launch {
            repository.updateFavoriteStatus(name, isFavorite)
        }
    }


    fun isFavorited(name: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val isFavorite = repository.isFavorited(name)
            callback(isFavorite) // isFavorited returns 1 or 0
        }
    }
//

//

}

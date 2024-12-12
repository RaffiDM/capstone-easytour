package com.dicoding.easytour.ui.shared

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dicoding.easytour.entity.HomeEntity

class SharedViewModel : ViewModel() {
    private val _sharedData = MutableLiveData<List<HomeEntity>>()
    val sharedData: LiveData<List<HomeEntity>> get() = _sharedData

    fun updateData(data: List<HomeEntity>) {
        _sharedData.value = data
    }
}

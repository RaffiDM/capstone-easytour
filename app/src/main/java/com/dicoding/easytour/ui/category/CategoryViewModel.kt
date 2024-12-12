package com.dicoding.easytour.ui.category

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.easytour.data.EasyTourRepository
import com.dicoding.easytour.data.pref.CategoryUI
import com.dicoding.easytour.retrofit.CategoryRequest
import kotlinx.coroutines.launch

class CategoryViewModel(private val repository: EasyTourRepository) : ViewModel() {

    private val _categories = MutableLiveData<List<CategoryUI>>()
    val categories: LiveData<List<CategoryUI>> = _categories

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun getCategories(userId: String?) {
        viewModelScope.launch {
            try {
                Log.d("CategoryViewModel", "Fetching categories for userId: $userId")
                val response = repository.getCategories(userId)
                if (response.error == false) {
                    val preferences = response.categoryPreferences
                    val categoryList = mutableListOf<CategoryUI>()

                    categoryList.add(CategoryUI("Taman Hiburan", preferences?.tamanHiburan == true))
                    categoryList.add(CategoryUI("Pusat Perbelanjaan", preferences?.pusatPerbelanjaan == true))
                    categoryList.add(CategoryUI("Budaya", preferences?.budaya == true))
                    categoryList.add(CategoryUI("Bahari", preferences?.bahari == true))
                    categoryList.add(CategoryUI("Cagar Alam", preferences?.cagarAlam == true))
                    categoryList.add(CategoryUI("Tempat Ibadah", preferences?.tempatIbadah == true))

                    _categories.value = categoryList
                } else {
                    _error.value = response.message ?: "Unknown error occurred"
                }
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error fetching categories: ${e.localizedMessage}")
                _error.value = e.localizedMessage ?: "An error occurred"
            }
        }
    }

    fun sendCategoryPreferences(categoryRequest: CategoryRequest) {
        viewModelScope.launch {
            try {
                Log.d("CategoryViewModel", "Sending category preferences: $categoryRequest")
                val response = repository.sendCategoryPreferences(categoryRequest)
                if (response.error == false) {
                    Log.d("CategoryViewModel", "Category preferences updated successfully.")
                } else {
                    _error.value = response.message ?: "Unknown error occurred"
                }
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error updating category preferences: ${e.localizedMessage}")
                _error.value = e.localizedMessage ?: "An error occurred"
            }
        }
    }
}

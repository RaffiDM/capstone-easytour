package com.dicoding.easytour.ui.category

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.easytour.CategoryAdapter
import com.dicoding.easytour.MainActivity
import com.dicoding.easytour.ViewModelFactory
import com.dicoding.easytour.data.pref.CategoryUI
import com.dicoding.easytour.databinding.ActivityCategoryBinding
import com.dicoding.easytour.di.Injection
import com.dicoding.easytour.retrofit.CategoryRequest

class CategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryBinding
    private lateinit var adapter: CategoryAdapter
    private lateinit var viewModel: CategoryViewModel

    private val selectedCategories = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val userId = sharedPreferences.getInt("userId", 0)

        if (userId != null) {
            Log.d("CategoryActivity", "UserId found: $userId")
            setupViewModel()
            observeViewModel()

            // Fetch categories using valid userId
            viewModel.getCategories(userId.toString())

            binding.btnDone.setOnClickListener {
                val categoriesMap = mutableMapOf<String, Boolean>()
                categoriesMap["TamanHiburan"] = selectedCategories.contains("Taman Hiburan")
                categoriesMap["Budaya"] = selectedCategories.contains("Budaya")
                categoriesMap["Bahari"] = selectedCategories.contains("Bahari")
                categoriesMap["CagarAlam"] = selectedCategories.contains("Cagar Alam")
                categoriesMap["PusatPerbelanjaan"] = selectedCategories.contains("Pusat Perbelanjaan")
                categoriesMap["TempatIbadah"] = selectedCategories.contains("Tempat Ibadah")

                val categoryRequest = CategoryRequest(
                    userID = userId.toString(), // userId is now a String
                    TamanHiburan = categoriesMap["TamanHiburan"] ?: false,
                    Budaya = categoriesMap["Budaya"] ?: false,
                    Bahari = categoriesMap["Bahari"] ?: false,
                    CagarAlam = categoriesMap["CagarAlam"] ?: false,
                    PusatPerbelanjaan = categoriesMap["PusatPerbelanjaan"] ?: false,
                    TempatIbadah = categoriesMap["TempatIbadah"] ?: false
                )

                viewModel.sendCategoryPreferences(categoryRequest)

                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("selectedCategories", ArrayList(selectedCategories))
                }
                startActivity(intent)
                finish()
            }
        } else {
            Toast.makeText(this, "UserId not found, please log in first", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupViewModel() {
        val repository = Injection.provideRepository(this)
        val viewModelFactory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(CategoryViewModel::class.java)
    }

    private fun observeViewModel() {
        viewModel.categories.observe(this) { categories ->
            Log.d("CategoryActivity", "Categories received: $categories")
            setupRecyclerView(categories)
        }

        viewModel.error.observe(this) { errorMessage ->
            Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView(categories: List<CategoryUI>) {
        adapter = CategoryAdapter(categories) { category, isChecked ->
            if (isChecked) {
                selectedCategories.add(category.title)
            } else {
                selectedCategories.remove(category.title)
            }
        }
        binding.rvCategories.adapter = adapter
        binding.rvCategories.layoutManager = LinearLayoutManager(this)
    }
}

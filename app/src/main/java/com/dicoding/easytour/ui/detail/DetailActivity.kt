package com.dicoding.easytour.ui.detail

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.easytour.R
import com.dicoding.easytour.ViewModelFactory
import com.dicoding.easytour.data.ResultState
import com.dicoding.easytour.data.pref.UserPreference
import com.dicoding.easytour.data.pref.dataStore
import com.dicoding.easytour.entity.HomeEntity
import com.dicoding.easytour.ui.adapter.HorizontalAdapter
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {
    private lateinit var ivDetailPhoto: ImageView
    private lateinit var tvDetailName: TextView
    private lateinit var tvDetailDescription: TextView
    private lateinit var tvDetailCity: TextView
    private lateinit var tvPrice: TextView
    private lateinit var ivFav: ImageView
    private lateinit var tvRating: TextView // Rating TextView
    private lateinit var horizontalAdapter: HorizontalAdapter
    private lateinit var userPreference: UserPreference
    private var isBookmarked: Boolean = false
    private lateinit var homeEntity: HomeEntity

    private val viewModel: DetailViewModel by viewModels {
        val factory = ViewModelFactory.getInstance(applicationContext)
        factory
    }
    companion object {
        const val EXTRA_PLACE_ID = "extra_place_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // Initialize UserPreference
        userPreference = UserPreference.getInstance(dataStore)

        // Initialize Views
        ivDetailPhoto = findViewById(R.id.iv_destination)
        tvDetailName = findViewById(R.id.tv_name)
        tvDetailDescription = findViewById(R.id.tv_description)
        tvDetailCity = findViewById(R.id.tv_city)
        tvPrice = findViewById(R.id.tv_price)
        ivFav = findViewById(R.id.iv_favorite)
        tvRating = findViewById(R.id.tv_rating) // Initialize Rating TextView

        // Initialize HorizontalAdapter
        horizontalAdapter = HorizontalAdapter { item ->
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra("EXTRA_ID", item.iD)
                putExtra("EXTRA_PHOTO_URL", item.imageUrl)
                putExtra("EXTRA_NAME", item.name)
                putExtra("EXTRA_RATING", item.rating)
                putExtra("EXTRA_PRICE", item.price)
                putExtra("EXTRA_DESCRIPTION", item.description)
                putExtra("EXTRA_CITY", item.city)
            }
            startActivity(intent)
        }

        // Setup RecyclerView
        findViewById<RecyclerView>(R.id.rv_detail).apply {
            layoutManager = LinearLayoutManager(this@DetailActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = horizontalAdapter
        }

        // Retrieve and set data
        val id = intent.getStringExtra("EXTRA_ID")
        val photoUrl = intent.getStringExtra("EXTRA_PHOTO_URL")
        val name = intent.getStringExtra("EXTRA_NAME") ?: "Unknown Name"
        val description = intent.getStringExtra("EXTRA_DESCRIPTION") ?: "No Description"
        val city = intent.getStringExtra("EXTRA_CITY") ?: "Unknown City"
        val price = intent.getStringExtra("EXTRA_PRICE") ?: "Unknown Price"
        val rating = intent.getFloatExtra("EXTRA_RATING", 0.0f) // Default to 0.0f if missing

        // Log received data for debugging
        Log.d("DetailActivity", "Received data -> Photo URL: $photoUrl, Name: $name, Description: $description, City: $city, Price: $price, Rating: $rating")

        // Initialize homeEntity with intent data
        homeEntity = HomeEntity(
            iD = id ?: "", // Default to an empty string if id is null
            name = name ?: "Unknown Name", // Default to "Unknown Name" if name is null
            description = description ?: "No Description", // Default to "No Description" if description is null
            city = city ?: "Unknown City", // Default to "Unknown City" if city is null
            price = price ?: "Unknown Price", // Default to "Unknown Price" if price is null
            category = "Unknown Category",
            categoryMatch = false,
            explanation = "No Explanation",
            rating = rating,
            imageUrl = photoUrl ?: "" // Default to an empty string or a placeholder if photoUrl is null
        )


        // Load photo using Glide
        if (!photoUrl.isNullOrEmpty()) {
            Glide.with(this).load(photoUrl).into(ivDetailPhoto)
        } else {
            Glide.with(this).load(R.drawable.holder).into(ivDetailPhoto) // Placeholder image
        }

        // Set UI data
        tvDetailName.text = name
        tvDetailDescription.text = description
        tvDetailCity.text = city
        tvPrice.text = "Price: $price"
        tvRating.text = "Rating: $rating" // Set Rating properly

        // Observe data for horizontal RecyclerView
        observeDetailData()
        // Check if this place is already favorited
        viewModel.isFavorited(homeEntity.name ?: "") { isFavorite ->
            isBookmarked = isFavorite
            updateFavoriteIcon(isFavorite)
        }


        // Handle favorite click
        ivFav.setOnClickListener {
            isBookmarked = !isBookmarked
            updateFavoriteIcon(isBookmarked)
            val favoriteStatus = if (isBookmarked) 1 else 0
            Log.d("detail", "isbookmarked= {$isBookmarked}, {$favoriteStatus}")
            viewModel.updateFavoriteStatus(homeEntity.name ?: "", favoriteStatus)
            Toast.makeText(this, if (isBookmarked) "Added to Favorites" else "Removed from Favorites", Toast.LENGTH_SHORT).show()
        }

    }


    private fun observeDetailData() {
        lifecycleScope.launch {
            userPreference.getSession().collect { user ->
                val userID = user.userId
                if (userID.isNotEmpty()) {
                    viewModel.getHomeData(userID).observe(this@DetailActivity) { result ->
                        when (result) {
                            is ResultState.Loading -> {
                                // Show loading if needed
                            }
                            is ResultState.Success -> {
                                horizontalAdapter.submitList(result.data.take(5)) // Show up to 5 items
                            }
                            is ResultState.Error -> {
                                Toast.makeText(
                                    this@DetailActivity,
                                    "Error loading data: ${result.error}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(this@DetailActivity, "Please log in first", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun updateFavoriteIcon(isFavorite: Boolean) {
        if (isFavorite) {
            ivFav.setImageResource(R.drawable.bookmark)
        } else {
            ivFav.setImageResource(R.drawable.bookmark_border) // Inactive favorite icon
        }
    }
}

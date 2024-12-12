package com.dicoding.easytour.request

import com.google.gson.annotations.SerializedName

data class FilterRequest(
    val userID: String,
    val city: String,
    @SerializedName("price_min")
    val priceMin: Int,
    @SerializedName("price_max")
    val priceMax: Int,
    @SerializedName("rating_min")
    val ratingMin: Float,
    @SerializedName("rating_max")
    val ratingMax: Float,
    val sorting: String
)
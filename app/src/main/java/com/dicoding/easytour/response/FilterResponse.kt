package com.dicoding.easytour.response

import com.google.gson.annotations.SerializedName

data class FilterResponse(
    val code: Int,
    val data: List<FilterItem>
)

data class FilterItem(
    @SerializedName("ID")
    val id: Int,
    @SerializedName("category")
    val category: String,
    @SerializedName("category_match")
    val categoryMatch: Boolean,
    @SerializedName("city")
    val city: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("explanation")
    val explanation: String,
    @SerializedName("image_url")
    val imageUrl: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("price")
    val price: String,
    @SerializedName("rating")
    val rating: Double
)
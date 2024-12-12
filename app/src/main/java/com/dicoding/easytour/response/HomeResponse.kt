package com.dicoding.easytour.response

import com.google.gson.annotations.SerializedName

data class HomeResponse(

	@field:SerializedName("code")
	val code: Int? = null,

	@field:SerializedName("data")
	val data: List<DataItem?>? = null
)

data class DataItem(

	@field:SerializedName("city")
	val city: String? = null,

	@field:SerializedName("image_url")
	val imageUrl: String? = null,

	@field:SerializedName("price")
	val price: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("rating")
	val rating: Float? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("category_match")
	val categoryMatch: Boolean? = null,

	@field:SerializedName("ID")
	val iD: String? = null,

	@field:SerializedName("category")
	val category: String? = null,

	@field:SerializedName("explanation")
	val explanation: String? = null
)

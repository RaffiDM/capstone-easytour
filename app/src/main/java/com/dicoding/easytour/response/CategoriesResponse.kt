package com.dicoding.easytour.response

import com.google.gson.annotations.SerializedName

data class CategoriesResponse(

	@field:SerializedName("category_preferences")
	val categoryPreferences: CategoryPreferences? = null,

	@field:SerializedName("error")
	val error: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)

data class CategoryPreferences(

	@field:SerializedName("TamanHiburan")
	val tamanHiburan: Boolean? = null,

	@field:SerializedName("PusatPerbelanjaan")
	val pusatPerbelanjaan: Boolean? = null,

	@field:SerializedName("Budaya")
	val budaya: Boolean? = null,

	@field:SerializedName("Bahari")
	val bahari: Boolean? = null,

	@field:SerializedName("CagarAlam")
	val cagarAlam: Boolean? = null,

	@field:SerializedName("TempatIbadah")
	val tempatIbadah: Boolean? = null
)

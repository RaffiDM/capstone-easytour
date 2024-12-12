package com.dicoding.easytour.response

import com.google.gson.annotations.SerializedName

data class RecommendationResponse(

	@field:SerializedName("code")
	val code: Int? = null,

	@field:SerializedName("data")
	val data: Data? = null
)

data class CategoryProbabilities(

	@field:SerializedName("Taman Hiburan")
	val tamanHiburan: Any? = null,


	@field:SerializedName("Tempat Ibadah")
	val tempatIbadah: Any? = null,

	@field:SerializedName("Budaya")
	val budaya: Any? = null,

	@field:SerializedName("Bahari")
	val bahari: Any? = null,

	@field:SerializedName("Pusat Perbelanjaan")
	val pusatPerbelanjaan: Any? = null,

	@field:SerializedName("Cagar Alam")
	val cagarAlam: Any? = null
)

data class Data(

	@field:SerializedName("category_probabilities")
	val categoryProbabilities: CategoryProbabilities? = null,

	@field:SerializedName("predicted_category")
	val predictedCategory: String? = null,

	@field:SerializedName("recommendations")
	val recommendations: List<RecommendationsItem?>? = null
)

data class RecommendationsItem(

	@field:SerializedName("city")
	val city: String? = null,
	@field:SerializedName("rating")
	val rating: Any? = null,
	@field:SerializedName("image_url")
	val imageUrl: String? = null,

	@field:SerializedName("price")
	val price: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("category")
	val category: String? = null,

	@field:SerializedName("explanation")
	val explanation: String? = null,

	@field:SerializedName("nn_similarity_score")
	val nnSimilarityScore: Any? = null
)

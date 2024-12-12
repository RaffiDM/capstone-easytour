package com.dicoding.easytour.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recommendations")
data class RecommendationItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String?,
    val description: String?,
    val price: String?,
    val priceCategory: String?,
    val city: String?,
    val category: String?,
    val categoryMatch: Boolean?,
    val explanation: String?,
    val finalScore: String?, // Converted from Any for simplicity
    val similarityScore: String? // Converted from Any for simplicity
)

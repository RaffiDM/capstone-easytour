package com.dicoding.easytour.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "home_data")
data class HomeEntity(
    @ColumnInfo(name = "id") val iD: String, // Made non-nullable
    @ColumnInfo(name = "city") val city: String?,
    @ColumnInfo(name = "price") val price: String?,
    @PrimaryKey
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "rating") val rating: Float?,
    @ColumnInfo(name = "image_url") val imageUrl: String?,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "category_match") val categoryMatch: Boolean?,
    @ColumnInfo(name = "category") val category: String?,
    @ColumnInfo(name = "explanation") val explanation: String?,
    @ColumnInfo(name = "is_favorite") var isFavorite: Int = 0 // 0 = not favorite, 1 = favorite
)




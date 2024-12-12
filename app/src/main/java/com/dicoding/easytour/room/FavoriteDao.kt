package com.dicoding.easytour.room

import androidx.room.*
//import com.dicoding.easytour.entity.FavoriteEntity

@Dao
interface FavoriteDao {

//    // Insert a new favorite
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertFavorite(favorite: FavoriteEntity)
//
//    // Delete a favorite by entity
//    @Delete
//    suspend fun deleteFavorite(favorite: FavoriteEntity)
//
//    // Delete a favorite by home ID
//    @Query("DELETE FROM favorite_data WHERE home_id = :homeId")
//    suspend fun deleteFavoriteByHomeId(homeId: Int)
//
//    // Retrieve all favorites
//    @Query("SELECT * FROM favorite_data")
//    suspend fun getAllFavorites(): List<FavoriteEntity>
//
//    // Retrieve a favorite by ID
//    @Query("SELECT * FROM favorite_data WHERE id = :id")
//    suspend fun getFavoriteById(id: Int): FavoriteEntity?
//
//    // Check if a place is favorited by home ID
//    @Query("SELECT EXISTS(SELECT 1 FROM favorite_data WHERE home_id = :homeId)")
//    suspend fun isFavorite(homeId: Int): Boolean
}

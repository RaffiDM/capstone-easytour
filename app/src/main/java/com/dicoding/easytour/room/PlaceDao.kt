package com.dicoding.easytour.room

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dicoding.easytour.entity.HomeEntity
import com.dicoding.easytour.entity.RecommendationItemEntity

@Dao
interface PlaceDao {
    // Insert a single recommendation
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecommendation(homeData: HomeEntity)


    // Get all recommendations (ensure type matches HomeEntity)
    @Query("SELECT * FROM home_data")
    suspend fun getAllRecommendations(): List<HomeEntity>

    // Delete a single event
    @Delete
    suspend fun delete(event: HomeEntity)

//    // Get all favorited items as LiveData
//    @Query("SELECT * FROM home_data WHERE is_favorite = 1")
//    fun getFavorited(): LiveData<List<HomeEntity>>
//
//    // Check if an item is favorited by ID
//    @Query("SELECT EXISTS(SELECT * FROM home_data WHERE name = :name AND is_favorite = 1)")
//    suspend fun isFavorited(name: String): Boolean

    // Update event (e.g., isFavorite status)
    @Update
    suspend fun updateEvents(event: HomeEntity)

    @Update
    suspend fun updateFavorite(homeEntity: HomeEntity)

    // Insert a single event (with IGNORE strategy for duplicates)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTour(event: HomeEntity)

    // Insert multiple events
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(events: List<HomeEntity>): List<Long>
    @Query("SELECT * FROM home_data WHERE is_favorite = 1")
    fun getFavoritedPlaces(): LiveData<List<HomeEntity>>

    @Query("UPDATE home_data SET is_favorite = :isFavorite WHERE name = :name")
    suspend fun updateFavoriteStatus(name: String, isFavorite: Int): Int

    @Query("SELECT * FROM home_data WHERE name= :name limit 1")
    suspend fun getFlashbyName(name: String): HomeEntity

    @Query("SELECT is_favorite FROM home_data WHERE name = :name")
    suspend fun isFavorited(name: String): Int

}

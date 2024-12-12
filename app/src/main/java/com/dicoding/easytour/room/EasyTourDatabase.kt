package com.dicoding.easytour.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dicoding.easytour.entity.HomeEntity
import com.dicoding.easytour.entity.RecommendationItemEntity
import com.dicoding.easytour.entity.UserEntity

@Database(entities = [HomeEntity::class, UserEntity::class], version = 4, exportSchema = false)
abstract class EasyTourDatabase : RoomDatabase() {

    abstract fun easytourDao(): PlaceDao
    abstract fun userDao(): UserDao
    abstract fun favoriteDao(): FavoriteDao // Tambahkan ini


    companion object {
        @Volatile
        private var INSTANCE: EasyTourDatabase? = null

        fun getInstance(context: Context): EasyTourDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EasyTourDatabase::class.java,
                    "easytour_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}

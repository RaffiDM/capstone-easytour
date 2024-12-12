package com.dicoding.easytour.room

import androidx.room.*
import com.dicoding.easytour.entity.UserEntity

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("SELECT * FROM user_data WHERE id = :id")
    suspend fun getUserById(id: kotlin.String): UserEntity?

    @Query("SELECT * FROM user_data")
    suspend fun getAllUsers(): List<UserEntity>
}

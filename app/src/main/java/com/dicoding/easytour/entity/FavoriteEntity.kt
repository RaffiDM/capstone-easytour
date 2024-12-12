package com.dicoding.easytour.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

//@Entity(
//    tableName = "favorite_data",
//    foreignKeys = [ForeignKey(
//        entity = HomeEntity::class,
//        parentColumns = ["id"],
//        childColumns = ["home_id"],
//        onDelete = ForeignKey.CASCADE
//    )]
//)
//data class FavoriteEntity(
//    @PrimaryKey(autoGenerate = true)
//    @ColumnInfo(name = "id") val id: Int = 0,
//    @ColumnInfo(name = "home_id") val homeId: Int,
//    @ColumnInfo(name = "date_added") val dateAdded: Long = System.currentTimeMillis()
//)

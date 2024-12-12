package com.dicoding.easytour.data.pref

data class UserModel(
    val email: String,
    val username: String,
    val token: String,
    val userId: String, // Menambahkan userId
    val isLogin: Boolean
)
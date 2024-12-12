package com.dicoding.easytour.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dicoding.easytour.MainActivity
import com.dicoding.easytour.data.pref.UserPreference
import com.dicoding.easytour.data.pref.dataStore
import com.dicoding.easytour.ui.home.HomeFragment
import com.dicoding.easytour.ui.login.LoginActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            val userPreference = UserPreference.getInstance(dataStore)
            val user = userPreference.getSession().first()
            Log.d("splash", "User isLogin: ${user.isLogin}")
            if (user.isLogin) {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            } else {
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            }
            finish()
        }
    }
}
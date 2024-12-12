package com.dicoding.easytour.ui.register

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.dicoding.easytour.ViewModelFactory
import com.dicoding.easytour.data.ResultState
import com.dicoding.easytour.databinding.ActivityRegisterBinding
import com.dicoding.easytour.ui.category.CategoryActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel
        val factory = ViewModelFactory.getInstance(this)
        viewModel = ViewModelProvider(this, factory)[RegisterViewModel::class.java]

        // Observe registration result
        viewModel.registerResult.observe(this) { result ->
            when (result) {
                is ResultState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val userId = result.data.userID
                    if (userId != null) {
                        // Ensure userId is an integer and save it to SharedPreferences
                        val userIdInt = userId  // Convert userId to Int if possible
                        if (userIdInt != null) {
                            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
                            sharedPreferences.edit().putInt("userId", userIdInt).apply()
                            Log.d("RegisterActivity", "userId saved: $userIdInt")

                            // Navigate to CategoryActivity
                            startActivity(Intent(this, CategoryActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Registration failed: userId is not an integer", Toast.LENGTH_SHORT).show()
                            Log.e("RegisterActivity", "userId is not an integer")
                        }
                    } else {
                        Toast.makeText(this, "Registration failed: userId is null", Toast.LENGTH_SHORT).show()
                        Log.e("RegisterActivity", "userId is null")
                    }
                }
                is ResultState.Error -> {
                    Log.d("kjdfsjhdjk", "register: ${result.error}")
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Registration failed: ${result.error}", Toast.LENGTH_SHORT).show()
                }
                ResultState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }

        // Handle sign-up button click
        binding.signupButton.setOnClickListener {
            val name = binding.edRegisterName.text.toString()
            val email = binding.edRegisterEmail.text.toString()
            val password = binding.edRegisterPassword.text.toString()

            if (name.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                viewModel.register(name, email, password)
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        playAnimation()
    }

    private fun playAnimation() {
        val title = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 1f).setDuration(500)
        //val nameTextView = ObjectAnimator.ofFloat(binding.nameTextView, View.ALPHA, 1f).setDuration(500)
        val nameEditTextLayout = ObjectAnimator.ofFloat(binding.nameEditTextLayout, View.ALPHA, 1f).setDuration(500)
        //val emailTextView = ObjectAnimator.ofFloat(binding.emailTextView, View.ALPHA, 1f).setDuration(500)
        val emailEditTextLayout = ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1f).setDuration(500)
        //val passwordTextView = ObjectAnimator.ofFloat(binding.passwordTextView, View.ALPHA, 1f).setDuration(500)
        val passwordEditTextLayout = ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 1f).setDuration(500)
        val signup = ObjectAnimator.ofFloat(binding.signupButton, View.ALPHA, 1f).setDuration(500)

        AnimatorSet().apply {
            playSequentially(
                title,
                nameEditTextLayout,
                emailEditTextLayout,
                passwordEditTextLayout,
                signup
            )
            startDelay = 100
        }.start()
    }
}

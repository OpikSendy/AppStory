@file:Suppress("DEPRECATION")

package com.example.appstory.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.appstory.MainActivity
import com.example.appstory.R
import com.example.appstory.data.request.LoginResponse
import com.example.appstory.databinding.ActivityLoginBinding
import com.example.appstory.ui.AuthViewModel
import com.example.appstory.ui.password.ForgotPasswordActivity
import com.example.appstory.ui.register.RegisterActivity
import com.example.appstory.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("LoginActivity", "onCreate called")

        checkSession()

        authViewModel.loginStatus.observe(this) { resource ->
            handleLoginStatus(resource)
        }

        val logoImageView: ImageView = findViewById(R.id.logoImageView)
        val animation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.logo_animation)
        logoImageView.startAnimation(animation)

        binding.btnLogin.setOnClickListener {
            attemptLogin()
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.btnForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun checkSession() {
        val preferences = getSharedPreferences("session", Context.MODE_PRIVATE)
        authViewModel.authState.observe(this) {
            if (preferences.getBoolean("rememberMe", false)) {
                val token = preferences.getString("auth_token", null)
                if (token != null) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }

        }
    }

    private fun handleLoginStatus(resource: Resource<LoginResponse>) {
        when (resource) {
            is Resource.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
                toggleInput(false)
            }
            is Resource.Success -> {
                binding.progressBar.visibility = View.GONE
                toggleInput(true)
                val loginResponse = resource.data
                if (loginResponse != null && !loginResponse.error) {
                    saveSession(binding.edLoginEmail.text.toString(), binding.checkboxRememberMe.isChecked)
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, loginResponse?.message ?: "Login gagal", Toast.LENGTH_SHORT).show()
                }
            }
            is Resource.Error -> {
                binding.progressBar.visibility = View.GONE
                toggleInput(true)
                Toast.makeText(this, resource.message ?: "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun attemptLogin() {
        val email = binding.edLoginEmail.text.toString().trim()
        val password = binding.edLoginPassword.text.toString().trim()

        if (isValidEmail(email) && password.length >= 8) {
            authViewModel.login(email, password)
        } else {
            if (!isValidEmail(email)) {
                binding.edLoginEmail.error = "Format email tidak valid"
            }
        }
    }

    private fun isValidEmail(email: CharSequence): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun saveSession(email: String, rememberMe: Boolean) {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val preferences = EncryptedSharedPreferences.create(
            "session",
            masterKeyAlias,
            applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        preferences.edit()
            .putString("email", email)
            .putBoolean("rememberMe", rememberMe)
            .apply()
    }

    private fun toggleInput(isEnabled: Boolean) {
        binding.edLoginEmail.isEnabled = isEnabled
        binding.edLoginPassword.isEnabled = isEnabled
        binding.btnLogin.isEnabled = isEnabled
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}
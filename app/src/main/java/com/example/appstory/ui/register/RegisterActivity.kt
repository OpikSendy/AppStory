package com.example.appstory.ui.register

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.appstory.R
import com.example.appstory.databinding.ActivityRegisterBinding
import com.example.appstory.ui.AuthViewModel
import com.example.appstory.ui.login.LoginActivity
import com.example.appstory.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val logoImageView: ImageView = findViewById(R.id.logoImageView)
        val animation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.logo_animation)
        logoImageView.startAnimation(animation)

        observeRegistrationStatus()

        checkRegistrationStatus()

        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun observeRegistrationStatus() {
        authViewModel.registrationStatus.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    toggleInput(false)
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    toggleInput(true)
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    toggleInput(true)
                    Toast.makeText(this, resource.message ?: "An error occurred", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkRegistrationStatus() {
        binding.btnRegister.setOnClickListener {
            val name = binding.edRegisterName.text.toString().trim()
            val email = binding.edRegisterEmail.text.toString().trim()
            val password = binding.edRegisterPassword.text.toString().trim()

            if (name.isEmpty()) {
                binding.edRegisterName.error = getString(R.string.error_empty_name)
            }
            if (!isValidEmail(email)) {
                binding.edRegisterEmail.error = getString(R.string.error_invalid_email)
            }
            if (password.length < 8) {
                binding.edRegisterPassword.error = getString(R.string.error_password_length)
            }

            authViewModel.registerUser(name, email, password)
        }
    }

    private fun isValidEmail(email: CharSequence): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun toggleInput(isEnabled: Boolean) {
        binding.edRegisterName.isEnabled = isEnabled
        binding.edRegisterEmail.isEnabled = isEnabled
        binding.edRegisterPassword.isEnabled = isEnabled
        binding.btnRegister.isEnabled = isEnabled
    }
}

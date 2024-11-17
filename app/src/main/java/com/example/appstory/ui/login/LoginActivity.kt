@file:Suppress("DEPRECATION")

package com.example.appstory.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
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

        setupViews()
        setupObservers()
    }

    private fun setupViews() {
        binding.logoImageView.startAnimation(
            AnimationUtils.loadAnimation(this, R.anim.logo_animation)
        )

        binding.edLoginEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateEmail(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.passwordInputView.addTextChangedListener { text ->
            validatePassword(text)
        }

        binding.passwordInputView.setErrorMessage(getString(R.string.error_password_length))

        binding.btnLogin.setOnClickListener { attemptLogin() }
        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.btnForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun setupObservers() {
        authViewModel.loginStatus.observe(this) { resource ->
            handleLoginStatus(resource)
        }

        authViewModel.isLoggedIn.observe(this) { isLoggedIn ->
            if (isLoggedIn) {
                navigateToMain()
            } else {
                Toast.makeText(this, "Please log in", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateEmail(email: String): Boolean {
        return if (!email.isValidEmail()) {
            binding.edLoginEmail.error = getString(R.string.error_invalid_email)
            false
        } else {
            binding.edLoginEmail.error = null
            true
        }
    }

    private fun validatePassword(password: String): Boolean {
        return if (password.length < 8) {
            binding.passwordInputView.setErrorMessage(getString(R.string.error_password_length))
            false
        } else {
            binding.passwordInputView.setErrorMessage(null)
            true
        }
    }

    private fun attemptLogin() {
        val email = binding.edLoginEmail.text.toString().trim()
        val password = binding.passwordInputView.getText()

        if (validateEmail(email) && validatePassword(password)) {
            authViewModel.loginUser(email, password)
        }
    }

    private fun handleLoginStatus(resource: Resource<LoginResponse>) {
        when (resource) {
            is Resource.Loading -> {
                binding.progressBar.isVisible = true
                toggleInputs(false)
            }
            is Resource.Success -> {
                binding.progressBar.isVisible = false
                toggleInputs(true)
                navigateToMain()
            }
            is Resource.Error -> {
                binding.progressBar.isVisible = false
                toggleInputs(true)
                showError(resource.message)
            }
        }
    }

    private fun toggleInputs(enabled: Boolean) {
        binding.apply {
            edLoginEmail.isEnabled = enabled
            passwordInputView.isEnabled = enabled
            btnLogin.isEnabled = enabled
            btnRegister.isEnabled = enabled
            btnForgotPassword.isEnabled = enabled
        }
    }

    private fun navigateToMain() {
        Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(this)
            finish()
        }
    }

    private fun showError(message: String?) {
        Toast.makeText(this, message ?: "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
    }

    private fun String.isValidEmail(): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }
}
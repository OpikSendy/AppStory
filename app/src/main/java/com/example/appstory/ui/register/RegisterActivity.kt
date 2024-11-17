package com.example.appstory.ui.register

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.appstory.R
import com.example.appstory.data.request.RegisterResponse
import com.example.appstory.databinding.ActivityRegisterBinding
import com.example.appstory.ui.AuthViewModel
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

        setupViews()
        setupObservers()
    }

    private fun setupViews() {
        binding.logoImageView.startAnimation(
            AnimationUtils.loadAnimation(this, R.anim.logo_animation)
        )

        binding.edRegisterName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateName(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.edRegisterEmail.addTextChangedListener(object : TextWatcher {
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

        binding.btnRegister.setOnClickListener { attemptRegister() }
        binding.btnLogin.setOnClickListener {
            finish()
        }
    }

    private fun setupObservers() {
        authViewModel.registrationStatus.observe(this) { resource ->
            handleRegistrationStatus(resource)
        }
        Toast.makeText(this, "Please regis here", Toast.LENGTH_SHORT).show()
    }

    private fun validateName(name: String): Boolean {
        return if (name.isEmpty()) {
            binding.edRegisterName.error = getString(R.string.error_empty_name)
            false
        } else {
            binding.edRegisterName.error = null
            true
        }
    }

    private fun validateEmail(email: String): Boolean {
        return if (!email.isValidEmail()) {
            binding.edRegisterEmail.error = getString(R.string.error_invalid_email)
            false
        } else {
            binding.edRegisterEmail.error = null
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


    private fun attemptRegister() {
        val name = binding.edRegisterName.text.toString().trim()
        val email = binding.edRegisterEmail.text.toString().trim()
        val password = binding.passwordInputView.getText()

        if (validateName(name) && validateEmail(email) && validatePassword(password)) {
            authViewModel.registerUser(name, email, password)
        }
    }

    private fun handleRegistrationStatus(resource: Resource<RegisterResponse>) {
        when (resource) {
            is Resource.Loading -> {
                binding.progressBar.isVisible = true
                toggleInputs(false)
            }
            is Resource.Success -> {
                binding.progressBar.isVisible = false
                toggleInputs(true)
                showSuccessAndNavigateToLogin()
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
            edRegisterName.isEnabled = enabled
            edRegisterEmail.isEnabled = enabled
            passwordInputView.isEnabled = enabled
            btnRegister.isEnabled = enabled
            btnLogin.isEnabled = enabled
        }
    }

    private fun showSuccessAndNavigateToLogin() {
        Toast.makeText(this, "Registrasi berhasil! Silakan login.", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun showError(message: String?) {
        Toast.makeText(this, message ?: "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
    }

    private fun String.isValidEmail(): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }
}
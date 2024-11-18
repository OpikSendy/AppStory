package com.example.appstory.ui.register

import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.example.appstory.R
import com.example.appstory.data.request.RegisterResponse
import com.example.appstory.databinding.ActivityRegisterBinding
import com.example.appstory.ui.AuthViewModel
import com.example.appstory.ui.custom.PasswordInputView
import com.example.appstory.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var passwordInputView: PasswordInputView
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        passwordInputView = binding.passwordInputView

        setupViews()
        setupObservers()
    }

    private fun setupViews() {
        binding.logoImageView.startAnimation(
            AnimationUtils.loadAnimation(this, R.anim.logo_animation)
        )

        binding.edRegisterName.addTextChangedListener { text ->
            validateName(text.toString())
        }

        binding.edRegisterEmail.addTextChangedListener { text ->
            validateEmail(text.toString())
        }

        passwordInputView.validateOnInit(getString(R.string.error_password_length))

        passwordInputView.addTextChangedListener()

        binding.btnRegister.setOnClickListener { attemptRegister() }

        binding.btnLogin.setOnClickListener {
            finish()
        }
    }

    private fun setupObservers() {
        authViewModel.registrationStatus.observe(this) { resource ->
            handleRegistrationStatus(resource)
        }
        Toast.makeText(this, "Please register here", Toast.LENGTH_SHORT).show()
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

    private fun attemptRegister() {
        val name = binding.edRegisterName.text.toString().trim()
        val email = binding.edRegisterEmail.text.toString().trim()
        val password = passwordInputView.getText()

        if (validateName(name) && validateEmail(email) && passwordInputView.isValidPassword()) {
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


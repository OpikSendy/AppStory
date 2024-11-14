package com.example.appstory.ui.password

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appstory.data.api.Retrofit
import com.example.appstory.data.response.ResetPasswordResponse
import com.example.appstory.databinding.ActivityForgotPasswordBinding
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@AndroidEntryPoint
class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSubmit.setOnClickListener {
            sendResetLink()
        }
    }

    private fun sendResetLink() {
        val email = binding.etEmail.text.toString()

        if (email.isEmpty()) {
            showToast("Email cannot be empty")
            return
        }

        if (!isValidEmail(email)) {
            showToast("Please enter a valid email address")
            return
        }

        sendResetRequest(email)
        showToast("Reset password link sent to $email")
        Handler(Looper.getMainLooper()).postDelayed({
            finish()
        }, 1500)

    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun sendResetRequest(email: String) {
        binding.progressBar.visibility = View.VISIBLE

        val apiService = Retrofit.instance
        val call = apiService.sendResetPasswordLink(email)

        call.enqueue(object : Callback<ResetPasswordResponse> {
            override fun onResponse(call: Call<ResetPasswordResponse>, response: Response<ResetPasswordResponse>) {
                binding.progressBar.visibility = View.GONE
                if (!response.isSuccessful) {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                    showToast("Failed to send reset link: $errorMessage")
                }

                if (response.isSuccessful) {
                    showToast("Reset password link sent to $email")
                    finish()
                } else {
                    showToast("Failed to send reset link")
                }
            }

            override fun onFailure(call: Call<ResetPasswordResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                showToast("Error: ${t.message}")
            }

        })
    }
}

package com.example.appstory.ui.custom

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.example.appstory.R
import com.example.appstory.databinding.ViewPasswordInputBinding

class PasswordInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var binding: ViewPasswordInputBinding = ViewPasswordInputBinding.inflate(LayoutInflater.from(context), this, true)

    fun getText(): String {
        return binding.passwordInputLayout.editText?.text.toString()
    }

    fun setErrorMessage(message: String?) {
        if (message == null) {
            binding.passwordInputLayout.error = null
        } else {
            binding.passwordInputLayout.error = message
        }
    }

    fun validatePassword() {
        val password = getText()
        if (password.length < 8) {
            setErrorMessage(context.getString(R.string.error_password_length))
        } else {
            setErrorMessage(null)
        }
    }

    fun isValidPassword(): Boolean {
        return getText().length >= 8
    }

    fun validateOnInit(errorMessage: String?) {
        if (getText().length < 8) {
            setErrorMessage(errorMessage)
        }
    }

    fun addTextChangedListener() {
        binding.passwordInputLayout.editText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validatePassword()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
}
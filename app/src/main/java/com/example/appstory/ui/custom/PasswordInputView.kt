package com.example.appstory.ui.custom

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.appstory.R
import com.google.android.material.textfield.TextInputLayout

class PasswordInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val inputLayout: TextInputLayout
    private val editText: EditText

    init {
        inflate(context, R.layout.view_password_input, this)
        inputLayout = findViewById(R.id.passwordInputLayout)
        editText = findViewById(R.id.edPassword)

        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validatePassword()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    fun setErrorMessage(errorMessage: String?) {
        if (errorMessage == null) {
            inputLayout.isErrorEnabled = false
        } else {
            inputLayout.error = errorMessage
            inputLayout.isErrorEnabled = true
        }
    }

    fun getText(): String = editText.text.toString()

    fun addTextChangedListener(listener: (String) -> Unit) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                listener(s?.toString() ?: "")
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun validatePassword() {
        val password = getText()
        if (password.length < 8) {
            setErrorMessage(context.getString(R.string.error_password_length))
        } else {
            setErrorMessage(null)
        }
    }
}

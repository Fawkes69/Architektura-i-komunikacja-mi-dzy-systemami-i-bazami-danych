package com.example.zarzdzanie_miejscami

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.zarzdzanie_miejscami.network.ApiClient
import com.example.zarzdzanie_miejscami.network.RegisterRequest
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var fullNameInputLayout: TextInputLayout
    private lateinit var fullNameEditText: TextInputEditText
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var confirmPasswordInputLayout: TextInputLayout
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var registerButton: MaterialButton
    private lateinit var backToLoginButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupListeners()
    }

    private fun initializeViews() {
        fullNameInputLayout = findViewById(R.id.full_name_input_layout)
        fullNameEditText = findViewById(R.id.full_name_edit_text)
        emailInputLayout = findViewById(R.id.email_input_layout)
        emailEditText = findViewById(R.id.email_edit_text)
        passwordInputLayout = findViewById(R.id.password_input_layout)
        passwordEditText = findViewById(R.id.password_edit_text)
        confirmPasswordInputLayout = findViewById(R.id.confirm_password_input_layout)
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text)
        registerButton = findViewById(R.id.register_button)
        backToLoginButton = findViewById(R.id.back_to_login_button)
    }

    private fun setupListeners() {
        registerButton.setOnClickListener {
            if (validateForm()) {
                performRegistration()
            }
        }

        backToLoginButton.setOnClickListener {
            finish()
        }
    }

    private fun validateForm(): Boolean {
        val fullName = fullNameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()

        var isValid = true

        if (fullName.isEmpty()) {
            fullNameInputLayout.error = getString(R.string.full_name_required)
            isValid = false
        } else {
            fullNameInputLayout.error = null
        }

        if (email.isEmpty()) {
            emailInputLayout.error = getString(R.string.email_required)
            isValid = false
        } else if (!isValidEmail(email)) {
            emailInputLayout.error = getString(R.string.invalid_email)
            isValid = false
        } else {
            emailInputLayout.error = null
        }

        if (password.isEmpty()) {
            passwordInputLayout.error = getString(R.string.password_required)
            isValid = false
        } else if (password.length < 8) {
            passwordInputLayout.error = getString(R.string.password_too_short)
            isValid = false
        } else if (!password.matches(Regex(".*[A-Z].*"))) {
            passwordInputLayout.error = getString(R.string.password_uppercase)
            isValid = false
        } else if (!password.matches(Regex(".*[0-9].*"))) {
            passwordInputLayout.error = getString(R.string.password_number)
            isValid = false
        } else {
            passwordInputLayout.error = null
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordInputLayout.error = getString(R.string.confirm_password_required)
            isValid = false
        } else if (password != confirmPassword) {
            confirmPasswordInputLayout.error = getString(R.string.passwords_not_match)
            isValid = false
        } else {
            confirmPasswordInputLayout.error = null
        }

        return isValid
    }

    private fun performRegistration() {
        val fullName = fullNameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()

        lifecycleScope.launch {
            registerButton.isEnabled = false
            val response = try {
                ApiClient.authApi.register(
                    RegisterRequest(
                        email = email,
                        fullName = fullName,
                        password = password
                    )
                )
            } catch (exception: Exception) {
                registerButton.isEnabled = true
                Toast.makeText(this@RegisterActivity, getString(R.string.api_error), Toast.LENGTH_SHORT).show()
                return@launch
            }

            registerButton.isEnabled = true

            if (response.isSuccessful) {
                Toast.makeText(this@RegisterActivity, getString(R.string.register_success), Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(
                    this@RegisterActivity,
                    response.errorBody()?.string()?.takeIf { it.isNotBlank() } ?: getString(R.string.registration_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

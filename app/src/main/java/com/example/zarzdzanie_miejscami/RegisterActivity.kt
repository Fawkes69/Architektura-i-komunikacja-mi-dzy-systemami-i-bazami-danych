package com.example.zarzdzanie_miejscami

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class RegisterActivity : AppCompatActivity() {

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
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()

        var isValid = true

        // Walidacja emaila
        if (email.isEmpty()) {
            emailInputLayout.error = getString(R.string.email_required)
            isValid = false
        } else if (!isValidEmail(email)) {
            emailInputLayout.error = getString(R.string.invalid_email)
            isValid = false
        } else {
            emailInputLayout.error = null
        }

        // Walidacja hasła
        if (password.isEmpty()) {
            passwordInputLayout.error = getString(R.string.password_required)
            isValid = false
        } else if (password.length < 6) {
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

        // Walidacja potwierdzenia hasła
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
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()

        // TODO: Implementacja logiki rejestracji (Firebase, serwer itp.)
        Toast.makeText(this, "Rejestracja: $email", Toast.LENGTH_SHORT).show()

        // Po udanej rejestracji powrót do logowania
        // startActivity(Intent(this, LoginActivity::class.java))
        // finish()
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
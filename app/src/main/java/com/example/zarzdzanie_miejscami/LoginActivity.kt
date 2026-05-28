package com.example.zarzdzanie_miejscami

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.zarzdzanie_miejscami.data.SessionManager
import com.example.zarzdzanie_miejscami.network.ApiClient
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var registerButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(applicationContext)

        sessionManager.getAccessToken()?.let {
            openHome()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_container)) { v, insets ->
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
        loginButton = findViewById(R.id.login_button)
        registerButton = findViewById(R.id.register_button)
    }

    private fun setupListeners() {
        loginButton.setOnClickListener {
            if (validateForm()) {
                performLogin()
            }
        }

        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateForm(): Boolean {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()

        var isValid = true

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
        } else if (password.length < 6) {
            passwordInputLayout.error = getString(R.string.password_too_short)
            isValid = false
        } else {
            passwordInputLayout.error = null
        }

        return isValid
    }

    private fun performLogin() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()

        lifecycleScope.launch {
            loginButton.isEnabled = false
            val response = try {
                ApiClient.authApi.login(email, password)
            } catch (exception: Exception) {
                loginButton.isEnabled = true
                Toast.makeText(this@LoginActivity, getString(R.string.api_error), Toast.LENGTH_SHORT).show()
                return@launch
            }

            loginButton.isEnabled = true

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    sessionManager.saveTokens(body.accessToken, body.refreshToken)
                    Toast.makeText(this@LoginActivity, getString(R.string.login_success), Toast.LENGTH_SHORT).show()
                    openHome()
                } else {
                    Toast.makeText(this@LoginActivity, getString(R.string.api_error), Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(
                    this@LoginActivity,
                    response.errorBody()?.string()?.takeIf { it.isNotBlank() } ?: getString(R.string.login_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun openHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

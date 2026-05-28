package com.example.zarzdzanie_miejscami

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.zarzdzanie_miejscami.data.SessionManager
import com.example.zarzdzanie_miejscami.network.ApiClient
import com.example.zarzdzanie_miejscami.network.ReservationCreateRequest
import com.google.android.material.textfield.TextInputEditText
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

class ReservationFormActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SPACE_ID = "extra_space_id"
        const val EXTRA_SPACE_NAME = "extra_space_name"
    }

    private val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    private lateinit var sessionManager: SessionManager
    private lateinit var spaceTitleTextView: TextView
    private lateinit var startTimeEditText: TextInputEditText
    private lateinit var endTimeEditText: TextInputEditText
    private lateinit var notesEditText: TextInputEditText
    private lateinit var createReservationButton: Button

    private var spaceId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(applicationContext)

        enableEdgeToEdge()
        setContentView(R.layout.activity_reservation_form)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.reservation_form_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        spaceId = intent.getIntExtra(EXTRA_SPACE_ID, -1)
        spaceTitleTextView = findViewById(R.id.space_title_text)
        startTimeEditText = findViewById(R.id.start_time_edit_text)
        endTimeEditText = findViewById(R.id.end_time_edit_text)
        notesEditText = findViewById(R.id.notes_edit_text)
        createReservationButton = findViewById(R.id.create_reservation_button)

        spaceTitleTextView.text = intent.getStringExtra(EXTRA_SPACE_NAME) ?: getString(R.string.create_reservation)
        createReservationButton.setOnClickListener { createReservation() }
    }

    private fun createReservation() {
        val token = sessionManager.getAccessToken() ?: return
        val startRaw = startTimeEditText.text?.toString().orEmpty().trim()
        val endRaw = endTimeEditText.text?.toString().orEmpty().trim()
        val notes = notesEditText.text?.toString().orEmpty()

        if (spaceId <= 0) {
            Toast.makeText(this, getString(R.string.load_failed), Toast.LENGTH_SHORT).show()
            return
        }

        val startIso = parseToIso(startRaw) ?: run {
            Toast.makeText(this, "Nieprawidłowy format startu", Toast.LENGTH_SHORT).show()
            return
        }
        val endIso = parseToIso(endRaw) ?: run {
            Toast.makeText(this, "Nieprawidłowy format końca", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            createReservationButton.isEnabled = false
            val response = try {
                ApiClient.reservationApi.createReservation(
                    "Bearer $token",
                    ReservationCreateRequest(
                        spaceId = spaceId,
                        startTime = startIso,
                        endTime = endIso,
                        notes = notes
                    )
                )
            } catch (exception: Exception) {
                createReservationButton.isEnabled = true
                Toast.makeText(this@ReservationFormActivity, getString(R.string.api_error), Toast.LENGTH_SHORT).show()
                return@launch
            }

            createReservationButton.isEnabled = true

            if (response.isSuccessful) {
                Toast.makeText(this@ReservationFormActivity, getString(R.string.reservation_created), Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(
                    this@ReservationFormActivity,
                    response.errorBody()?.string()?.takeIf { it.isNotBlank() } ?: getString(R.string.load_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun parseToIso(value: String): String? {
        return try {
            val local = LocalDateTime.parse(value, inputFormatter)
            local.atZone(ZoneId.systemDefault()).toInstant().toString()
        } catch (_: Exception) {
            null
        }
    }
}

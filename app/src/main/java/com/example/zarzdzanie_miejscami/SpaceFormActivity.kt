package com.example.zarzdzanie_miejscami

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.zarzdzanie_miejscami.data.SessionManager
import com.example.zarzdzanie_miejscami.network.ApiClient
import com.example.zarzdzanie_miejscami.network.SpaceCreateRequest
import com.example.zarzdzanie_miejscami.network.SpaceUpdateRequest
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import android.widget.TextView
import kotlinx.coroutines.launch

class SpaceFormActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SPACE_ID = "extra_space_id"
        const val EXTRA_SPACE_NAME = "extra_space_name"
        const val EXTRA_SPACE_DESCRIPTION = "extra_space_description"
        const val EXTRA_SPACE_TYPE = "extra_space_type"
        const val EXTRA_SPACE_FLOOR = "extra_space_floor"
        const val EXTRA_SPACE_CAPACITY = "extra_space_capacity"
        const val EXTRA_SPACE_POS_X = "extra_space_pos_x"
        const val EXTRA_SPACE_POS_Y = "extra_space_pos_y"
        const val EXTRA_SPACE_AVAILABLE = "extra_space_available"
    }

    private lateinit var sessionManager: SessionManager
    private lateinit var titleEditText: TextInputEditText
    private lateinit var formTitleText: TextView
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var typeEditText: TextInputEditText
    private lateinit var floorEditText: TextInputEditText
    private lateinit var capacityEditText: TextInputEditText
    private lateinit var posXEditText: TextInputEditText
    private lateinit var posYEditText: TextInputEditText
    private lateinit var availableSwitch: MaterialSwitch
    private lateinit var saveButton: Button

    private var spaceId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(applicationContext)

        enableEdgeToEdge()
        setContentView(R.layout.activity_space_form)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.space_form_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        titleEditText = findViewById(R.id.space_name_edit_text)
        formTitleText = findViewById(R.id.space_form_title)
        descriptionEditText = findViewById(R.id.space_description_edit_text)
        typeEditText = findViewById(R.id.space_type_edit_text)
        floorEditText = findViewById(R.id.space_floor_edit_text)
        capacityEditText = findViewById(R.id.space_capacity_edit_text)
        posXEditText = findViewById(R.id.space_pos_x_edit_text)
        posYEditText = findViewById(R.id.space_pos_y_edit_text)
        availableSwitch = findViewById(R.id.space_available_switch)
        saveButton = findViewById(R.id.save_space_button)

        spaceId = intent.getIntExtra(EXTRA_SPACE_ID, -1)
        if (spaceId > 0) {
            fillFields()
            formTitleText.text = getString(R.string.edit_space)
        } else {
            formTitleText.text = getString(R.string.add_space)
        }

        saveButton.setOnClickListener { saveSpace() }
    }

    private fun fillFields() {
        titleEditText.setText(intent.getStringExtra(EXTRA_SPACE_NAME).orEmpty())
        descriptionEditText.setText(intent.getStringExtra(EXTRA_SPACE_DESCRIPTION).orEmpty())
        typeEditText.setText(intent.getStringExtra(EXTRA_SPACE_TYPE).orEmpty())
        floorEditText.setText(intent.getIntExtra(EXTRA_SPACE_FLOOR, 0).toString())
        capacityEditText.setText(intent.getIntExtra(EXTRA_SPACE_CAPACITY, 1).toString())
        posXEditText.setText(intent.getFloatExtra(EXTRA_SPACE_POS_X, 0f).toString())
        posYEditText.setText(intent.getFloatExtra(EXTRA_SPACE_POS_Y, 0f).toString())
        availableSwitch.isChecked = intent.getBooleanExtra(EXTRA_SPACE_AVAILABLE, true)
    }

    private fun saveSpace() {
        val token = sessionManager.getAccessToken() ?: return
        val name = titleEditText.text?.toString().orEmpty().trim()
        val description = descriptionEditText.text?.toString().orEmpty().trim()
        val type = normalizeSpaceType(typeEditText.text?.toString().orEmpty())
        val floor = floorEditText.text?.toString()?.toIntOrNull()
        val capacity = capacityEditText.text?.toString()?.toIntOrNull()
        val posX = posXEditText.text?.toString()?.toFloatOrNull() ?: 0f
        val posY = posYEditText.text?.toString()?.toFloatOrNull() ?: 0f
        val isAvailable = availableSwitch.isChecked

        if (name.isBlank() || type == null || floor == null || capacity == null) {
            Toast.makeText(this, "Uzupełnij wymagane pola", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            saveButton.isEnabled = false
            val response = try {
                if (spaceId > 0) {
                    ApiClient.reservationApi.updateSpace(
                        "Bearer $token",
                        spaceId,
                        SpaceUpdateRequest(
                            name = name,
                            description = description,
                            isAvailable = isAvailable,
                            capacity = capacity,
                            posX = posX,
                            posY = posY
                        )
                    )
                } else {
                    ApiClient.reservationApi.createSpace(
                        "Bearer $token",
                        SpaceCreateRequest(
                            name = name,
                            description = description,
                            spaceType = type,
                            floor = floor,
                            capacity = capacity,
                            isAvailable = isAvailable,
                            posX = posX,
                            posY = posY
                        )
                    )
                }
            } catch (exception: Exception) {
                saveButton.isEnabled = true
                Toast.makeText(this@SpaceFormActivity, getString(R.string.api_error), Toast.LENGTH_SHORT).show()
                return@launch
            }

            saveButton.isEnabled = true

            if (response.isSuccessful) {
                Toast.makeText(this@SpaceFormActivity, getString(R.string.space_saved), Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(
                    this@SpaceFormActivity,
                    response.errorBody()?.string()?.takeIf { it.isNotBlank() } ?: getString(R.string.load_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun normalizeSpaceType(raw: String): String? {
        return when (raw.trim().lowercase()) {
            "desk", "biurko" -> "desk"
            "meeting_room", "meeting room", "sala", "sala konferencyjna" -> "meeting_room"
            else -> null
        }
    }
}

package com.example.zarzdzanie_miejscami

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zarzdzanie_miejscami.data.SessionManager
import com.example.zarzdzanie_miejscami.network.ApiClient
import com.example.zarzdzanie_miejscami.network.SpaceDto
import com.example.zarzdzanie_miejscami.network.UserDto
import com.google.android.material.chip.ChipGroup
import com.google.android.material.search.SearchView
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var userNameText: TextView
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var deskAdapter: DeskAdapter
    private lateinit var chipGroup: ChipGroup
    private lateinit var myReservationsButton: Button
    private lateinit var adminPanelButton: Button

    private var currentUser: UserDto? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(applicationContext)

        if (sessionManager.getAccessToken().isNullOrBlank()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupRecyclerView()
        setupListeners()
        loadCurrentUser()
        loadSpaces()
    }

    override fun onResume() {
        super.onResume()
        if (::deskAdapter.isInitialized) {
            loadSpaces()
        }
    }

    private fun initializeViews() {
        userNameText = findViewById(R.id.user_name_text)
        searchView = findViewById(R.id.search_view)
        recyclerView = findViewById(R.id.desks_recycler_view)
        chipGroup = findViewById(R.id.chip_filter_group)
        myReservationsButton = findViewById(R.id.my_reservations_button)
        adminPanelButton = findViewById(R.id.admin_panel_button)
    }

    private fun setupRecyclerView() {
        deskAdapter = DeskAdapter(mutableListOf()) { desk ->
            startActivity(
                Intent(this, ReservationFormActivity::class.java)
                    .putExtra(ReservationFormActivity.EXTRA_SPACE_ID, desk.id)
                    .putExtra(ReservationFormActivity.EXTRA_SPACE_NAME, desk.name)
            )
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity, LinearLayoutManager.VERTICAL, false)
            adapter = deskAdapter
        }
    }

    private fun setupListeners() {
        myReservationsButton.setOnClickListener {
            startActivity(Intent(this, MyReservationsActivity::class.java))
        }

        adminPanelButton.setOnClickListener {
            startActivity(Intent(this, AdminActivity::class.java))
        }
    }

    private fun loadCurrentUser() {
        val token = sessionManager.getAccessToken() ?: return

        lifecycleScope.launch {
            val response = try {
                ApiClient.authApi.getMe("Bearer $token")
            } catch (exception: Exception) {
                Toast.makeText(this@HomeActivity, getString(R.string.api_error), Toast.LENGTH_SHORT).show()
                return@launch
            }

            if (response.isSuccessful) {
                currentUser = response.body()
                userNameText.text = currentUser?.fullName ?: getString(R.string.app_name)
                adminPanelButton.visibility = if (currentUser?.isAdmin == true) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }
            } else if (response.code() == 401) {
                sessionManager.clear()
                startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
                finish()
            }
        }
    }

    private fun loadSpaces() {
        val token = sessionManager.getAccessToken() ?: return

        lifecycleScope.launch {
            val response = try {
                ApiClient.reservationApi.getSpaces("Bearer $token")
            } catch (exception: Exception) {
                Toast.makeText(this@HomeActivity, getString(R.string.api_error), Toast.LENGTH_SHORT).show()
                return@launch
            }

            if (response.isSuccessful) {
                deskAdapter.submitList(response.body().orEmpty().toDeskItems())
            } else if (response.code() == 401) {
                sessionManager.clear()
                startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
                finish()
            } else {
                Toast.makeText(
                    this@HomeActivity,
                    response.errorBody()?.string()?.takeIf { it.isNotBlank() } ?: getString(R.string.load_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun List<SpaceDto>.toDeskItems(): List<Desk> {
        return map { space ->
            Desk(
                id = space.id,
                name = space.name,
                location = "Piętro ${space.floor} · ${space.spaceType.name.lowercase().replace('_', ' ')}",
                pricePerDay = if (space.isAvailable) 1.0 else 0.0,
                rating = if (space.isAvailable) 5.0 else 0.0,
                isFavorite = false
            )
        }
    }
}

data class Desk(
    val id: Int,
    val name: String,
    val location: String,
    val pricePerDay: Double,
    val rating: Double,
    val isFavorite: Boolean
)

package com.example.zarzdzanie_miejscami

import android.content.Intent
import android.os.Bundle
import android.widget.Button
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
import kotlinx.coroutines.launch

class AdminActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var addSpaceButton: Button
    private lateinit var adapter: AdminSpaceAdapter
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
        setContentView(R.layout.activity_admin)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.admin_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.admin_spaces_recycler_view)
        addSpaceButton = findViewById(R.id.add_space_button)
        adapter = AdminSpaceAdapter(mutableListOf(), ::editSpace, ::deleteSpace)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        addSpaceButton.setOnClickListener {
            startActivity(Intent(this, SpaceFormActivity::class.java))
        }

        loadCurrentUser()
        loadSpaces()
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) {
            loadSpaces()
        }
    }

    private fun loadSpaces() {
        val token = sessionManager.getAccessToken() ?: return
        lifecycleScope.launch {
            val response = try {
                ApiClient.reservationApi.getSpaces("Bearer $token")
            } catch (exception: Exception) {
                Toast.makeText(this@AdminActivity, getString(R.string.api_error), Toast.LENGTH_SHORT).show()
                return@launch
            }

            if (response.isSuccessful) {
                adapter.submitList(response.body().orEmpty())
            } else {
                Toast.makeText(
                    this@AdminActivity,
                    response.errorBody()?.string()?.takeIf { it.isNotBlank() } ?: getString(R.string.load_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun loadCurrentUser() {
        val token = sessionManager.getAccessToken() ?: return
        lifecycleScope.launch {
            val response = try {
                ApiClient.authApi.getMe("Bearer $token")
            } catch (exception: Exception) {
                return@launch
            }

            if (response.isSuccessful) {
                currentUser = response.body()
                if (currentUser?.isAdmin != true) {
                    Toast.makeText(this@AdminActivity, "Brak uprawnień administratora", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun editSpace(space: SpaceDto) {
        startActivity(
            Intent(this, SpaceFormActivity::class.java)
                .putExtra(SpaceFormActivity.EXTRA_SPACE_ID, space.id)
                .putExtra(SpaceFormActivity.EXTRA_SPACE_NAME, space.name)
                .putExtra(SpaceFormActivity.EXTRA_SPACE_DESCRIPTION, space.description)
                .putExtra(SpaceFormActivity.EXTRA_SPACE_TYPE, space.spaceType.name.lowercase())
                .putExtra(SpaceFormActivity.EXTRA_SPACE_FLOOR, space.floor)
                .putExtra(SpaceFormActivity.EXTRA_SPACE_CAPACITY, space.capacity)
                .putExtra(SpaceFormActivity.EXTRA_SPACE_POS_X, space.posX)
                .putExtra(SpaceFormActivity.EXTRA_SPACE_POS_Y, space.posY)
                .putExtra(SpaceFormActivity.EXTRA_SPACE_AVAILABLE, space.isAvailable)
        )
    }

    private fun deleteSpace(space: SpaceDto) {
        val token = sessionManager.getAccessToken() ?: return
        lifecycleScope.launch {
            val response = try {
                ApiClient.reservationApi.deleteSpace("Bearer $token", space.id)
            } catch (exception: Exception) {
                Toast.makeText(this@AdminActivity, getString(R.string.api_error), Toast.LENGTH_SHORT).show()
                return@launch
            }

            if (response.isSuccessful) {
                Toast.makeText(this@AdminActivity, getString(R.string.space_deleted), Toast.LENGTH_SHORT).show()
                loadSpaces()
            } else {
                Toast.makeText(
                    this@AdminActivity,
                    response.errorBody()?.string()?.takeIf { it.isNotBlank() } ?: getString(R.string.load_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

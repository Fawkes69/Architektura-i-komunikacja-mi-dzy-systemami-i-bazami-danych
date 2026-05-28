package com.example.zarzdzanie_miejscami

import android.os.Bundle
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
import com.example.zarzdzanie_miejscami.network.ReservationDto
import kotlinx.coroutines.launch

class MyReservationsActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReservationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(applicationContext)

        enableEdgeToEdge()
        setContentView(R.layout.activity_my_reservations)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.my_reservations_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.reservations_recycler_view)
        adapter = ReservationAdapter(mutableListOf(), ::cancelReservation)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        loadReservations()
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) {
            loadReservations()
        }
    }

    private fun loadReservations() {
        val token = sessionManager.getAccessToken() ?: return
        lifecycleScope.launch {
            val response = try {
                ApiClient.reservationApi.getReservations("Bearer $token")
            } catch (exception: Exception) {
                Toast.makeText(this@MyReservationsActivity, getString(R.string.api_error), Toast.LENGTH_SHORT).show()
                return@launch
            }

            if (response.isSuccessful) {
                adapter.submitList(response.body().orEmpty())
            } else {
                Toast.makeText(
                    this@MyReservationsActivity,
                    response.errorBody()?.string()?.takeIf { it.isNotBlank() } ?: getString(R.string.load_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun cancelReservation(reservation: ReservationDto) {
        val token = sessionManager.getAccessToken() ?: return
        lifecycleScope.launch {
            val response = try {
                ApiClient.reservationApi.cancelReservation("Bearer $token", reservation.id)
            } catch (exception: Exception) {
                Toast.makeText(this@MyReservationsActivity, getString(R.string.api_error), Toast.LENGTH_SHORT).show()
                return@launch
            }

            if (response.isSuccessful) {
                Toast.makeText(this@MyReservationsActivity, getString(R.string.reservation_cancelled), Toast.LENGTH_SHORT).show()
                loadReservations()
            } else {
                Toast.makeText(
                    this@MyReservationsActivity,
                    response.errorBody()?.string()?.takeIf { it.isNotBlank() } ?: getString(R.string.load_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

package com.example.zarzdzanie_miejscami

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbarlayout.AppBarLayout
import com.google.android.material.chip.ChipGroup
import com.google.android.material.search.SearchView
import com.google.android.material.textfield.TextInputEditText

class HomeActivity : AppCompatActivity() {

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var deskAdapter: DeskAdapter
    private lateinit var chipGroup: ChipGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    }

    private fun initializeViews() {
        searchView = findViewById(R.id.search_view)
        recyclerView = findViewById(R.id.desks_recycler_view)
        chipGroup = findViewById(R.id.chip_filter_group)
    }

    private fun setupRecyclerView() {
        deskAdapter = DeskAdapter(getDummyDesks())
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity, LinearLayoutManager.VERTICAL, false)
            adapter = deskAdapter
        }
    }

    private fun setupListeners() {
        // TODO: Implementacja wyszukiwania
        // TODO: Implementacja filtrów
    }

    private fun getDummyDesks(): List<Desk> {
        return listOf(
            Desk(1, "Biuro A - Stanowisko 1", "Piętro 2", 45.0, 4.5, true),
            Desk(2, "Biuro A - Stanowisko 2", "Piętro 2", 45.0, 4.2, false),
            Desk(3, "Biuro B - Stanowisko 3", "Piętro 1", 50.0, 4.8, true),
            Desk(4, "Sala konferencyjna A", "Parter", 120.0, 5.0, true),
            Desk(5, "Biuro C - Stanowisko 5", "Piętro 3", 40.0, 4.1, false)
        )
    }
}

// Model danych
data class Desk(
    val id: Int,
    val name: String,
    val location: String,
    val pricePerDay: Double,
    val rating: Double,
    val isFavorite: Boolean
)
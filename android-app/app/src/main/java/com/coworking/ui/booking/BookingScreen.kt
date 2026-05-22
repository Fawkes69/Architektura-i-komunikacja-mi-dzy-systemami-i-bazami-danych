package com.coworking.ui.booking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    spaceId: Int,
    onBack: () -> Unit,
    onBookingSuccess: () -> Unit,
    viewModel: BookingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var startHour by remember { mutableStateOf(9) }
    var endHour by remember { mutableStateOf(10) }
    var notes by remember { mutableStateOf("") }

    LaunchedEffect(uiState.bookingSuccess) {
        if (uiState.bookingSuccess) {
            viewModel.resetBookingSuccess()
            onBookingSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Zarezerwuj miejsce") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card {
                Column(Modifier.padding(16.dp)) {
                    Text("Szczegóły rezerwacji", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text("Miejsce #$spaceId", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Date picker simplified
            OutlinedCard {
                Column(Modifier.padding(16.dp)) {
                    Text("Data", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (0..6).forEach { offset ->
                            val date = LocalDate.now().plusDays(offset.toLong())
                            val isSelected = date == selectedDate
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedDate = date },
                                label = {
                                    Text(date.format(DateTimeFormatter.ofPattern("d\nMMM")))
                                }
                            )
                        }
                    }
                }
            }

            // Time selection
            OutlinedCard {
                Column(Modifier.padding(16.dp)) {
                    Text("Godziny", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(12.dp))

                    Text("Od: ${startHour}:00", style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = startHour.toFloat(),
                        onValueChange = { startHour = it.toInt() },
                        valueRange = 7f..20f,
                        steps = 12
                    )

                    Spacer(Modifier.height(8.dp))

                    Text("Do: ${endHour}:00", style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = endHour.toFloat(),
                        onValueChange = { endHour = it.toInt() },
                        valueRange = 8f..21f,
                        steps = 12
                    )

                    if (endHour <= startHour) {
                        Text(
                            "Godzina końca musi być późniejsza niż start",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notatki (opcjonalnie)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            uiState.error?.let { error ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(
                        error,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Button(
                onClick = {
                    val isoDate = selectedDate.toString()
                    val startTime = "${isoDate}T${startHour.toString().padStart(2,'0')}:00:00Z"
                    val endTime = "${isoDate}T${endHour.toString().padStart(2,'0')}:00:00Z"
                    viewModel.createReservation(spaceId, startTime, endTime, notes)
                },
                enabled = !uiState.isLoading && endHour > startHour,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Potwierdź rezerwację")
                }
            }
        }
    }
}

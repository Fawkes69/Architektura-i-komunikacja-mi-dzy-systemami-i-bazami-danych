package com.coworking.ui.booking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.coworking.data.local.entities.ReservationEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReservationsScreen(viewModel: BookingViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var confirmCancelId by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Moje rezerwacje") }) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            if (uiState.isLoading && uiState.reservations.isEmpty()) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else if (uiState.reservations.isEmpty()) {
                Column(
                    Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Event, contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    Text("Brak rezerwacji", style = MaterialTheme.typography.titleMedium)
                    Text("Zarezerwuj miejsce z ekranu głównego",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.reservations) { reservation ->
                        ReservationCard(
                            reservation = reservation,
                            onCancel = { confirmCancelId = reservation.id }
                        )
                    }
                }
            }
        }
    }

    // Cancel confirmation dialog
    confirmCancelId?.let { id ->
        AlertDialog(
            onDismissRequest = { confirmCancelId = null },
            title = { Text("Anulować rezerwację?") },
            text = { Text("Tej operacji nie można cofnąć.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelReservation(id)
                        confirmCancelId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Anuluj rezerwację") }
            },
            dismissButton = {
                OutlinedButton(onClick = { confirmCancelId = null }) { Text("Zostaw") }
            }
        )
    }
}

@Composable
fun ReservationCard(reservation: ReservationEntity, onCancel: () -> Unit) {
    val isCancelled = reservation.status == "cancelled"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCancelled)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Miejsce #${reservation.spaceId}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                AssistChip(
                    onClick = {},
                    label = { Text(if (isCancelled) "Anulowana" else "Aktywna") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (isCancelled)
                            MaterialTheme.colorScheme.errorContainer
                        else
                            MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "Od: ${formatDateTime(reservation.startTime)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "Do: ${formatDateTime(reservation.endTime)}",
                style = MaterialTheme.typography.bodyMedium
            )

            if (reservation.notes.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    reservation.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!isCancelled) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onCancel,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Anuluj rezerwację")
                }
            }
        }
    }
}

private fun formatDateTime(iso: String): String {
    return try {
        val parts = iso.removePrefix("").split("T")
        val date = parts[0]
        val time = parts.getOrNull(1)?.take(5) ?: ""
        "$date $time"
    } catch (e: Exception) { iso }
}

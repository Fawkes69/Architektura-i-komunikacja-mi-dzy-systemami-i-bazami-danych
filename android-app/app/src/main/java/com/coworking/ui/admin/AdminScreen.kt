package com.coworking.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.coworking.data.local.entities.SpaceEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(viewModel: AdminViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var deleteConfirmId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Panel Admina") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj miejsce")
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            Column {
                uiState.successMessage?.let {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Text(it, Modifier.padding(12.dp))
                    }
                }
                uiState.error?.let {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(it, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }

                if (uiState.isLoading && uiState.spaces.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                "Wszystkie miejsca (${uiState.spaces.size})",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        items(uiState.spaces) { space ->
                            AdminSpaceCard(space = space, onDelete = { deleteConfirmId = space.id })
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddSpaceDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, description, type, floor, capacity ->
                viewModel.addSpace(name, description, type, floor, capacity)
                showAddDialog = false
            }
        )
    }

    deleteConfirmId?.let { id ->
        AlertDialog(
            onDismissRequest = { deleteConfirmId = null },
            title = { Text("Usunąć miejsce?") },
            text = { Text("Miejsce #$id zostanie trwale usunięte.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteSpace(id); deleteConfirmId = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Usuń") }
            },
            dismissButton = {
                OutlinedButton(onClick = { deleteConfirmId = null }) { Text("Anuluj") }
            }
        )
    }
}

@Composable
fun AdminSpaceCard(space: SpaceEntity, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (space.spaceType == "desk") Icons.Default.Chair else Icons.Default.MeetingRoom,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(space.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    "Piętro ${space.floor} · ${if (space.spaceType == "desk") "Biurko" else "Sala"} · ${space.capacity} os.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = space.isAvailable,
                onCheckedChange = null // Read-only display; update via PATCH in real app
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Usuń", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun AddSpaceDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, Int, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var spaceType by remember { mutableStateOf("desk") }
    var floor by remember { mutableStateOf("1") }
    var capacity by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dodaj nowe miejsce") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nazwa") }, singleLine = true)
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Opis") })

                Text("Typ:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = spaceType == "desk", onClick = { spaceType = "desk" }, label = { Text("Biurko") })
                    FilterChip(selected = spaceType == "meeting_room", onClick = { spaceType = "meeting_room" }, label = { Text("Sala") })
                }

                OutlinedTextField(value = floor, onValueChange = { floor = it }, label = { Text("Piętro") }, singleLine = true)
                OutlinedTextField(value = capacity, onValueChange = { capacity = it }, label = { Text("Pojemność") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(name, description, spaceType, floor.toIntOrNull() ?: 1, capacity.toIntOrNull() ?: 1)
                },
                enabled = name.isNotBlank()
            ) { Text("Dodaj") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Anuluj") }
        }
    )
}

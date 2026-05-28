package com.coworking.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.coworking.data.local.entities.SpaceEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSpaceClick: (Int) -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showMapView by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dostępne miejsca") },
                actions = {
                    IconButton(onClick = { showMapView = !showMapView }) {
                        Icon(
                            if (showMapView) Icons.Default.List else Icons.Default.Map,
                            contentDescription = if (showMapView) "Lista" else "Mapa"
                        )
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profil")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Date selector
            DateSelector(
                selectedDate = uiState.selectedDate,
                onDateSelected = viewModel::selectDate
            )

            // Floor filter
            FloorFilterRow(
                selectedFloor = uiState.selectedFloor,
                onFloorSelected = viewModel::selectFloor
            )

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = viewModel::refresh) { Text("Spróbuj ponownie") }
                    }
                }
            } else if (showMapView) {
                FloorMapView(
                    spaces = uiState.spaces,
                    onSpaceClick = onSpaceClick
                )
            } else {
                SpaceListView(
                    spaces = uiState.spaces,
                    onSpaceClick = onSpaceClick
                )
            }
        }
    }
}

@Composable
fun DateSelector(selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    val today = LocalDate.now()
    val dates = (0..13).map { today.plusDays(it.toLong()) }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(dates) { date ->
            val isSelected = date == selectedDate
            val formatter = DateTimeFormatter.ofPattern("EEE\nd")
            FilterChip(
                selected = isSelected,
                onClick = { onDateSelected(date) },
                label = {
                    Text(
                        date.format(formatter),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }
    }
}

@Composable
fun FloorFilterRow(selectedFloor: Int?, onFloorSelected: (Int?) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedFloor == null,
                onClick = { onFloorSelected(null) },
                label = { Text("Wszystkie piętra") }
            )
        }
        items((1..5).toList()) { floor ->
            FilterChip(
                selected = selectedFloor == floor,
                onClick = { onFloorSelected(floor) },
                label = { Text("Piętro $floor") }
            )
        }
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
fun SpaceListView(spaces: List<SpaceEntity>, onSpaceClick: (Int) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(spaces) { space ->
            SpaceCard(space = space, onClick = { onSpaceClick(space.id) })
        }
    }
}

@Composable
fun SpaceCard(space: SpaceEntity, onClick: () -> Unit) {
    val isDesk = space.spaceType == "desk"
    val availableColor = MaterialTheme.colorScheme.primaryContainer
    val unavailableColor = MaterialTheme.colorScheme.errorContainer

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (space.isAvailable) availableColor else unavailableColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isDesk) Icons.Default.Chair else Icons.Default.MeetingRoom,
                    contentDescription = null,
                    tint = if (space.isAvailable)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onErrorContainer
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(space.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "Piętro ${space.floor} · ${if (isDesk) "Biurko" else "Sala"} · Maks. ${space.capacity} os.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (space.description.isNotBlank()) {
                    Text(
                        space.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            // Availability badge
            AssistChip(
                onClick = {},
                label = { Text(if (space.isAvailable) "Wolne" else "Zajęte") },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (space.isAvailable) availableColor else unavailableColor
                )
            )
        }
    }
}

@Composable
fun FloorMapView(spaces: List<SpaceEntity>, onSpaceClick: (Int) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Text(
            "Widok mapy piętra",
            modifier = Modifier.align(Alignment.TopCenter),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Draw spaces as positioned dots based on pos_x/pos_y (0.0-1.0 range)
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            spaces.forEach { space ->
                val x = (space.posX * maxWidth.value).dp
                val y = (space.posY * maxHeight.value).dp

                Box(
                    modifier = Modifier
                        .offset(x = x, y = y)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (space.isAvailable)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error
                        )
                        .clickable { onSpaceClick(space.id) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (space.spaceType == "desk") Icons.Default.Chair else Icons.Default.MeetingRoom,
                        contentDescription = space.name,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

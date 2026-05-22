package com.coworking.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.coworking.api.UserDto
import com.coworking.data.repository.AuthRepository
import com.coworking.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: UserDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init { loadProfile() }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val r = authRepository.getMe()) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, user = r.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = r.message) }
            }
        }
    }

    fun logout() = viewModelScope.launch { authRepository.logout() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Profil") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            // Avatar
            Surface(
                modifier = Modifier.size(96.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else if (uiState.user != null) {
                val user = uiState.user!!
                Text(user.fullName, style = MaterialTheme.typography.headlineSmall)
                Text(user.email, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)

                if (user.isAdmin) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Administrator") },
                        leadingIcon = { Icon(Icons.Default.AdminPanelSettings, null, Modifier.size(16.dp)) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()

            if (uiState.user?.isAdmin == true) {
                OutlinedButton(
                    onClick = onNavigateToAdmin,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AdminPanelSettings, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Panel Administratora")
                }
            }

            Button(
                onClick = {
                    viewModel.logout()
                    onLogout()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Logout, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Wyloguj się")
            }
        }
    }
}

package com.coworking.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.coworking.ui.admin.AdminScreen
import com.coworking.ui.auth.AuthViewModel
import com.coworking.ui.auth.LoginScreen
import com.coworking.ui.auth.RegisterScreen
import com.coworking.ui.booking.BookingScreen
import com.coworking.ui.booking.MyReservationsScreen
import com.coworking.ui.home.HomeScreen
import com.coworking.ui.profile.ProfileScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Booking : Screen("booking/{spaceId}") {
        fun createRoute(spaceId: Int) = "booking/$spaceId"
    }
    object MyReservations : Screen("my_reservations")
    object Profile : Screen("profile")
    object Admin : Screen("admin")
}

data class BottomNavItem(val label: String, val icon: ImageVector, val screen: Screen)

val bottomNavItems = listOf(
    BottomNavItem("Miejsca", Icons.Default.Home, Screen.Home),
    BottomNavItem("Rezerwacje", Icons.Default.CalendarToday, Screen.MyReservations),
    BottomNavItem("Profil", Icons.Default.Person, Screen.Profile),
)

@Composable
fun CoworkingNavHost() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val token by authViewModel.isLoggedIn.collectAsState(initial = null)

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val showBottomBar = currentRoute in listOf(
        Screen.Home.route, Screen.MyReservations.route, Screen.Profile.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.screen.route,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { _ ->
        NavHost(
            navController = navController,
            startDestination = if (token != null) Screen.Home.route else Screen.Login.route
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = { navController.navigate(Screen.Home.route) { popUpTo(0) } },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = { navController.navigate(Screen.Login.route) { popUpTo(Screen.Login.route) } },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    onSpaceClick = { spaceId -> navController.navigate(Screen.Booking.createRoute(spaceId)) },
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
                )
            }
            composable(
                Screen.Booking.route,
                arguments = listOf(navArgument("spaceId") { type = NavType.IntType })
            ) { backStack ->
                val spaceId = backStack.arguments!!.getInt("spaceId")
                BookingScreen(
                    spaceId = spaceId,
                    onBack = { navController.popBackStack() },
                    onBookingSuccess = {
                        navController.navigate(Screen.MyReservations.route) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                )
            }
            composable(Screen.MyReservations.route) {
                MyReservationsScreen()
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogout = { navController.navigate(Screen.Login.route) { popUpTo(0) } },
                    onNavigateToAdmin = { navController.navigate(Screen.Admin.route) }
                )
            }
            composable(Screen.Admin.route) {
                AdminScreen()
            }
        }
    }
}

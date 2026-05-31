package com.sanay3y.egy.presentation

import MyJobsScreen
import ProviderDetailsScreen
import SearchScreen
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.sanay3y.egy.R
import com.sanay3y.egy.presentation.screens.auth.LoginScreen
import com.sanay3y.egy.presentation.screens.auth.RegisterScreen
import com.sanay3y.egy.presentation.screens.auth.RoleSelectionScreen
import com.sanay3y.egy.presentation.screens.client.ClientHomeScreen
import com.sanay3y.egy.presentation.viewmodel.AuthViewModel
import com.sanay3y.egy.presentation.viewmodel.ClientViewModel
import com.sanay3y.egy.presentation.viewmodel.RequestViewModel

sealed class BottomNavItem(val route: String, val label: String, val icon: Int) {
    object Home : BottomNavItem("home", "Home", R.drawable.home)
    object Search : BottomNavItem("search", "Search", R.drawable.search)
    object Jobs : BottomNavItem("jobs", "Jobs", R.drawable.wrench)
    object Profile : BottomNavItem("profile", "Profile", R.drawable.profile)
}

@Composable
fun Sanay3yApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val clientViewModel: ClientViewModel = viewModel()
    
    var userId by remember { mutableStateOf("") }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomBarScreens = listOf("home", "search", "jobs", "profile")
    val showBottomBar = currentDestination?.route?.substringBefore("?") in bottomBarScreens

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val items = listOf(
                        BottomNavItem.Home,
                        BottomNavItem.Search,
                        BottomNavItem.Jobs,
                        BottomNavItem.Profile
                    )
                    items.forEach { item ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route?.substringBefore("?") == item.route } == true
                        
                        NavigationBarItem(
                            icon = { Icon(painter = painterResource(item.icon), contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )

                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()) // 👈 Fixed: Only apply bottom padding
        ) {
            composable("login") {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = { uid, hasRole ->
                        userId = uid
                        if (hasRole) {
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            navController.navigate("role_selection/$uid")
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate("register")
                    }
                )
            }

            composable("register") {
                RegisterScreen(
                    viewModel = authViewModel,
                    onRegisterSuccess = { uid, hasRole ->
                        userId = uid
                        if (hasRole) {
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            navController.navigate("role_selection/$uid")
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }

            composable("role_selection/{uid}") { backStackEntry ->
                val uid = backStackEntry.arguments?.getString("uid") ?: ""
                RoleSelectionScreen(
                    uid = uid,
                    viewModel = authViewModel,
                    onRoleAssigned = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable("home") {
                ClientHomeScreen(
                    userId = userId,
                    onCategoryClick = { categoryName ->
                        navController.navigate("search?category=$categoryName")
                    }
                )
            }

            composable(
                route = "search?category={category}", //  Accept optional category
                arguments = listOf(navArgument("category") { defaultValue = "" })
            ) { backStackEntry ->
                val category = backStackEntry.arguments?.getString("category") ?: ""
                SearchScreen(
                    viewModel = clientViewModel,
                    category = category, // Pass it to the screen
                    onNavigateToDetails = {
                        navController.navigate("provider_details")
                    }
                )
            }

            composable("jobs"){
                MyJobsScreen(clientViewModel = ClientViewModel(), requestViewModel = RequestViewModel())
            }

            composable("profile") {
                Surface {
                    Text("User Profile Screen")
                }
            }

            composable("provider_details") {
                ProviderDetailsScreen(
                    viewModel = clientViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

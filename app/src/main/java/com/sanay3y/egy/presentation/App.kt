package com.sanay3y.egy.presentation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import android.net.Uri
import com.sanay3y.egy.R
import com.sanay3y.egy.presentation.screens.auth.LoginScreen
import com.sanay3y.egy.presentation.screens.auth.RegisterScreen
import com.sanay3y.egy.presentation.screens.auth.RoleSelectionScreen
import com.sanay3y.egy.presentation.screens.client.ClientHomeScreen
import com.sanay3y.egy.presentation.viewmodel.AuthViewModel
import com.sanay3y.egy.presentation.viewmodel.ClientViewModel
import com.sanay3y.egy.presentation.viewmodel.RequestViewModel

import androidx.compose.runtime.saveable.rememberSaveable
import com.sanay3y.egy.data.model.UserRole
import com.sanay3y.egy.presentation.screens.provider.ProviderDashboardScreen
import com.sanay3y.egy.presentation.screens.provider.ProviderSetupScreen
import com.sanay3y.egy.presentation.screens.provider.ProviderQuotationScreen
import com.sanay3y.egy.presentation.viewmodel.AuthState
import com.sanay3y.egy.presentation.screens.provider.ActiveJobScreen
import com.sanay3y.egy.presentation.screens.ProfileScreen
import com.sanay3y.egy.presentation.screens.client.MyJobsScreen
import com.sanay3y.egy.presentation.screens.client.ProviderDetailsScreen
import com.sanay3y.egy.presentation.screens.client.RequestConfirmationScreen
import com.sanay3y.egy.presentation.screens.client.ServiceRequestScreen
import com.sanay3y.egy.presentation.screens.client.SearchScreen
import com.sanay3y.egy.presentation.screens.client.QuotationReviewScreen
import com.sanay3y.egy.presentation.screens.client.RatingScreen
import com.sanay3y.egy.presentation.viewmodel.JobTrackingViewModel

import androidx.compose.ui.res.stringResource

sealed class BottomNavItem(val route: String, val labelResId: Int, val icon: Int) {
    object Home : BottomNavItem("home", R.string.nav_home, R.drawable.home)
    object Search : BottomNavItem("search", R.string.nav_search, R.drawable.search)
    object Jobs : BottomNavItem("jobs", R.string.nav_jobs, R.drawable.wrench)
    object Profile : BottomNavItem("profile", R.string.nav_profile, R.drawable.profile)
}

@Composable
fun Sanay3yApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val clientViewModel: ClientViewModel = viewModel()
    val requestViewModel: RequestViewModel = viewModel()

    val authState by authViewModel.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var userId by rememberSaveable { mutableStateOf("") }
    var userRole by rememberSaveable { mutableStateOf<UserRole?>(null) }
    var lastHandledAuthState by remember { mutableStateOf<AuthState?>(null) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route?.substringBefore("?")

    val clientBottomBarScreens = listOf("home", "search", "jobs", "profile")
    val providerBottomBarScreens = listOf("provider_dashboard", "profile")
    val showBottomBar = currentDestination?.route?.substringBefore("?")?.substringBefore("/") in (clientBottomBarScreens + providerBottomBarScreens)

    LaunchedEffect(authState, currentRoute) {
        if (authState == lastHandledAuthState) return@LaunchedEffect
        lastHandledAuthState = authState

        when (val state = authState) {
            is AuthState.Success -> {
                userId = state.uid
                userRole = state.role

                val destination = when {
                    !state.hasRole -> "role_selection/${Uri.encode(state.uid)}"
                    state.role == UserRole.PROVIDER && !state.isSetupCompleted ->
                        "provider_setup/${Uri.encode(state.uid)}"
                    state.role == UserRole.PROVIDER -> "provider_dashboard"
                    else -> "home"
                }

                if (currentRoute != destination) {
                    navController.navigate(destination) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                        restoreState = false
                    }
                }
            }

            is AuthState.Idle -> {
                if (currentRoute != "login") {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }

            is AuthState.Error -> {
                snackbarHostState.showSnackbar(state.message)
            }

            else -> Unit
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    if (userRole == UserRole.PROVIDER) {
                        // Provider Bottom Bar
                        NavigationBarItem(
                            icon = { Icon(painter = painterResource(R.drawable.home), contentDescription = stringResource(R.string.nav_dashboard)) },
                            label = { Text(stringResource(R.string.nav_dashboard)) },
                            selected = currentDestination?.hierarchy?.any { it.route == "provider_dashboard" } == true,
                            onClick = {
                                navController.navigate("provider_dashboard") {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(painter = painterResource(R.drawable.profile), contentDescription = stringResource(R.string.nav_profile)) },
                            label = { Text(stringResource(R.string.nav_profile)) },
                            selected = currentDestination?.hierarchy?.any { it.route == "profile" } == true,
                            onClick = {
                                navController.navigate("profile") {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    } else {
                        // Client Bottom Bar
                        val items = listOf(
                            BottomNavItem.Home,
                            BottomNavItem.Search,
                            BottomNavItem.Jobs,
                            BottomNavItem.Profile
                        )
                        items.forEach { item ->
                            val isSelected = currentDestination?.hierarchy?.any { it.route?.substringBefore("?") == item.route } == true

                            NavigationBarItem(
                                icon = { Icon(painter = painterResource(item.icon), contentDescription = stringResource(item.labelResId)) },
                                label = { Text(stringResource(item.labelResId)) },
                                selected = isSelected,
                                onClick = {
                                    navController.navigate(item.route) {
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login", // Initial destination is login, but LaunchedEffect will redirect if needed
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable("login") {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = { uid, hasRole ->
                        // Handled by LaunchedEffect(authState)
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
                        // Handled by LaunchedEffect(authState)
                    },
                    onNavigateToLogin = {
                        if (!navController.navigateUp()) {
                            navController.navigate("login") {
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }

            composable("role_selection/{uid}") { backStackEntry ->
                val uid = Uri.decode(backStackEntry.arguments?.getString("uid") ?: "")
                RoleSelectionScreen(
                    uid = uid,
                    viewModel = authViewModel,
                    onRoleAssigned = {
                        // Handled by LaunchedEffect(authState)
                    },
                    onNavigateBack = {
                        navController.navigateUp()
                    }
                )
            }

            // Client Screens
            composable("home") {
                ClientHomeScreen(
                    userId = userId,
                    onCategoryClick = { categoryName ->
                        navController.navigate(
                            "search?category=${Uri.encode(categoryName)}"
                        )
                    }
                )
            }

            composable("search?category={category}") { backStackEntry ->
                val category = Uri.decode(
                    backStackEntry.arguments?.getString("category") ?: ""
                )
                SearchScreen(
                    viewModel = clientViewModel,
                    userId = userId,
                    category = category,
                    onNavigateToDetails = { providerId: String ->
                        navController.navigate("provider_details/${Uri.encode(providerId)}")
                    }
                )
            }

            composable("jobs"){
                MyJobsScreen(
                    userId = userId,
                    clientViewModel = clientViewModel,
                    requestViewModel = requestViewModel,
                    onViewQuotation = { requestId ->
                        navController.navigate("quotation_review/${Uri.encode(requestId)}")
                    },
                    onRateProvider = { requestId, providerId ->
                        navController.navigate("rating/${Uri.encode(requestId)}/${Uri.encode(providerId)}")
                    },
                    onTrackOrder = { requestId ->
                        navController.navigate("active_job/${Uri.encode(requestId)}")
                    }
                )
            }

            composable("quotation_review/{requestId}") { backStackEntry ->
                val requestId = Uri.decode(backStackEntry.arguments?.getString("requestId") ?: "")
                QuotationReviewScreen(
                    requestId = requestId,
                    onResponded = { navController.navigateUp() },
                    onBack = { navController.navigateUp() }
                )
            }

            composable("profile") {
                ProfileScreen(
                    userId = userId,
                    authViewModel = authViewModel,
                    onLogout = {
                        authViewModel.logout()
                    }
                )
            }

            composable("provider_details/{providerId}") { backStackEntry ->
                val providerId = Uri.decode(backStackEntry.arguments?.getString("providerId") ?: "")
                ProviderDetailsScreen(
                    providerId = providerId,
                    viewModel = clientViewModel,
                    onStartRequest = { pId, serviceType ->
                        navController.navigate(
                            "service_request/${Uri.encode(pId)}/${Uri.encode(serviceType)}"
                        )
                    },
                    onNavigateBack = {
                        navController.navigateUp()
                    }
                )
            }

            composable("service_request/{providerId}/{serviceType}") { backStackEntry ->
                val providerId = Uri.decode(backStackEntry.arguments?.getString("providerId") ?: "")
                val serviceType = Uri.decode(
                    backStackEntry.arguments?.getString("serviceType") ?: ""
                )
                ServiceRequestScreen(
                    viewModel = requestViewModel,
                    providerId = providerId,
                    serviceType = serviceType,
                    userId = userId,
                    onBack = { navController.navigateUp() },
                    onConfirm = {
                        navController.navigate("request_confirmation") {
                            launchSingleTop = true
                        }
                    }
                )
            }

            // Provider Screens
            composable("provider_setup/{uid}") { backStackEntry ->
                val uid = Uri.decode(backStackEntry.arguments?.getString("uid") ?: "")
                ProviderSetupScreen(
                    uid = uid,
                    navController = navController
                )
            }

            composable("request_confirmation") {
                RequestConfirmationScreen(
                    onNavigateToHome = { navController.navigate("home") { launchSingleTop = true } },
                    onNavigateToJobs = { navController.navigate("jobs") { launchSingleTop = true } }
                )
            }

            composable("provider_dashboard") {
                ProviderDashboardScreen(
                    providerId = userId,
                    onNavigateToRequestDetails = { requestId ->
                        navController.navigate("active_job/${Uri.encode(requestId)}")
                    },
                    onNavigateToQuotation = { requestId ->
                        navController.navigate("provider_quotation/${Uri.encode(requestId)}")
                    }
                )
            }

            composable("provider_quotation/{requestId}") { backStackEntry ->
                val requestId = Uri.decode(backStackEntry.arguments?.getString("requestId") ?: "")
                ProviderQuotationScreen(
                    requestId = requestId,
                    providerId = userId,
                    onSubmitted = {
                        navController.navigateUp()
                    },
                    onBack = { navController.navigateUp() }
                )
            }

            composable("active_job/{requestId}") { backStackEntry ->
                val requestId = Uri.decode(backStackEntry.arguments?.getString("requestId") ?: "")
                val trackingViewModel: JobTrackingViewModel = viewModel()
                ActiveJobScreen(
                    requestId = requestId,
                    userId = userId,
                    viewModel = trackingViewModel,
                    onBack = { navController.navigateUp() }
                )
            }

            composable("rating/{requestId}/{providerId}") { backStackEntry ->
                val requestId = Uri.decode(backStackEntry.arguments?.getString("requestId") ?: "")
                val providerId = Uri.decode(backStackEntry.arguments?.getString("providerId") ?: "")
                RatingScreen(
                    requestId = requestId,
                    userId = userId,
                    providerId = providerId,
                    onBackToHome = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
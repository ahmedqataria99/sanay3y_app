package com.sanay3y.egy.presentation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.sanay3y.egy.presentation.screens.auth.LoginScreen
import com.sanay3y.egy.presentation.screens.auth.RegisterScreen
import com.sanay3y.egy.presentation.screens.auth.RoleSelectionScreen
import com.sanay3y.egy.presentation.screens.client.ClientHomeScreen
import com.sanay3y.egy.presentation.viewmodel.AuthViewModel

@Composable
fun Sanay3yApp() {

    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { uid, hasRole ->
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
            ClientHomeScreen()
        }
    }
}

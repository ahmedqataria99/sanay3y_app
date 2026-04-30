package com.sanay3y.egy.presentation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sanay3y.egy.presentation.screens.auth.LoginScreen
import com.sanay3y.egy.presentation.screens.auth.RegisterScreen
import com.sanay3y.egy.presentation.screens.client.ClientHomeScreen
import com.sanay3y.egy.presentation.viewmodel.AuthViewModel
import com.sanay3y.egy.ui.theme.Sanay3y_appTheme

@Composable
fun Sanay3yApp() {
    Sanay3y_appTheme {
        val navController = rememberNavController()
        val authViewModel: AuthViewModel = viewModel()

        NavHost(
            navController = navController,
            startDestination = "login"
        ) {
            composable("login") {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = { uid ->
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
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
                    onRegisterSuccess = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }

            composable("home") {
                ClientHomeScreen()
            }
        }
    }
}

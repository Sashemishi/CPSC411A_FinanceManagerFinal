package com.example.financetracker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.financetracker.ui.auth.LoginScreen
import com.example.financetracker.ui.auth.SignUpScreen
import com.example.financetracker.ui.dashboard.DashboardScreen
import com.example.financetracker.ui.profile.ProfileScreen
import com.example.financetracker.viewmodel.AuthState
import com.example.financetracker.viewmodel.AuthViewModel

object Routes {
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
    const val DASHBOARD = "dashboard"
    const val PROFILE = "profile"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                navController.navigate(Routes.DASHBOARD) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            }
            is AuthState.Unauthenticated -> {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(Routes.DASHBOARD) { inclusive = true }
                }
            }
            else -> Unit
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {

        composable(Routes.LOGIN) {
            LoginScreen(
                onSignUpClick = {
                    navController.navigate(Routes.SIGN_UP)
                },
                authViewModel = authViewModel
            )
        }

        composable(Routes.SIGN_UP) {
            SignUpScreen(
                onLoginClick = {
                    navController.popBackStack()
                },
                authViewModel = authViewModel
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onProfileClick = {
                    navController.navigate(Routes.PROFILE)
                }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onLogout = {
                    authViewModel.logout()
                }
            )
        }
    }
}

package com.example.financetracker.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.financetracker.ui.auth.LoginScreen
import com.example.financetracker.ui.auth.SignUpScreen
import com.example.financetracker.ui.dashboard.DashboardScreen
import com.example.financetracker.ui.profile.ProfileScreen
import com.example.financetracker.ui.transactions.AddTransactionScreen
import com.example.financetracker.ui.transactions.EditTransactionScreen
import com.example.financetracker.ui.transactions.TransactionsListScreen
import com.example.financetracker.viewmodel.AuthStatus
import com.example.financetracker.viewmodel.AuthViewModel
object Routes {
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
    const val DASHBOARD = "dashboard"
    const val PROFILE = "profile"
    const val TRANSACTIONS = "transactions"
    const val ADD_TRANSACTION = "add_transaction"
    const val EDIT_TRANSACTION = "edit_transaction"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val authStatus by authViewModel.authStatus.collectAsState()

    LaunchedEffect(authStatus) {
        when (authStatus) {
            AuthStatus.AUTHENTICATED -> {
                navController.navigate(Routes.DASHBOARD) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            }
            AuthStatus.UNAUTHENTICATED -> {
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
                onSignUpClick = { navController.navigate(Routes.SIGN_UP) },
                authViewModel = authViewModel
            )
        }

        composable(Routes.SIGN_UP) {
            SignUpScreen(
                onLoginClick = { navController.popBackStack() },
                authViewModel = authViewModel
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onProfileClick = { navController.navigate(Routes.PROFILE) },
                onTransactionsClick = { navController.navigate(Routes.TRANSACTIONS) }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onLogout = { authViewModel.logout() }
            )
        }

        composable(Routes.TRANSACTIONS) {
            TransactionsListScreen(
                onAddTransactionClick = {
                    navController.navigate(Routes.ADD_TRANSACTION)
                },
                onEditTransactionClick = { transactionId ->
                    navController.navigate("${Routes.EDIT_TRANSACTION}/$transactionId")
                }
            )
        }

        composable(Routes.ADD_TRANSACTION) {
            AddTransactionScreen(
                onTransactionSaved = { navController.popBackStack() }
            )
        }

        composable("${Routes.EDIT_TRANSACTION}/{transactionId}") { backStackEntry ->
            val transactionId =
                backStackEntry.arguments?.getString("transactionId") ?: return@composable

            EditTransactionScreen(
                transactionId = transactionId,
                onDone = { navController.popBackStack() }
            )
        }
    }
}

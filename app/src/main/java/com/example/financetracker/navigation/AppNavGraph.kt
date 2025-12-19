package com.example.financetracker.navigation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.financetracker.ui.auth.LoginScreen
import com.example.financetracker.ui.auth.SignUpScreen
import com.example.financetracker.ui.categories.CategoriesManagementScreen
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
    const val CATEGORIES = "categories"
}

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Dashboard : BottomNavItem(Routes.DASHBOARD, Icons.Default.Home, "Dashboard")
    object Transactions : BottomNavItem(Routes.TRANSACTIONS, Icons.Default.List, "Transactions")
    object Profile : BottomNavItem(Routes.PROFILE, Icons.Default.Person, "Profile")
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val bottomNavRoutes = setOf(Routes.DASHBOARD, Routes.TRANSACTIONS, Routes.PROFILE)

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

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomNavRoutes) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.LOGIN,
            modifier = Modifier.padding(innerPadding)
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
                    onNavigateToCategories = { navController.navigate(Routes.CATEGORIES) }
                )
            }
            composable(Routes.TRANSACTIONS) {
                TransactionsListScreen(
                    onAddTransactionClick = { navController.navigate(Routes.ADD_TRANSACTION) },
                    onEditTransactionClick = { transactionId ->
                        navController.navigate("${Routes.EDIT_TRANSACTION}/$transactionId")
                    }
                )
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onLogout = { authViewModel.logout() },
                    onClearData = { authViewModel.clearAllUserData() }
                )
            }
            composable(Routes.CATEGORIES) {
                CategoriesManagementScreen(
                )
            }
            composable(Routes.ADD_TRANSACTION) {
                AddTransactionScreen(onTransactionSaved = { navController.popBackStack() })
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
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Transactions,
        BottomNavItem.Profile
    )
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
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

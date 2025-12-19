package com.example.financetracker.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financetracker.viewmodel.DashboardViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardScreen(
    dashboardViewModel: DashboardViewModel = viewModel(),
    onNavigateToCategories: () -> Unit
) {
    val uiState by dashboardViewModel.uiState.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Welcome, ${uiState.username}",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            BalanceCard(
                balance = currencyFormat.format(uiState.totalBalance),
                income = currencyFormat.format(uiState.totalIncome),
                expense = currencyFormat.format(uiState.totalExpense)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onNavigateToCategories,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Manage Categories")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Recent Transactions", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.recentTransactions.isEmpty()) {
                Text("No recent transactions.", style = MaterialTheme.typography.bodyMedium)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.recentTransactions) { transaction ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(transaction.title)
                            Text(
                                text = currencyFormat.format(transaction.amount),
                                color = if (transaction.type == com.example.financetracker.data.model.TransactionType.INCOME) Color(
                                    0xFF00FF67
                                ) else Color.Red
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BalanceCard(balance: String, income: String, expense: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Total Balance", style = MaterialTheme.typography.titleMedium)
            Text(
                text = balance,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = if (balance.startsWith("-")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Income", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = income,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF00C853)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Expense", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = expense,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Red
                    )
                }
            }
        }
    }
}

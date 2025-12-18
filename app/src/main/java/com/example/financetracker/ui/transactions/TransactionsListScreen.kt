package com.example.financetracker.ui.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financetracker.data.model.Transaction
import com.example.financetracker.viewmodel.TransactionViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsListScreen(
    onAddTransactionClick: () -> Unit,
    onEditTransactionClick: (String) -> Unit,
    viewModel: TransactionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            viewModel.loadTransactions(userId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Transactions") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTransactionClick) {
                Text("+")
            }
        }
    ) { paddingValues ->

        if (uiState.transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No transactions found")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.transactions) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onClick = {
                            onEditTransactionClick(transaction.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: Transaction,
    onClick: (Transaction) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(transaction) }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = transaction.title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "$${transaction.amount}",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

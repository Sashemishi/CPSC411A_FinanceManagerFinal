package com.example.financetracker.ui.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financetracker.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionScreen(
    transactionId: String,
    onDone: () -> Unit,
    viewModel: TransactionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val transaction = uiState.transactions.find { it.id == transactionId }

    var showDeleteDialog by remember { mutableStateOf(false) }

    if (transaction == null) {
        Text("Transaction not found")
        return
    }

    var title by remember { mutableStateOf(transaction.title) }
    var amount by remember { mutableStateOf(transaction.amount.toString()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Transaction") },
                actions = {
                    TextButton(onClick = { showDeleteDialog = true }) {
                        Text("Delete")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    viewModel.updateTransaction(
                        transaction.copy(
                            title = title,
                            amount = amount.toDoubleOrNull() ?: transaction.amount
                        )
                    )
                    onDone()
                }
            ) {
                Text("Save Changes")
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Transaction") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(transaction.id)
                        showDeleteDialog = false
                        onDone()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

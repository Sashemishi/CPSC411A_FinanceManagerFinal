package com.example.financetracker.ui.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financetracker.viewmodel.TransactionViewModel
import com.example.financetracker.data.model.Transaction
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onTransactionSaved: () -> Unit,
    viewModel: TransactionViewModel = viewModel()
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Transaction") }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category") },
                modifier = Modifier.fillMaxWidth()
            )

            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid

                    if (title.isBlank() || amount.isBlank() || userId == null) {
                        error = "Please fill all fields"
                        return@Button
                    }

                    val transaction = Transaction(
                        id = UUID.randomUUID().toString(),
                        userId = userId,
                        title = title,
                        amount = amount.toDoubleOrNull() ?: 0.0,
                        categoryId = category,
                        date = System.currentTimeMillis()
                    )

                    viewModel.addTransaction(transaction)
                    onTransactionSaved()
                }
            ) {
                Text("Save Transaction")
            }
        }
    }
}
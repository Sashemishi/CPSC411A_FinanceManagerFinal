package com.example.financetracker.ui.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financetracker.data.model.Category
import com.example.financetracker.data.model.Transaction
import com.example.financetracker.data.model.TransactionType
import com.example.financetracker.viewmodel.TransactionViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onTransactionSaved: () -> Unit,
    viewModel: TransactionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories = uiState.categories

    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var isCategoryMenuExpanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var hasTitleBeenFocused by remember { mutableStateOf(false) }
    var hasAmountBeenFocused by remember { mutableStateOf(false) }

    val isTitleValid = title.isNotBlank()
    val isAmountValid = amount.matches(Regex("^\\d*\\.?\\d+\$")) && (amount.toDoubleOrNull() ?: 0.0) > 0

    LaunchedEffect(categories) {
        if (selectedCategory == null && categories.isNotEmpty()) {
            selectedCategory = categories.first()
        }
    }

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
                onValueChange = {
                    title = it
                    hasTitleBeenFocused = true
                },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                // --- NEW: Visual error handling ---
                isError = !isTitleValid && hasTitleBeenFocused,
                supportingText = {
                    if (!isTitleValid && hasTitleBeenFocused) {
                        Text("Title cannot be empty")
                    }
                }
            )

            OutlinedTextField(
                value = amount,
                onValueChange = {
                    amount = it
                    hasAmountBeenFocused = true
                },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = !isAmountValid && hasAmountBeenFocused,
                supportingText = {
                    if (!isAmountValid && hasAmountBeenFocused) {
                        Text("Please enter a valid positive number")
                    }
                }
            )

            ExposedDropdownMenuBox(
                expanded = isCategoryMenuExpanded,
                onExpandedChange = { isCategoryMenuExpanded = !isCategoryMenuExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    readOnly = true,
                    value = selectedCategory?.name ?: "Select a category",
                    onValueChange = {},
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryMenuExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = isCategoryMenuExpanded,
                    onDismissRequest = { isCategoryMenuExpanded = false }
                ) {
                    if (categories.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No categories available") },
                            enabled = false,
                            onClick = {}
                        )
                    } else {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategory = category
                                    isCategoryMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedType == TransactionType.EXPENSE,
                    onClick = { selectedType = TransactionType.EXPENSE },
                    label = { Text("Expense") }
                )
                FilterChip(
                    selected = selectedType == TransactionType.INCOME,
                    onClick = { selectedType = TransactionType.INCOME },
                    label = { Text("Income") }
                )
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = isTitleValid && isAmountValid && selectedCategory != null,
                onClick = {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    val finalAmount = amount.toDoubleOrNull()
                    val currentCategory = selectedCategory

                    if (userId == null || finalAmount == null || currentCategory == null) {
                        return@Button
                    }

                    val transaction = Transaction(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        amount = finalAmount,
                        type = selectedType,
                        categoryId = currentCategory.id,
                        date = System.currentTimeMillis(),
                        note = note,
                        userId = userId
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

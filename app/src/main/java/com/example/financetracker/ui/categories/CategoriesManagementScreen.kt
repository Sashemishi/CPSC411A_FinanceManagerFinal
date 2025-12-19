package com.example.financetracker.ui.categories

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financetracker.data.model.Category
import com.example.financetracker.data.model.Transaction
import com.example.financetracker.viewmodel.CascadeAction
import com.example.financetracker.viewmodel.CategoryViewModel
import com.example.financetracker.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesManagementScreen(
    categoryViewModel: CategoryViewModel = viewModel(),
    transactionViewModel: TransactionViewModel = viewModel()
) {
    val categoryUiState by categoryViewModel.uiState.collectAsState()
    val transactionUiState by transactionViewModel.uiState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Category?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Category?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Manage Categories") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (categoryUiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (categoryUiState.categories.isEmpty()) {
                Text("No categories yet. Add one!", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categoryUiState.categories) { category ->
                        CategoryItem(
                            category = category,
                            onEditClick = { showEditDialog = it },
                            onDeleteClick = { showDeleteDialog = it }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            CategoryAddDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { categoryName ->
                    categoryViewModel.addCategory(categoryName, 0xFF6200EE)
                    showAddDialog = false
                }
            )
        }

        showEditDialog?.let { category ->
            CategoryEditDialog(
                category = category,
                onDismiss = { showEditDialog = null },
                onConfirm = { updatedCategory ->
                    categoryViewModel.updateCategory(updatedCategory)
                    showEditDialog = null
                }
            )
        }

        showDeleteDialog?.let { category ->
            val associatedTransactions = transactionUiState.transactions
                .filter { it.categoryId == category.id }

            val categoriesForReassignment = categoryUiState.categories
                .filter { it.id != category.id }

            CategoryDeleteDialog(
                category = category,
                associatedTransactions = associatedTransactions,
                categoriesForReassignment = categoriesForReassignment,
                onDismiss = { showDeleteDialog = null },
                onConfirm = { action, newCategoryId ->
                    val transactionIds = associatedTransactions.map { it.id }
                    categoryViewModel.handleCascadeAction(
                        categoryId = category.id,
                        transactionIds = transactionIds,
                        action = action,
                        newCategoryId = newCategoryId
                    )
                    showDeleteDialog = null
                }
            )
        }
    }
}

@Composable
private fun CategoryItem(
    category: Category,
    onEditClick: (Category) -> Unit,
    onDeleteClick: (Category) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyLarge
            )
            Row {
                IconButton(onClick = { onEditClick(category) }) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Category")
                }
                IconButton(onClick = { onDeleteClick(category) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Category",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryAddDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var categoryName by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Category") },
        text = {
            OutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                label = { Text("Category Name") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(categoryName) },
                enabled = categoryName.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun CategoryEditDialog(category: Category, onDismiss: () -> Unit, onConfirm: (Category) -> Unit) {
    var categoryName by remember { mutableStateOf(category.name) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Category") },
        text = {
            OutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                label = { Text("Category Name") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(category.copy(name = categoryName)) },
                enabled = categoryName.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDeleteDialog(
    category: Category,
    associatedTransactions: List<Transaction>,
    categoriesForReassignment: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (CascadeAction, String?) -> Unit
) {
    var selectedOption by remember { mutableStateOf(CascadeAction.DELETE_TRANSACTIONS) }
    var expanded by remember { mutableStateOf(false) }
    var selectedReassignCategory by remember { mutableStateOf(categoriesForReassignment.firstOrNull()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete '${category.name}'?") },
        text = {
            Column {
                if (associatedTransactions.isNotEmpty()) {
                    Text("This category has ${associatedTransactions.size} associated transaction(s). What would you like to do?")
                    Spacer(Modifier.height(16.dp))

                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedOption == CascadeAction.DELETE_TRANSACTIONS,
                            onClick = { selectedOption = CascadeAction.DELETE_TRANSACTIONS }
                        )
                        Text("Delete all associated transactions", modifier = Modifier.padding(start = 8.dp))
                    }

                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedOption == CascadeAction.REASSIGN_TRANSACTIONS,
                            onClick = { selectedOption = CascadeAction.REASSIGN_TRANSACTIONS }
                        )
                        Text("Reassign transactions to:", modifier = Modifier.padding(start = 8.dp))
                    }

                    if (selectedOption == CascadeAction.REASSIGN_TRANSACTIONS) {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.padding(start = 48.dp)
                        ) {
                            OutlinedTextField(
                                modifier = Modifier.menuAnchor(),
                                value = selectedReassignCategory?.name ?: "No other categories",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                categoriesForReassignment.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.name) },
                                        onClick = {
                                            selectedReassignCategory = cat
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text("Are you sure you want to delete this category? This action cannot be undone.")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(selectedOption, selectedReassignCategory?.id)
                },
                enabled = !(selectedOption == CascadeAction.REASSIGN_TRANSACTIONS && selectedReassignCategory == null),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

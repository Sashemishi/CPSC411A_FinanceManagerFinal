package com.example.financetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetracker.data.model.Category
import com.example.financetracker.data.repository.CategoryRepository
import com.example.financetracker.data.repository.TransactionRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class CascadeAction {
    DELETE_TRANSACTIONS,
    REASSIGN_TRANSACTIONS
}

class CategoryViewModel(
    private val categoryRepository: CategoryRepository = CategoryRepository(),
    private val transactionRepository: TransactionRepository = TransactionRepository()
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val userId = auth.currentUser?.uid ?: run {
                _uiState.update { it.copy(isLoading = false, error = "User not logged in.") }
                return@launch
            }

            categoryRepository.getCategories(userId)
                .catch { exception ->
                    _uiState.update { it.copy(error = exception.message, isLoading = false) }
                }
                .collect { categories ->
                    _uiState.update {
                        it.copy(categories = categories, isLoading = false)
                    }
                }
        }
    }

    fun addCategory(name: String, color: Long) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId == null || name.isBlank()) {
                _uiState.update { it.copy(error = "User not logged in or category name is empty.") }
                return@launch
            }

            val category = Category(
                userId = userId,
                name = name.trim(),
                color = color
            )
            try {
                categoryRepository.addCategory(category)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to add category") }
            }
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            if (category.name.isBlank()) {
                _uiState.update { it.copy(error = "Category name cannot be empty.") }
                return@launch
            }
            try {
                categoryRepository.updateCategory(category)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to update category") }
            }
        }
    }

    private fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            try {
                categoryRepository.deleteCategory(categoryId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to delete category") }
            }
        }
    }

    fun handleCascadeAction(
        categoryId: String,
        transactionIds: List<String>,
        action: CascadeAction,
        newCategoryId: String? = null
    ) {
        viewModelScope.launch {
            try {
                when (action) {
                    CascadeAction.DELETE_TRANSACTIONS -> {
                        if (transactionIds.isNotEmpty()) {
                            transactionRepository.deleteTransactions(transactionIds)
                        }
                    }
                    CascadeAction.REASSIGN_TRANSACTIONS -> {
                        if (newCategoryId != null && transactionIds.isNotEmpty()) {
                            transactionRepository.updateCategoryForTransactions(transactionIds, newCategoryId)
                        }
                    }
                }
                deleteCategory(categoryId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed during cascade delete operation") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.ReceiptRepository
import com.example.data.repository.VaultStats
import com.example.domain.model.ReceiptCategory
import com.example.domain.model.ReceiptItem
import com.example.domain.model.WarrantyStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortOption(val title: String) {
    EXPIRY_DATE_ASC("Expiry (Soonest)"),
    EXPIRY_DATE_DESC("Expiry (Furthest)"),
    PURCHASE_DATE_DESC("Newest Purchase"),
    PURCHASE_DATE_ASC("Oldest Purchase"),
    AMOUNT_DESC("Price (High to Low)"),
    AMOUNT_ASC("Price (Low to High)"),
    MERCHANT_ASC("Merchant (A-Z)")
}

data class ReceiptUiState(
    val searchQuery: String = "",
    val selectedCategory: ReceiptCategory? = null,
    val selectedSortOption: SortOption = SortOption.EXPIRY_DATE_ASC,
    val selectedStatusFilter: WarrantyStatus? = null,
    val isLoading: Boolean = false
)

class ReceiptViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ReceiptRepository(application)
    private val userPrefs = UserPreferencesRepository(application)

    private val _uiState = MutableStateFlow(ReceiptUiState())
    val uiState: StateFlow<ReceiptUiState> = _uiState

    val vaultStats: StateFlow<VaultStats> = repository.vaultStats.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VaultStats()
    )

    val expiringSoonReceipts: StateFlow<List<ReceiptItem>> = repository.expiringSoonReceipts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val filteredReceipts: StateFlow<List<ReceiptItem>> = combine(
        repository.allReceipts,
        _uiState
    ) { allItems, state ->
        var result = allItems

        // Search filter
        if (state.searchQuery.isNotBlank()) {
            val q = state.searchQuery.trim().lowercase()
            result = result.filter { item ->
                item.merchantName.lowercase().contains(q) ||
                        item.itemName.lowercase().contains(q) ||
                        item.notes.lowercase().contains(q) ||
                        item.category.displayName.lowercase().contains(q)
            }
        }

        // Category filter
        if (state.selectedCategory != null) {
            result = result.filter { it.category == state.selectedCategory }
        }

        // Status filter
        if (state.selectedStatusFilter != null) {
            result = result.filter { it.warrantyStatus == state.selectedStatusFilter }
        }

        // Sorting
        result = when (state.selectedSortOption) {
            SortOption.EXPIRY_DATE_ASC -> result.sortedBy { it.warrantyExpiryDateMillis }
            SortOption.EXPIRY_DATE_DESC -> result.sortedByDescending { it.warrantyExpiryDateMillis }
            SortOption.PURCHASE_DATE_DESC -> result.sortedByDescending { it.purchaseDateMillis }
            SortOption.PURCHASE_DATE_ASC -> result.sortedBy { it.purchaseDateMillis }
            SortOption.AMOUNT_DESC -> result.sortedByDescending { it.totalAmount }
            SortOption.AMOUNT_ASC -> result.sortedBy { it.totalAmount }
            SortOption.MERCHANT_ASC -> result.sortedBy { it.merchantName.lowercase() }
        }

        result
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            val isSeeded = userPrefs.isInitialSampleSeeded.first()
            if (!isSeeded) {
                repository.seedSampleReceiptsIfEmpty()
                userPrefs.setInitialSampleSeeded(true)
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onCategorySelected(category: ReceiptCategory?) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = if (_uiState.value.selectedCategory == category) null else category
        )
    }

    fun onStatusFilterSelected(status: WarrantyStatus?) {
        _uiState.value = _uiState.value.copy(
            selectedStatusFilter = if (_uiState.value.selectedStatusFilter == status) null else status
        )
    }

    fun onSortOptionSelected(sortOption: SortOption) {
        _uiState.value = _uiState.value.copy(selectedSortOption = sortOption)
    }

    fun deleteReceipt(receipt: ReceiptItem) {
        viewModelScope.launch {
            repository.deleteReceipt(receipt)
        }
    }

    fun updateReceipt(receipt: ReceiptItem) {
        viewModelScope.launch {
            repository.updateReceipt(receipt)
        }
    }

    fun saveReceipt(receipt: ReceiptItem, onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.insertReceipt(receipt)
            onComplete(id)
        }
    }

    fun resetToSampleData() {
        viewModelScope.launch {
            repository.clearAll()
            repository.seedSampleReceiptsIfEmpty()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun getReceiptFlow(id: Long) = repository.getReceiptById(id)
}

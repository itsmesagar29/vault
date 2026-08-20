package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.ReceiptRepository
import com.example.domain.model.ReceiptItem
import com.example.worker.NotificationHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val userPrefs = UserPreferencesRepository(application)
    private val repository = ReceiptRepository(application)

    val allReceipts: StateFlow<List<ReceiptItem>> = repository.allReceipts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val themeMode: StateFlow<String> = userPrefs.themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "SYSTEM"
    )

    val defaultWarrantyMonths: StateFlow<Int> = userPrefs.defaultWarrantyMonths.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 12
    )

    val reminderDaysBefore: StateFlow<Int> = userPrefs.reminderDaysBefore.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 7
    )

    val defaultCurrency: StateFlow<String> = userPrefs.defaultCurrency.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "$"
    )

    val notificationsEnabled: StateFlow<Boolean> = userPrefs.notificationsEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            userPrefs.setThemeMode(mode)
        }
    }

    fun setDefaultWarrantyMonths(months: Int) {
        viewModelScope.launch {
            userPrefs.setDefaultWarrantyMonths(months)
        }
    }

    fun setReminderDaysBefore(days: Int) {
        viewModelScope.launch {
            userPrefs.setReminderDaysBefore(days)
        }
    }

    fun setDefaultCurrency(currency: String) {
        viewModelScope.launch {
            userPrefs.setDefaultCurrency(currency)
            repository.updateAllCurrencies(currency)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPrefs.setNotificationsEnabled(enabled)
        }
    }

    fun triggerTestNotification() {
        NotificationHelper.showTestNotification(
            getApplication(),
            "🔔 BillVault Reminder Alert",
            "Your 2-Year Warranty for 'MacBook Pro' is expiring in 5 days! Tap to view claim details."
        )
    }

    fun resetDataToSamples() {
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
}

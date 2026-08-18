package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode") // SYSTEM, LIGHT, DARK
        val DEFAULT_WARRANTY_MONTHS = intPreferencesKey("default_warranty_months")
        val REMINDER_DAYS_BEFORE = intPreferencesKey("reminder_days_before")
        val DEFAULT_CURRENCY = stringPreferencesKey("default_currency")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val INITIAL_SAMPLE_SEEDED = booleanPreferencesKey("initial_sample_seeded")
    }

    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.THEME_MODE] ?: "SYSTEM"
    }

    val defaultWarrantyMonths: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DEFAULT_WARRANTY_MONTHS] ?: 12
    }

    val reminderDaysBefore: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REMINDER_DAYS_BEFORE] ?: 7
    }

    val defaultCurrency: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DEFAULT_CURRENCY] ?: "₹"
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
    }

    val isInitialSampleSeeded: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.INITIAL_SAMPLE_SEEDED] ?: false
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    suspend fun setDefaultWarrantyMonths(months: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_WARRANTY_MONTHS] = months
        }
    }

    suspend fun setReminderDaysBefore(days: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDER_DAYS_BEFORE] = days
        }
    }

    suspend fun setDefaultCurrency(currency: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_CURRENCY] = currency
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setInitialSampleSeeded(seeded: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.INITIAL_SAMPLE_SEEDED] = seeded
        }
    }
}

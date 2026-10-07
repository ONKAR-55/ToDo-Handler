package com.example.todohandler.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {
    companion object {
        val THEME_KEY = stringPreferencesKey("theme")
        val WINDOW_START_NOTIFICATION_KEY = booleanPreferencesKey("window_start_notification")
        val PRE_END_WRAP_WARNING_KEY = booleanPreferencesKey("pre_end_wrap_warning")
    }

    val themeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: "Dark"
    }

    val windowStartNotificationFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[WINDOW_START_NOTIFICATION_KEY] ?: true
    }

    val preEndWrapWarningFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PRE_END_WRAP_WARNING_KEY] ?: true
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
        }
    }

    suspend fun setWindowStartNotification(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[WINDOW_START_NOTIFICATION_KEY] = enabled
        }
    }

    suspend fun setPreEndWrapWarning(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PRE_END_WRAP_WARNING_KEY] = enabled
        }
    }
}

package com.example.life4pollinators.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.life4pollinators.data.models.Theme
import kotlinx.coroutines.flow.map

/**
 * Repository per la gestione delle impostazioni: logica del cambio tema.
 *
 * Gestisce il tema dell’app usando DataStore (persistenza locale).
 * Espone un flow con il tema corrente e una funzione per modificarlo.
 */
class SettingsRepository (
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val THEME_KEY = stringPreferencesKey("theme")
    }

    val theme = dataStore.data
        .map { preferences ->
            try {
                Theme.valueOf(preferences[THEME_KEY] ?: "System")
            } catch (_: Exception) {
                Theme.System
            }
        }

    suspend fun setTheme(theme: Theme) =
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.toString()
        }
}
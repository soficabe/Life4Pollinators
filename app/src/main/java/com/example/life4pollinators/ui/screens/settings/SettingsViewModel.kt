package com.example.life4pollinators.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.life4pollinators.data.models.Theme
import com.example.life4pollinators.data.repositories.AuthRepository
import com.example.life4pollinators.data.repositories.SettingsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsState(
    val theme: Theme,
    val isAuthenticated: Boolean = false
)

interface SettingsActions {
    fun changeTheme(theme: Theme) : Job
    fun logout()
}

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val state = settingsRepository.theme.map { SettingsState(it) }.stateIn (
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = SettingsState(Theme.System)
    )

    val actions = object : SettingsActions {
        override fun changeTheme(theme: Theme) =
            viewModelScope.launch { settingsRepository.setTheme(theme) }

        override fun logout() {
            viewModelScope.launch {
                authRepository.signOut()
            }
        }
    }
}
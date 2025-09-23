package com.example.life4pollinators.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.life4pollinators.R
import com.example.life4pollinators.data.models.Theme
import com.example.life4pollinators.data.repositories.AuthRepository
import com.example.life4pollinators.data.repositories.ChangePasswordResult
import com.example.life4pollinators.data.repositories.SettingsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Stato della schermata impostazioni.
 *
 * @param theme Tema attuale selezionato
 * @param isAuthenticated True se l'utente è loggato
 * @param changePasswordResult Risultato ultimo cambio password
 * @param isChangingPassword True se il cambio password è in corso
 * @param changePasswordError Id risorsa errore cambio password
 */
data class SettingsState(
    val theme: Theme = Theme.System,
    val isAuthenticated: Boolean = false,
    val changePasswordResult: ChangePasswordResult? = null,
    val isChangingPassword: Boolean = false,
    val changePasswordError: Int? = null
)

/**
 * Azioni disponibili nella schermata impostazioni.
 */
interface SettingsActions {
    fun changeTheme(theme: Theme): Job
    fun logout() : Job
    fun changePassword(newPassword: String, confirmPassword: String)  // Rimosso currentPassword
    fun clearChangePasswordError()
}

/**
 * ViewModel per la schermata delle impostazioni.
 * Gestisce tema, logout, cambio password.
 */
class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.theme.collect { theme ->
                _state.update { it.copy(theme = theme) }
            }
        }
    }

    val actions = object : SettingsActions {
        override fun changeTheme(theme: Theme) =
            viewModelScope.launch {
                settingsRepository.setTheme(theme)
            }

        override fun logout() =
            viewModelScope.launch {
                authRepository.signOut()
            }

        override fun changePassword(newPassword: String, confirmPassword: String) {  // Rimosso currentPassword
            _state.update { it.copy(isChangingPassword = true, changePasswordError = null) }
            viewModelScope.launch {
                val result = authRepository.changePassword(newPassword, confirmPassword)  // Rimosso currentPassword
                _state.update {
                    it.copy(
                        changePasswordResult = result,
                        isChangingPassword = false,
                        changePasswordError =
                        when (result) {
                            is ChangePasswordResult.Error.RequiredFields -> R.string.requiredFields_error
                            is ChangePasswordResult.Error.WeakPassword -> R.string.weakPassword
                            is ChangePasswordResult.Error.PasswordMismatch -> R.string.passwordNotMatch_error
                            is ChangePasswordResult.Error.NetworkError -> R.string.network_error
                            is ChangePasswordResult.Error.UnknownError -> R.string.generic_change_psw_error
                            else -> null
                        }
                    )
                }
            }
        }

        override fun clearChangePasswordError() {
            _state.update { it.copy(changePasswordError = null, changePasswordResult = null) }
        }
    }
}
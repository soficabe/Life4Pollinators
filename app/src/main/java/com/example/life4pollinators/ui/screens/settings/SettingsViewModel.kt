package com.example.life4pollinators.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.life4pollinators.R
import com.example.life4pollinators.data.models.Theme
import com.example.life4pollinators.data.repositories.AuthRepository
import com.example.life4pollinators.data.repositories.ChangePasswordResult
import com.example.life4pollinators.data.repositories.SignOutResult
import com.example.life4pollinators.data.repositories.SettingsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Stato della schermata impostazioni.
 */
data class SettingsState(
    val theme: Theme = Theme.System,
    val isAuthenticated: Boolean = false,
    val changePasswordResult: ChangePasswordResult? = null,
    val isChangingPassword: Boolean = false,
    val changePasswordError: Int? = null,
    val newPasswordError: Int? = null,
    val confirmPasswordError: Int? = null,
    val signOutError: Int? = null,
    val isLoggingOut: Boolean = false
)

/**
 * Azioni disponibili nella schermata impostazioni.
 */
interface SettingsActions {
    fun changeTheme(theme: Theme): Job
    fun logout() : Job
    fun changePassword(newPassword: String, confirmPassword: String)
    fun clearChangePasswordError()
    fun clearSignOutError()
}

/**
 * ViewModel per la schermata delle impostazioni.
 * Gestisce tema, logout, cambio password e validazione lato client.
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
                _state.update { it.copy(signOutError = null, isLoggingOut = true) }

                when (authRepository.signOut()) {
                    is SignOutResult.Success -> {
                        _state.update { it.copy(isLoggingOut = false) }
                    }
                    is SignOutResult.Error.NetworkError -> {
                        _state.update {
                            it.copy(
                                signOutError = R.string.network_error_connection,
                                isLoggingOut = false
                            )
                        }
                    }
                    is SignOutResult.Error.UnknownError -> {
                        _state.update {
                            it.copy(
                                signOutError = R.string.unknown_error,
                                isLoggingOut = false
                            )
                        }
                    }
                }
            }

        /**
         * Cambio password con validazione lato client e feedback immediato.
         */
        override fun changePassword(newPassword: String, confirmPassword: String) {
            // Reset errori precedenti
            _state.update {
                it.copy(
                    isChangingPassword = false,
                    changePasswordError = null,
                    newPasswordError = null,
                    confirmPasswordError = null
                )
            }

            // Validazione lato client
            var hasError = false
            if (newPassword.isBlank()) {
                _state.update { it.copy(newPasswordError = R.string.requiredFields_error) }
                hasError = true
            } else if (newPassword.length < 6) {
                _state.update { it.copy(newPasswordError = R.string.weakPassword) }
                hasError = true
            }
            if (confirmPassword.isBlank()) {
                _state.update { it.copy(confirmPasswordError = R.string.requiredFields_error) }
                hasError = true
            } else if (newPassword != confirmPassword) {
                _state.update { it.copy(confirmPasswordError = R.string.passwordNotMatch_error) }
                hasError = true
            }
            if (hasError) return

            // Se tutto ok, procediamo col repository
            _state.update { it.copy(isChangingPassword = true) }
            viewModelScope.launch {
                val result = authRepository.changePassword(newPassword)
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
            _state.update {
                it.copy(
                    changePasswordError = null,
                    changePasswordResult = null,
                    newPasswordError = null,
                    confirmPasswordError = null
                )
            }
        }

        override fun clearSignOutError() {
            _state.update { it.copy(signOutError = null) }
        }
    }
}
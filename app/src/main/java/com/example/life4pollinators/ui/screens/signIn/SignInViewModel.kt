package com.example.life4pollinators.ui.screens.signIn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.life4pollinators.R
import com.example.life4pollinators.data.repositories.AuthRepository
import com.example.life4pollinators.data.repositories.SignInResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Stato della schermata di login.
 * Include errori per campo per validazione lato client.
 */
data class SignInState(
    val email: String = "",
    val psw: String = "",
    val signInResult: SignInResult? = null,
    val errorMessageRes: Int? = null, // errore generico (backend)
    val emailError: Int? = null,
    val passwordError: Int? = null,
    val isLoading: Boolean = false
)

/**
 * Interface che definisce tutte le azioni possibili nella schermata di SignIn.
 */
interface SignInActions {
    fun setEmail(email: String)
    fun setPsw(psw: String)
    fun signIn()
    fun clearError()
}

/**
 * ViewModel per la schermata di login utente.
 * Gestisce stato, azioni e logica di autenticazione.
 * Effettua la validazione lato client.
 */
class SignInViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()

    val actions = object : SignInActions {
        override fun setEmail(email: String) {
            _state.update { it.copy(email = email, emailError = null, errorMessageRes = null) }
        }

        override fun setPsw(psw: String) {
            _state.update { it.copy(psw = psw, passwordError = null, errorMessageRes = null) }
        }

        /**
         * Esegue il sign in dell'utente con validazione lato client.
         */
        override fun signIn() {
            val currentState = _state.value

            // Reset errori precedenti
            _state.update {
                it.copy(
                    errorMessageRes = null,
                    emailError = null,
                    passwordError = null
                )
            }

            // Validazione lato client
            var hasError = false
            if (currentState.email.isBlank()) {
                _state.update { it.copy(emailError = R.string.required_fields) }
                hasError = true
            } else if (!currentState.email.contains("@") || !currentState.email.contains(".")) {
                _state.update { it.copy(emailError = R.string.invalid_email) }
                hasError = true
            }
            if (currentState.psw.isBlank()) {
                _state.update { it.copy(passwordError = R.string.required_fields) }
                hasError = true
            }
            if (hasError) return

            // Solo se tutto ok procedi col repository
            _state.update {
                it.copy(
                    signInResult = SignInResult.Loading,
                    isLoading = true
                )
            }

            viewModelScope.launch {
                try {
                    val result = authRepository.signIn(
                        email = currentState.email,
                        password = currentState.psw
                    )

                    _state.update {
                        it.copy(
                            signInResult = result,
                            isLoading = false
                        )
                    }

                    // Gestione messaggi di errore specifici
                    when (result) {
                        SignInResult.Loading -> {}
                        SignInResult.Success -> {
                            _state.update { it.copy(errorMessageRes = null) }
                        }
                        is SignInResult.Error -> {
                            val errorMessageRes = when (result) {
                                SignInResult.Error.InvalidCredentials ->
                                    R.string.invalid_credentials
                                SignInResult.Error.RequiredFields ->
                                    R.string.required_fields
                                SignInResult.Error.InvalidEmail ->
                                    R.string.invalid_email
                                SignInResult.Error.NetworkError ->
                                    R.string.network_error
                                is SignInResult.Error.UnknownError ->
                                    R.string.unknown_error
                            }
                            _state.update { it.copy(errorMessageRes = errorMessageRes) }
                        }
                    }
                } catch (e: Exception) {
                    _state.update {
                        it.copy(
                            signInResult = SignInResult.Error.UnknownError(e),
                            errorMessageRes = R.string.unknown_error,
                            isLoading = false
                        )
                    }
                }
            }
        }

        override fun clearError() {
            _state.update {
                it.copy(
                    errorMessageRes = null,
                    signInResult = null,
                    emailError = null,
                    passwordError = null
                )
            }
        }
    }
}
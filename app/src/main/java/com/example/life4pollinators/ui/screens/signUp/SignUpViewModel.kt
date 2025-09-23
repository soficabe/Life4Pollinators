package com.example.life4pollinators.ui.screens.signUp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.life4pollinators.R
import com.example.life4pollinators.data.repositories.AuthRepository
import com.example.life4pollinators.data.repositories.SignUpResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Data class rappresentante lo stato della schermata di registrazione.
 * Ora include errori per campo per validazione lato client.
 */
data class SignUpState(
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val psw: String = "",
    val confirmPsw: String = "",
    val signUpResult: SignUpResult = SignUpResult.Loading,
    val isLoading: Boolean = false,
    val errorMessage: Int? = null, // Errore generico (backend)
    val usernameError: Int? = null,
    val firstNameError: Int? = null,
    val lastNameError: Int? = null,
    val emailError: Int? = null,
    val passwordError: Int? = null,
    val confirmPasswordError: Int? = null
)

/**
 * Interface che definisce tutte le azioni possibili nella schermata di registrazione.
 */
interface SignUpActions {
    fun setUsername(username: String)
    fun setFirstName(firstName: String)
    fun setLastName(lastName: String)
    fun setEmail(email: String)
    fun setPsw(psw: String)
    fun setConfirmPsw(confirmPsw: String)
    fun signUp()
    fun clearError()
}

/**
 * ViewModel per la schermata di registrazione utente.
 * Gestisce lo stato della UI e la logica di business per la registrazione.
 * Include validazione lato client immediata.
 */
class SignUpViewModel (
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SignUpState())
    val state = _state.asStateFlow()

    //Implementazione azioni di registrazione
    val actions = object : SignUpActions {
        override fun setUsername(username: String) =
            _state.update { it.copy(username = username.trim(), usernameError = null, errorMessage = null) }

        override fun setFirstName(firstName: String) =
            _state.update { it.copy(firstName = firstName.trim(), firstNameError = null, errorMessage = null) }

        override fun setLastName(lastName: String) =
            _state.update { it.copy(lastName = lastName.trim(), lastNameError = null, errorMessage = null) }

        override fun setEmail(email: String) =
            _state.update { it.copy(email = email.trim().lowercase(), emailError = null, errorMessage = null) }

        override fun setPsw(psw: String) =
            _state.update { it.copy(psw = psw, passwordError = null, errorMessage = null) }

        override fun setConfirmPsw(confirmPsw: String) =
            _state.update { it.copy(confirmPsw = confirmPsw, confirmPasswordError = null, errorMessage = null) }

        /**
         * Esegue la registrazione dell'utente con validazione lato client.
         */
        override fun signUp() {
            val currentState = _state.value

            // Reset errori precedenti
            _state.update {
                it.copy(
                    errorMessage = null,
                    usernameError = null,
                    firstNameError = null,
                    lastNameError = null,
                    emailError = null,
                    passwordError = null,
                    confirmPasswordError = null
                )
            }

            // Validazione lato client
            var hasError = false
            if (currentState.username.isBlank()) {
                _state.update { it.copy(usernameError = R.string.requiredFields_error) }
                hasError = true
            }
            if (currentState.firstName.isBlank()) {
                _state.update { it.copy(firstNameError = R.string.requiredFields_error) }
                hasError = true
            }
            if (currentState.lastName.isBlank()) {
                _state.update { it.copy(lastNameError = R.string.requiredFields_error) }
                hasError = true
            }
            if (currentState.email.isBlank()) {
                _state.update { it.copy(emailError = R.string.requiredFields_error) }
                hasError = true
            } else if (!currentState.email.contains("@") || !currentState.email.contains(".")) {
                _state.update { it.copy(emailError = R.string.email_invalid_format) }
                hasError = true
            }
            if (currentState.psw.isBlank()) {
                _state.update { it.copy(passwordError = R.string.requiredFields_error) }
                hasError = true
            } else if (currentState.psw.length < 6) {
                _state.update { it.copy(passwordError = R.string.weakPassword) }
                hasError = true
            }
            if (currentState.confirmPsw.isBlank()) {
                _state.update { it.copy(confirmPasswordError = R.string.requiredFields_error) }
                hasError = true
            } else if (currentState.psw != currentState.confirmPsw) {
                _state.update { it.copy(confirmPasswordError = R.string.passwordNotMatch_error) }
                hasError = true
            }
            if (hasError) return // Interrompi se c'è un errore di validazione locale

            // Se tutto ok, procedi come prima
            _state.update { it.copy(isLoading = true) }

            viewModelScope.launch {
                try {
                    // Chiamata al repository con tutti i controlli lato server
                    val result = authRepository.signUp(
                        username = currentState.username,
                        firstName = currentState.firstName,
                        lastName = currentState.lastName,
                        email = currentState.email,
                        password = currentState.psw
                    )

                    // Aggiornamento stato con risultato
                    _state.update {
                        it.copy(
                            signUpResult = result,
                            isLoading = false
                        )
                    }

                    // Gestione messaggi di errore specifici dal backend/database
                    when (result) {
                        SignUpResult.Loading -> {}
                        SignUpResult.Success -> {}
                        SignUpResult.Error.UserAlreadyExists,
                        SignUpResult.Error.EmailAlreadyExists -> {
                            _state.update { it.copy(emailError = R.string.email_already_exists) }
                        }
                        SignUpResult.Error.UsernameAlreadyExists -> {
                            _state.update { it.copy(usernameError = R.string.username_already_exists) }
                        }
                        SignUpResult.Error.WeakPassword -> {
                            _state.update { it.copy(passwordError = R.string.weakPassword) }
                        }
                        SignUpResult.Error.InvalidEmail -> {
                            _state.update { it.copy(emailError = R.string.email_invalid_format) }
                        }
                        SignUpResult.Error.PasswordMismatch -> {
                            _state.update { it.copy(confirmPasswordError = R.string.passwordNotMatch_error) }
                        }
                        SignUpResult.Error.RequiredFields -> {
                            _state.update { it.copy(errorMessage = R.string.requiredFields_error) }
                        }
                        SignUpResult.Error.NetworkError -> {
                            _state.update { it.copy(errorMessage = R.string.network_error) }
                        }
                        is SignUpResult.Error.UnknownError -> {
                            _state.update { it.copy(errorMessage = R.string.generic_signup_error) }
                        }
                    }
                } catch (e: Exception) {
                    _state.update {
                        it.copy(
                            signUpResult = SignUpResult.Error.UnknownError(e),
                            isLoading = false,
                            errorMessage = R.string.generic_signup_error
                        )
                    }
                }
            }
        }

        override fun clearError() =
            _state.update {
                it.copy(
                    errorMessage = null,
                    usernameError = null,
                    firstNameError = null,
                    lastNameError = null,
                    emailError = null,
                    passwordError = null,
                    confirmPasswordError = null
                )
            }
    }
}
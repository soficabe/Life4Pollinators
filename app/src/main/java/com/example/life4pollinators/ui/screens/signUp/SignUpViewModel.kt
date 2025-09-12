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
 * Data class rappresentante lo stato completo della schermata di registrazione.
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
    val errorMessage: Int? = null
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

//ViewModel per la gestione della logica della schermata di registrazione.
class SignUpViewModel (
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SignUpState())
    val state = _state.asStateFlow()

    //Implementazione azioni di registrazione.
    val actions = object : SignUpActions {
        override fun setUsername(username: String) =
            _state.update { it.copy(username = username.trim()) }

        override fun setFirstName(firstName: String) =
            _state.update { it.copy(firstName = firstName.trim()) }

        override fun setLastName(lastName: String) =
            _state.update { it.copy(lastName = lastName.trim()) }

        override fun setEmail(email: String) =
            _state.update { it.copy(email = email.trim().lowercase()) }

        override fun setPsw(psw: String) =
            _state.update { it.copy(psw = psw) }

        override fun setConfirmPsw(confirmPsw: String) =
            _state.update { it.copy(confirmPsw = confirmPsw) }

        /**
         * Esegue la registrazione dell'utente.
         */
        override fun signUp() {
            val currentState = _state.value

            // Reset errore precedente
            _state.update { it.copy(errorMessage = null) }

            viewModelScope.launch {
                // Imposta loading
                _state.update { it.copy(isLoading = true) }

                try {
                    // Chiamata al repository con tutti i controlli
                    val result = authRepository.signUp(
                        username = currentState.username,
                        firstName = currentState.firstName,
                        lastName = currentState.lastName,
                        email = currentState.email,
                        password = currentState.psw,
                        confirmPassword = currentState.confirmPsw
                    )

                    // Aggiornamento stato con risultato
                    _state.update { it.copy(signUpResult = result, isLoading = false) }

                    // Gestione messaggi di errore specifici usando i tipi corretti
                    when (result) {
                        SignUpResult.Loading -> {
                            // Stato di caricamento
                        }

                        SignUpResult.Success -> {
                            // Successo - navigazione gestita dalla UI
                        }

                        SignUpResult.Error.UserAlreadyExists -> {
                            _state.update { it.copy(errorMessage = R.string.userExisting_error) }
                        }

                        SignUpResult.Error.UsernameAlreadyExists -> {
                            _state.update { it.copy(errorMessage = R.string.username_already_exists) }
                        }

                        SignUpResult.Error.EmailAlreadyExists -> {
                            _state.update { it.copy(errorMessage = R.string.email_already_exists) }
                        }

                        SignUpResult.Error.WeakPassword -> {
                            _state.update { it.copy(errorMessage = R.string.weakPassword) }
                        }

                        SignUpResult.Error.InvalidEmail -> {
                            _state.update { it.copy(errorMessage = R.string.email_invalid_format) }
                        }


                        SignUpResult.Error.PasswordMismatch -> {
                            _state.update { it.copy(errorMessage = R.string.passwordNotMatch_error) }
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
                    // Gestione errori imprevisti
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
            _state.update { it.copy(errorMessage = null) }
    }
}
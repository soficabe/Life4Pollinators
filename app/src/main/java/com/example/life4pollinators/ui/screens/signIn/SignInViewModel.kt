package com.example.life4pollinators.ui.screens.signIn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.life4pollinators.data.repositories.AuthRepository
import com.example.life4pollinators.data.repositories.SignInResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Data class rappresentante lo stato completo della schermata di SignIn.
 */
data class SignInState(
    val email: String = "",
    val psw: String = "",
    val signInResult: SignInResult? = null,
    val errorMessage: String? = null,
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
 * ViewModel per la gestione della logica della schermata di SignIn.
 */
class SignInViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()

    /**
     * Implementazione azioni di signIn.
     */
    val actions = object : SignInActions {
        override fun setEmail(email: String) {
            _state.update { it.copy(email = email, errorMessage = null) }
        }

        override fun setPsw(psw: String) {
            _state.update { it.copy(psw = psw, errorMessage = null) }
        }

        /**
         * Esegue il signIn dell'utente.
         */
        override fun signIn() {
            val currentState = _state.value

            // Reset errore precedente e imposta loading
            _state.update {
                it.copy(
                    errorMessage = null,
                    signInResult = SignInResult.Loading,
                    isLoading = true
                )
            }

            viewModelScope.launch {
                try {
                    // Chiamata al repository
                    val result = authRepository.signIn(
                        email = currentState.email,
                        password = currentState.psw
                    )

                    // Aggiornamento stato con risultato
                    _state.update {
                        it.copy(
                            signInResult = result,
                            isLoading = false
                        )
                    }

                    // Gestione messaggi di errore specifici
                    when (result) {
                        SignInResult.Loading -> {
                            // Già gestito sopra
                        }

                        SignInResult.Success -> {
                            // Successo - navigazione gestita dalla UI
                            _state.update { it.copy(errorMessage = null) }
                        }

                        is SignInResult.Error -> {
                            val errorMessage = when (result) {
                                SignInResult.Error.InvalidCredentials ->
                                    "Invalid email or password. Please check your credentials."

                                SignInResult.Error.RequiredFields ->
                                    "Email and password are required"

                                SignInResult.Error.InvalidEmail ->
                                    "Please enter a valid email address"

                                SignInResult.Error.NetworkError ->
                                    "Network error. Please check your connection."

                                is SignInResult.Error.UnknownError ->
                                    "Sign in failed. Please try again."
                            }

                            _state.update { it.copy(errorMessage = errorMessage) }
                        }
                    }
                } catch (e: Exception) {
                    // Gestione errori imprevisti
                    _state.update {
                        it.copy(
                            signInResult = SignInResult.Error.UnknownError(e),
                            errorMessage = "Sign in failed. Please try again.",
                            isLoading = false
                        )
                    }
                }
            }
        }

        override fun clearError() {
            _state.update {
                it.copy(
                    errorMessage = null,
                    signInResult = null
                )
            }
        }
    }
}
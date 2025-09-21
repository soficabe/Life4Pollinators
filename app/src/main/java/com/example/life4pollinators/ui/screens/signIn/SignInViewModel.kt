package com.example.life4pollinators.ui.screens.signIn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.life4pollinators.data.repositories.AuthRepository
import com.example.life4pollinators.data.repositories.SignInResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.life4pollinators.R

data class SignInState(
    val email: String = "",
    val psw: String = "",
    val signInResult: SignInResult? = null,
    val errorMessageRes: Int? = null,
    val isLoading: Boolean = false
)

interface SignInActions {
    fun setEmail(email: String)
    fun setPsw(psw: String)
    fun signIn()
    fun clearError()
}

class SignInViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()

    val actions = object : SignInActions {
        override fun setEmail(email: String) {
            _state.update { it.copy(email = email, errorMessageRes = null) }
        }

        override fun setPsw(psw: String) {
            _state.update { it.copy(psw = psw, errorMessageRes = null) }
        }

        override fun signIn() {
            val currentState = _state.value

            _state.update {
                it.copy(
                    errorMessageRes = null,
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
                    signInResult = null
                )
            }
        }
    }
}
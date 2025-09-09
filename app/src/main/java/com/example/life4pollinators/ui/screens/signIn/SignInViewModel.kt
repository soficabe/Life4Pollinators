package com.example.life4pollinators.ui.screens.signIn

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SignInState (
    val email: String = "",
    val psw: String = ""
)

interface SignInActions {
    fun setEmail(email: String)
    fun setPsw(psw: String)
    fun signIn()
    fun signInWithGoogle()
}

class SignInViewModel (
    //private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()

    val actions = object : SignInActions {
        override fun setEmail(email: String) =
            _state.update { it.copy(email = email) }

        override fun setPsw(psw: String) =
            _state.update { it.copy(psw = psw) }

        override fun signIn() {
            TODO("Not yet implemented")
        }

        override fun signInWithGoogle() {
            TODO("Not yet implemented")
        }
    }
}
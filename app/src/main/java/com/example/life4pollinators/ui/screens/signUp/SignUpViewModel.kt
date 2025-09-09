package com.example.life4pollinators.ui.screens.signUp

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SignUpState (
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val psw: String = "",
    val confirmPsw: String = ""
)

interface SignUpActions {
    fun setUsername(username: String)
    fun setFirstName(firstName: String)
    fun setLastName(lastName: String)
    fun setEmail(email: String)
    fun setPsw(psw: String)
    fun setConfirmPsw(confirmPsw: String)
    fun signUp()
}

class SignUpViewModel (
    //private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SignUpState())
    val state = _state.asStateFlow()

    val actions = object : SignUpActions {
        override fun setUsername(username: String) =
            _state.update { it.copy(username = username) }

        override fun setFirstName(firstName: String) =
            _state.update { it.copy(firstName = firstName) }

        override fun setLastName(lastName: String) =
            _state.update { it.copy(lastName = lastName) }

        override fun setEmail(email: String) =
            _state.update { it.copy(email = email) }

        override fun setPsw(psw: String) =
            _state.update { it.copy(psw = psw) }

        override fun setConfirmPsw(confirmPsw: String) =
            _state.update { it.copy(confirmPsw = confirmPsw) }

        override fun signUp() {
            TODO("Not yet implemented")
        }
    }
}
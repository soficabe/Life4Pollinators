package com.example.life4pollinators.ui.screens.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.life4pollinators.data.database.entities.User
import com.example.life4pollinators.data.repositories.AuthRepository
import com.example.life4pollinators.data.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileState(
    val user: User? = null,
    val isRefreshing: Boolean = false
)

interface ProfileActions {
    fun refreshProfile()
}

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    val actions = object : ProfileActions {

        override fun refreshProfile() {
            viewModelScope.launch {
                try {
                    _state.update {
                        it.copy(
                            isRefreshing = true
                        )
                    }

                    val authUser = authRepository.getAuthUser()
                    val user = userRepository.getUser(authUser.id)

                    _state.update {
                        it.copy(
                            user = user,
                            isRefreshing = false
                        )
                    }
                } catch (e: Exception) {
                    Log.e("ProfileViewModel", "Errore durante il refresh del profilo", e)

                    _state.update {
                        it.copy(
                            isRefreshing = false
                        )
                    }
                }
            }
        }
    }

    init {
        actions.refreshProfile()
    }
}

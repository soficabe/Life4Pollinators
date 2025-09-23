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

/**
 * Stato della schermata profilo.
 *
 * @property user Dati utente autenticato (null se non caricati)
 * @property isRefreshing True se il profilo è in refresh/caricamento
 */
data class ProfileState(
    val user: User? = null,
    val isRefreshing: Boolean = false
)

/**
 * Azioni disponibili nella schermata profilo.
 */
interface ProfileActions {
    fun refreshProfile()
}

/**
 * ViewModel per la schermata profilo.
 * Si occupa di caricare le informazioni dell'utente autenticato.
 */
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
        // Carica i dati all'avvio del ViewModel
        actions.refreshProfile()
    }
}
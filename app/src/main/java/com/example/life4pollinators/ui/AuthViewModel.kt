package com.example.life4pollinators.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.life4pollinators.data.repositories.AuthRepository
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel che espone lo stato di autenticazione dell'utente.
 * Si basa sul flusso sessionStatus fornito dall'AuthRepository.
 */
class AuthViewModel(
    authRepository: AuthRepository
) : ViewModel() {

    /**
     * Flusso che indica se l'utente è autenticato.
     * True se la sessione corrente è di tipo Authenticated, false altrimenti.
     */
    val isAuthenticated: StateFlow<Boolean> =
        authRepository.sessionStatus
            .map { it is SessionStatus.Authenticated }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Flusso per ottenere direttamente l'oggetto utente autenticato
    // val currentUser = authRepository.sessionStatus
    //     .map { status -> (status as? SessionStatus.Authenticated)?.session?.user }
    //     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
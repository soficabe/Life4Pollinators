package com.example.life4pollinators.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.life4pollinators.data.repositories.AuthRepository
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AuthViewModel(
    authRepository: AuthRepository
) : ViewModel() {

    val isAuthenticated: StateFlow<Boolean> =
        authRepository.sessionStatus
            .map { it is SessionStatus.Authenticated }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)


    //val currentUser = authRepository.sessionStatus
    //    .map { status -> (status as? SessionStatus.Authenticated)?.session?.user }
    //    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
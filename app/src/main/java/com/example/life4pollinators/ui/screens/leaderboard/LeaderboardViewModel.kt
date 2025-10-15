package com.example.life4pollinators.ui.screens.leaderboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.life4pollinators.data.repositories.AuthRepository
import com.example.life4pollinators.data.repositories.SightingsRepository
import com.example.life4pollinators.data.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Dati di un singolo utente nella leaderboard
 */
data class LeaderboardEntry(
    val userId: String,
    val username: String,
    val position: Int,
    val score: Int,
    val isCurrentUser: Boolean = false
)

/**
 * Stato della schermata leaderboard
 */
data class LeaderboardState(
    val entries: List<LeaderboardEntry> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUserId: String? = null
)

/**
 * Azioni disponibili nella schermata leaderboard
 */
interface LeaderboardActions {
    fun loadLeaderboard()
    fun refresh()
}

/**
 * ViewModel per la schermata leaderboard
 */
class LeaderboardViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val sightingsRepository: SightingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LeaderboardState())
    val state = _state.asStateFlow()

    val actions = object : LeaderboardActions {
        override fun loadLeaderboard() {
            viewModelScope.launch {
                try {
                    _state.update { it.copy(isLoading = true, error = null) }

                    // Ottieni l'ID dell'utente corrente
                    val authUser = authRepository.getAuthUser()
                    val currentUserId = authUser.id

                    // Recupera tutti gli utenti e tutti gli avvistamenti
                    val allUsers = userRepository.getAllUsers()

                    // Nessun utente nel database
                    if (allUsers.isEmpty()) {
                        _state.update {
                            it.copy(
                                entries = emptyList(),
                                currentUserId = currentUserId,
                                isLoading = false
                            )
                        }
                        return@launch
                    }

                    val allSightings = sightingsRepository.getAllSightings()

                    // Raggruppa gli avvistamenti per utente
                    val sightingsByUser = allSightings.groupBy { it.userId }

                    // Calcola i punteggi per TUTTI gli utenti
                    val userScores = allUsers.map { user ->
                        val userSightings = sightingsByUser[user.id] ?: emptyList()
                        val uniqueSpecies = userSightings
                            .map { it.targetId }
                            .toSet()
                            .size
                        val totalSightings = userSightings.size
                        val score = (uniqueSpecies * 10) + totalSightings

                        Triple(user.id, score, user.username)
                    }
                        // Ordina per: 1) punteggio decrescente, 2) username alfabetico (per stabilità)
                        .sortedWith(compareByDescending<Triple<String, Int, String>> { it.second }
                            .thenBy { it.third })

                    // Crea le entry della leaderboard gestendo i pari merito
                    val entries = mutableListOf<LeaderboardEntry>()
                    var currentPosition = 1

                    userScores.forEachIndexed { index, (userId, score, username) ->
                        // Se non è il primo e ha un punteggio DIVERSO dal precedente, incrementa la posizione
                        if (index > 0 && userScores[index - 1].second != score) {
                            currentPosition++
                        }

                        entries.add(
                            LeaderboardEntry(
                                userId = userId,
                                username = username,
                                position = currentPosition,
                                score = score,
                                isCurrentUser = userId == currentUserId
                            )
                        )
                    }

                    _state.update {
                        it.copy(
                            entries = entries,
                            currentUserId = currentUserId,
                            isLoading = false
                        )
                    }

                } catch (e: Exception) {
                    Log.e("LeaderboardViewModel", "Errore caricamento leaderboard", e)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = e.message
                        )
                    }
                }
            }
        }

        override fun refresh() {
            loadLeaderboard()
        }
    }

    init {
        actions.loadLeaderboard()
    }
}
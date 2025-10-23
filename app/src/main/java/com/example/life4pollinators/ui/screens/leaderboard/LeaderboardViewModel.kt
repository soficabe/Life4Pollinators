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

                    // Recupera TUTTI gli utenti registrati
                    val allUsers = userRepository.getAllUsers()

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

                    // Calcola i ranking degli utenti con sightings
                    val rankingsMap = sightingsRepository.calculateAllUserRankings()

                    // Determina la posizione per utenti senza sightings
                    val lastPosition = sightingsRepository.getPositionForUsersWithoutSightings()

                    // Crea le entry della leaderboard
                    val entries = mutableListOf<LeaderboardEntry>()

                    // Utenti CON sightings: raggruppa per posizione e ordina alfabeticamente
                    val usersWithSightings = allUsers.filter { rankingsMap.containsKey(it.id) }

                    val entriesByPosition = usersWithSightings
                        .map { user ->
                            val ranking = rankingsMap[user.id]!!
                            LeaderboardEntry(
                                userId = user.id,
                                username = user.username,
                                position = ranking.position,
                                score = ranking.userScore.score,
                                isCurrentUser = user.id == currentUserId
                            )
                        }
                        .groupBy { it.position }

                    // Aggiungi in ordine di posizione, ordinando alfabeticamente dentro ogni gruppo
                    entriesByPosition.keys.sorted().forEach { position ->
                        val usersAtPosition = entriesByPosition[position]!!.sortedBy { it.username.lowercase() }
                        entries.addAll(usersAtPosition)
                    }

                    // Utenti SENZA sightings: ordina alfabeticamente
                    val usersWithoutSightings = allUsers
                        .filter { !rankingsMap.containsKey(it.id) }
                        .sortedBy { it.username.lowercase() }

                    usersWithoutSightings.forEach { user ->
                        entries.add(
                            LeaderboardEntry(
                                userId = user.id,
                                username = user.username,
                                position = lastPosition,
                                score = 0,
                                isCurrentUser = user.id == currentUserId
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
package com.example.life4pollinators.ui.screens.leaderboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.life4pollinators.R
import com.example.life4pollinators.data.repositories.AuthRepository
import com.example.life4pollinators.data.repositories.SightingsRepository
import com.example.life4pollinators.data.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Rappresenta un singolo utente nella classifica globale.
 *
 * @property userId ID univoco dell'utente
 * @property username Username visualizzato nella leaderboard
 * @property position Posizione in classifica (1 = primo posto)
 * @property score Punteggio totale accumulato
 * @property isCurrentUser True se è l'utente autenticato corrente
 */
data class LeaderboardEntry(
    val userId: String,
    val username: String,
    val position: Int,
    val score: Int,
    val isCurrentUser: Boolean = false
)

/**
 * Stato della schermata leaderboard.
 *
 * @property entries Lista ordinata di utenti in classifica
 * @property isLoading True se sta caricando i dati
 * @property error ID risorsa stringa di errore (null se nessun errore)
 * @property currentUserId ID dell'utente autenticato corrente
 */
data class LeaderboardState(
    val entries: List<LeaderboardEntry> = emptyList(),
    val isLoading: Boolean = false,
    val error: Int? = null,
    val currentUserId: String? = null
)

/**
 * Azioni disponibili nella schermata leaderboard.
 */
interface LeaderboardActions {
    // Carica la classifica globale dal backend
    fun loadLeaderboard()
    // Ricarica la classifica
    fun refresh()
}

/**
 * ViewModel per la schermata leaderboard globale.
 *
 * Responsabilità:
 * - Caricare tutti gli utenti registrati
 * - Calcolare i ranking basati su punteggi avvistamenti
 * - Ordinare gli utenti per posizione e alfabeticamente
 * - Evidenziare l'utente corrente
 *
 * Logica di ordinamento:
 * 1. Utenti CON avvistamenti: ordinati per posizione, poi alfabeticamente dentro ogni posizione
 * 2. Utenti SENZA avvistamenti: tutti in ultima posizione, ordinati alfabeticamente
 *
 * @property authRepository Repository per ottenere l'utente autenticato
 * @property userRepository Repository per recupero lista utenti
 * @property sightingsRepository Repository per calcolo ranking e punteggi
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

                    // Calcola i ranking di TUTTI gli utenti con almeno un avvistamento
                    // Mappa: userId -> UserRanking (con posizione e punteggio)
                    val rankingsMap = sightingsRepository.calculateAllUserRankings()

                    // Determina la posizione per utenti senza avvistamenti (ultima posizione + 1)
                    val lastPosition = sightingsRepository.getPositionForUsersWithoutSightings()

                    // Crea le entry della leaderboard
                    val entries = mutableListOf<LeaderboardEntry>()

                    // === PARTE 1: Utenti CON avvistamenti ===
                    // Raggruppa per posizione (per gestire parimerito) e ordina alfabeticamente
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
                        .groupBy { it.position } // Raggruppa per posizione (gestisce parimerito)

                    // Aggiungi in ordine di posizione (1°, 2°, 3°, ...),
                    // ordinando alfabeticamente dentro ogni gruppo di parimerito
                    entriesByPosition.keys.sorted().forEach { position ->
                        val usersAtPosition = entriesByPosition[position]!!
                            .sortedBy { it.username.lowercase() } // Ordine alfabetico case-insensitive
                        entries.addAll(usersAtPosition)
                    }

                    // === PARTE 2: Utenti SENZA avvistamenti ===
                    // Tutti in ultima posizione, ordinati alfabeticamente
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
                            error = when {
                                e.message?.contains("network", ignoreCase = true) == true ||
                                        e.message?.contains("unable to resolve host", ignoreCase = true) == true ||
                                        e.message?.contains("failed to connect", ignoreCase = true) == true -> {
                                    R.string.network_error_connection
                                }
                                else -> R.string.leaderboard_error
                            }
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
        // Carica la leaderboard all'avvio
        actions.loadLeaderboard()
    }
}
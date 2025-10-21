package com.example.life4pollinators.ui.screens.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.life4pollinators.R
import com.example.life4pollinators.data.database.entities.User
import com.example.life4pollinators.data.repositories.AuthRepository
import com.example.life4pollinators.data.repositories.InsectsRepository
import com.example.life4pollinators.data.repositories.PlantsRepository
import com.example.life4pollinators.data.repositories.SightingsRepository
import com.example.life4pollinators.data.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Statistiche dell'utente
 */
data class UserStats(
    val totalSightings: Int = 0,
    val uniquePlants: Int = 0,
    val uniqueInsects: Int = 0,
    val globalRank: Int = -1,
    val totalScore: Int = 0,
    val totalPlants: Int = 0,
    val totalInsects: Int = 0
) {
    val plantsText: String
        get() = "$uniquePlants/$totalPlants"

    val insectsText: String
        get() = "$uniqueInsects/$totalInsects"
}

/**
 * Stato della schermata profilo.
 */
data class ProfileState(
    val user: User? = null,
    val stats: UserStats = UserStats(),
    val isRefreshing: Boolean = false,
    val isLoadingStats: Boolean = false,
    val errorLoadingProfile: Int? = null
)

/**
 * Azioni disponibili nella schermata profilo.
 */
interface ProfileActions {
    fun refreshProfile()
    fun refreshStats()
    fun clearError()
}

/**
 * ViewModel per la schermata profilo.
 */
class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val sightingsRepository: SightingsRepository,
    private val plantsRepository: PlantsRepository,
    private val insectsRepository: InsectsRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    val actions = object : ProfileActions {

        override fun refreshProfile() {
            viewModelScope.launch {
                try {
                    _state.update { it.copy(isRefreshing = true, errorLoadingProfile = null) }

                    val authUser = authRepository.getAuthUser()
                    val user = userRepository.getUser(authUser.id)

                    // Se user è null, è probabilmente un errore di rete
                    if (user == null) {
                        _state.update {
                            it.copy(
                                isRefreshing = false,
                                errorLoadingProfile = R.string.network_error_connection
                            )
                        }
                        return@launch
                    }

                    _state.update {
                        it.copy(
                            user = user,
                            isRefreshing = false,
                            errorLoadingProfile = null
                        )
                    }

                    refreshStats()
                } catch (e: Exception) {
                    Log.e("ProfileViewModel", "Errore durante il refresh del profilo", e)
                    _state.update {
                        it.copy(
                            isRefreshing = false,
                            errorLoadingProfile = when {
                                e.message?.contains("network", ignoreCase = true) == true ||
                                        e.message?.contains("unable to resolve host", ignoreCase = true) == true ||
                                        e.message?.contains("failed to connect", ignoreCase = true) == true -> {
                                    R.string.network_error_connection
                                }
                                else -> R.string.error_loading_profile
                            }
                        )
                    }
                }
            }
        }

        override fun refreshStats() {
            viewModelScope.launch {
                try {
                    _state.update { it.copy(isLoadingStats = true) }

                    val authUser = authRepository.getAuthUser()
                    val userId = authUser.id

                    val totalSightings = sightingsRepository.getUserTotalSightingsCount(userId)
                    val uniquePlants = sightingsRepository.getUserSightedSpecies(userId, "plant").size
                    val uniqueInsects = sightingsRepository.getUserSightedSpecies(userId, "insect").size
                    val totalPlants = plantsRepository.getTotalPlantsCount()
                    val totalInsects = insectsRepository.getTotalInsectsCount()

                    // Recupera ranking globale
                    val (globalRank, totalScore) = sightingsRepository.getGlobalRanking(userId)

                    val stats = UserStats(
                        totalSightings = totalSightings,
                        uniquePlants = uniquePlants,
                        uniqueInsects = uniqueInsects,
                        globalRank = globalRank,
                        totalScore = totalScore,
                        totalPlants = totalPlants,
                        totalInsects = totalInsects
                    )

                    _state.update {
                        it.copy(
                            stats = stats,
                            isLoadingStats = false
                        )
                    }

                } catch (e: Exception) {
                    Log.e("ProfileViewModel", "Errore durante il caricamento delle statistiche", e)
                    _state.update { it.copy(isLoadingStats = false) }
                }
            }
        }

        override fun clearError() {
            _state.update { it.copy(errorLoadingProfile = null) }
        }
    }

    init {
        // Carica i dati all'avvio del ViewModel
        actions.refreshProfile()
    }
}
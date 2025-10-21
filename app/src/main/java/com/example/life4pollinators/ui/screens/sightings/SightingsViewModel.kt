package com.example.life4pollinators.ui.screens.sightings

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.life4pollinators.R
import com.example.life4pollinators.data.database.entities.Insect
import com.example.life4pollinators.data.database.entities.InsectGroup
import com.example.life4pollinators.data.database.entities.Plant
import com.example.life4pollinators.data.repositories.AuthRepository
import com.example.life4pollinators.data.repositories.InsectsRepository
import com.example.life4pollinators.data.repositories.PlantsRepository
import com.example.life4pollinators.data.repositories.SightingsRepository
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

data class SpeciesItem(
    val id: String,
    val name: String,
    val imageUrl: String?,
    val isSighted: Boolean,
    val type: SpeciesType
)

enum class SpeciesType {
    PLANT, INSECT
}

enum class SpeciesFilter(@StringRes val displayNameRes: Int) {
    PLANTS(R.string.filter_plants),
    BEES(R.string.filter_bees),
    BUTTERFLIES(R.string.filter_butterflies),
    MOTHS(R.string.filter_moths),
    BEEFLIES(R.string.filter_beeflies),
    HOVERFLIES(R.string.filter_hoverflies),
    BEETLES(R.string.filter_beetles),
    WASPS(R.string.filter_wasps)
}

data class SightingsState(
    val isAuthenticated: Boolean = false,
    val userId: String? = null,
    val selectedFilter: SpeciesFilter = SpeciesFilter.PLANTS,
    val allPlants: List<Plant> = emptyList(),
    val allInsects: List<Insect> = emptyList(),
    val insectGroups: List<InsectGroup> = emptyList(),
    val sightedPlantIds: Set<String> = emptySet(),
    val sightedInsectIds: Set<String> = emptySet(),
    val filteredSpecies: List<SpeciesItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: Int? = null
)

interface SightingsActions {
    fun selectFilter(filter: SpeciesFilter)
    fun refresh()
}

class SightingsViewModel(
    private val authRepository: AuthRepository,
    private val sightingsRepository: SightingsRepository,
    private val plantsRepository: PlantsRepository,
    private val insectsRepository: InsectsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SightingsState())
    val state: StateFlow<SightingsState> = _state.asStateFlow()

    // Determina se la lingua del dispositivo è italiano
    private val isItalian: Boolean
        get() = Locale.getDefault().language == "it"

    // Mappa per trovare l'ID del gruppo dato il nome in inglese
    private val groupNameToIdMap: Map<String, String>
        get() = _state.value.insectGroups.associate { it.nameEn to it.id }

    val actions = object : SightingsActions {
        override fun selectFilter(filter: SpeciesFilter) {
            _state.update { it.copy(selectedFilter = filter) }
            updateFilteredSpecies()
        }

        override fun refresh() {
            loadAllSpecies()
            state.value.userId?.let { loadUserSightings(it) }
        }
    }

    init {
        observeAuth()
        loadAllSpecies()
    }

    private fun observeAuth() {
        viewModelScope.launch {
            authRepository.sessionStatus.collect { sessionStatus ->
                when (sessionStatus) {
                    is SessionStatus.Authenticated -> {
                        val userId = sessionStatus.session.user?.id
                        _state.update {
                            it.copy(
                                isAuthenticated = true,
                                userId = userId
                            )
                        }
                        userId?.let { loadUserSightings(it) }
                    }
                    else -> {
                        _state.update {
                            it.copy(
                                isAuthenticated = false,
                                userId = null,
                                sightedPlantIds = emptySet(),
                                sightedInsectIds = emptySet()
                            )
                        }
                        updateFilteredSpecies()
                    }
                }
            }
        }
    }

    private fun loadAllSpecies() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }  // Reset errore

            try {
                val plants = plantsRepository.getPlants()
                val insectGroups = insectsRepository.getInsectGroups()

                // Controlla se le liste sono vuote (probabile errore di rete)
                if (plants.isEmpty() && insectGroups.isEmpty()) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = R.string.network_error_connection
                        )
                    }
                    return@launch
                }

                val insects = insectGroups.flatMap { group ->
                    insectsRepository.getInsectsByGroup(group.id)
                }

                _state.update {
                    it.copy(
                        allPlants = plants,
                        allInsects = insects,
                        insectGroups = insectGroups,
                        isLoading = false,
                        error = null
                    )
                }

                updateFilteredSpecies()
            } catch (e: Exception) {
                Log.e("SightingsViewModel", "Error loading species", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = R.string.network_error_connection
                    )
                }
            }
        }
    }

    private fun loadUserSightings(userId: String) {
        viewModelScope.launch {
            try {
                Log.d("SightingsVM", "Loading sightings for user: $userId")

                val sightedPlants = sightingsRepository.getUserSightedSpecies(userId, "plant")
                val sightedInsects = sightingsRepository.getUserSightedSpecies(userId, "insect")

                Log.d("SightingsVM", "Sighted plants: $sightedPlants")
                Log.d("SightingsVM", "Sighted insects: $sightedInsects")

                _state.update {
                    it.copy(
                        sightedPlantIds = sightedPlants,
                        sightedInsectIds = sightedInsects
                    )
                }

                Log.d("SightingsVM", "State updated. Plant IDs: ${_state.value.sightedPlantIds}")
                Log.d("SightingsVM", "State updated. Insect IDs: ${_state.value.sightedInsectIds}")

                updateFilteredSpecies()
            } catch (e: Exception) {
                Log.e("SightingsViewModel", "Error loading user sightings", e)
            }
        }
    }

    private fun updateFilteredSpecies() {
        val currentState = _state.value

        val species = when (currentState.selectedFilter) {
            SpeciesFilter.PLANTS -> {
                currentState.allPlants.map { plant ->
                    SpeciesItem(
                        id = plant.id,
                        name = if (isItalian) plant.nameIt else plant.nameEn,
                        imageUrl = plant.imageUrl,
                        isSighted = currentState.sightedPlantIds.contains(plant.id),
                        type = SpeciesType.PLANT
                    )
                }
            }
            SpeciesFilter.BEES -> {
                filterInsectsByGroupName("Bees", currentState)
            }
            SpeciesFilter.BUTTERFLIES -> {
                filterInsectsByGroupName("Butterflies", currentState)
            }
            SpeciesFilter.MOTHS -> {
                filterInsectsByGroupName("Moths", currentState)
            }
            SpeciesFilter.BEEFLIES -> {
                filterInsectsByGroupName("Beeflies", currentState)
            }
            SpeciesFilter.HOVERFLIES -> {
                filterInsectsByGroupName("Hoverflies", currentState)
            }
            SpeciesFilter.BEETLES -> {
                filterInsectsByGroupName("Beetles", currentState)
            }
            SpeciesFilter.WASPS -> {
                filterInsectsByGroupName("Wasps", currentState)
            }
        }

        _state.update { it.copy(filteredSpecies = species) }
    }

    private fun filterInsectsByGroupName(groupName: String, state: SightingsState): List<SpeciesItem> {
        val groupId = groupNameToIdMap[groupName] ?: return emptyList()

        return state.allInsects
            .filter { it.group == groupId }
            .map { insect ->
                SpeciesItem(
                    id = insect.id,
                    name = insect.name,
                    imageUrl = insect.insectImage,
                    isSighted = state.sightedInsectIds.contains(insect.id),
                    type = SpeciesType.INSECT
                )
            }
    }
}
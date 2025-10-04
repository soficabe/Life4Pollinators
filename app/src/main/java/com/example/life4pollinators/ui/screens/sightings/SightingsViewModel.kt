package com.example.life4pollinators.ui.screens.sightings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.life4pollinators.data.database.entities.Insect
import com.example.life4pollinators.data.database.entities.Plant
import com.example.life4pollinators.data.repositories.AuthRepository
import com.example.life4pollinators.data.repositories.InsectsRepository
import com.example.life4pollinators.data.repositories.PlantsRepository
import com.example.life4pollinators.data.repositories.SightingsRepository
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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

enum class SpeciesFilter {
    PLANTS, BEES, BUTTERFLIES, MOTHS, BEEFLIES, HOVERFLIES
}

data class SightingsState(
    val isAuthenticated: Boolean = false,
    val userId: String? = null,
    val selectedFilter: SpeciesFilter = SpeciesFilter.PLANTS,
    val allPlants: List<Plant> = emptyList(),
    val allInsects: List<Insect> = emptyList(),
    val sightedPlantIds: Set<String> = emptySet(),
    val sightedInsectIds: Set<String> = emptySet(),
    val filteredSpecies: List<SpeciesItem> = emptyList(),
    val isLoading: Boolean = true
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

    // UUID dei gruppi di insetti dal database
    companion object {
        private const val BEES_GROUP_ID = "fc4fc047-c445-4d4b-8e1a-55e4358294ab"
        private const val BUTTERFLIES_GROUP_ID = "2ea44d50-041c-4e47-8ba2-d35d8e042df7"
        private const val MOTHS_GROUP_ID = "f890dd36-97b4-4a8c-81e9-12c26c2692aa"
        private const val BEEFLIES_GROUP_ID = "3fdf4b2c-aa6c-4d47-a1c5-2d91fefc0134"
        private const val HOVERFLIES_GROUP_ID = "6f698630-239c-42fd-8e44-f69789111356"
    }

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
            _state.update { it.copy(isLoading = true) }

            val plants = plantsRepository.getPlants()
            val insectGroups = insectsRepository.getInsectGroups()
            val insects = insectGroups.flatMap { group ->
                insectsRepository.getInsectsByGroup(group.id)
            }

            _state.update {
                it.copy(
                    allPlants = plants,
                    allInsects = insects,
                    isLoading = false
                )
            }

            updateFilteredSpecies()
        }
    }

    private fun loadUserSightings(userId: String) {
        viewModelScope.launch {
            val sightedPlants = sightingsRepository.getUserSightedSpecies(userId, "plant")
            val sightedInsects = sightingsRepository.getUserSightedSpecies(userId, "insect")

            _state.update {
                it.copy(
                    sightedPlantIds = sightedPlants,
                    sightedInsectIds = sightedInsects
                )
            }

            updateFilteredSpecies()
        }
    }

    private fun updateFilteredSpecies() {
        val currentState = _state.value

        val species = when (currentState.selectedFilter) {
            SpeciesFilter.PLANTS -> {
                currentState.allPlants.map { plant ->
                    SpeciesItem(
                        id = plant.id,
                        name = plant.nameEn,
                        imageUrl = plant.imageUrl,
                        isSighted = currentState.sightedPlantIds.contains(plant.id),
                        type = SpeciesType.PLANT
                    )
                }
            }
            SpeciesFilter.BEES -> {
                currentState.allInsects
                    .filter { it.group == BEES_GROUP_ID }
                    .map { insect ->
                        SpeciesItem(
                            id = insect.id,
                            name = insect.name,
                            imageUrl = insect.insectImage,
                            isSighted = currentState.sightedInsectIds.contains(insect.id),
                            type = SpeciesType.INSECT
                        )
                    }
            }
            SpeciesFilter.BUTTERFLIES -> {
                currentState.allInsects
                    .filter { it.group == BUTTERFLIES_GROUP_ID }
                    .map { insect ->
                        SpeciesItem(
                            id = insect.id,
                            name = insect.name,
                            imageUrl = insect.insectImage,
                            isSighted = currentState.sightedInsectIds.contains(insect.id),
                            type = SpeciesType.INSECT
                        )
                    }
            }
            SpeciesFilter.MOTHS -> {
                currentState.allInsects
                    .filter { it.group == MOTHS_GROUP_ID }
                    .map { insect ->
                        SpeciesItem(
                            id = insect.id,
                            name = insect.name,
                            imageUrl = insect.insectImage,
                            isSighted = currentState.sightedInsectIds.contains(insect.id),
                            type = SpeciesType.INSECT
                        )
                    }
            }
            SpeciesFilter.BEEFLIES -> {
                currentState.allInsects
                    .filter { it.group == BEEFLIES_GROUP_ID }
                    .map { insect ->
                        SpeciesItem(
                            id = insect.id,
                            name = insect.name,
                            imageUrl = insect.insectImage,
                            isSighted = currentState.sightedInsectIds.contains(insect.id),
                            type = SpeciesType.INSECT
                        )
                    }
            }
            SpeciesFilter.HOVERFLIES -> {
                currentState.allInsects
                    .filter { it.group == HOVERFLIES_GROUP_ID }
                    .map { insect ->
                        SpeciesItem(
                            id = insect.id,
                            name = insect.name,
                            imageUrl = insect.insectImage,
                            isSighted = currentState.sightedInsectIds.contains(insect.id),
                            type = SpeciesType.INSECT
                        )
                    }
            }
        }

        _state.update { it.copy(filteredSpecies = species) }
    }
}
package com.example.life4pollinators.ui.screens.plants

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.life4pollinators.R
import com.example.life4pollinators.data.database.entities.Plant
import com.example.life4pollinators.data.repositories.PlantsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlantDetailState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val plant: Plant? = null,
    val pollinatorGroups: List<String> = emptyList()
)

class PlantDetailViewModel(
    private val plantsRepository: PlantsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(PlantDetailState())
    val state = _state.asStateFlow()

    init {
        val plantId: String? = savedStateHandle["plantId"]
        if (plantId != null) {
            loadPlantDetail(plantId)
        }
    }

    private fun loadPlantDetail(plantId: String) {
        viewModelScope.launch {
            try {
                val plant = plantsRepository.getPlantById(plantId)
                val pollinatorGroups = plantsRepository.getPollinatorGroupsForPlant(plantId)
                _state.value = PlantDetailState(
                    isLoading = false,
                    plant = plant,
                    pollinatorGroups = pollinatorGroups
                )
            } catch (e: Exception) {
                _state.value = PlantDetailState(
                    isLoading = false,
                    error = R.string.loading_error.toString()
                )
            }
        }
    }
}
package com.example.life4pollinators.ui.screens.plants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.life4pollinators.R
import com.example.life4pollinators.data.database.entities.Plant
import com.example.life4pollinators.data.repositories.PlantsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlantsListState(
    val plants: List<Plant> = emptyList(),
    val isLoading: Boolean = false,
    val error: Int? = null
)

class PlantsListViewModel(
    private val repository: PlantsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PlantsListState())
    val state = _state.asStateFlow()

    init {
        loadPlants()
    }

    private fun loadPlants() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val plants = repository.getPlants()
                if (plants.isEmpty()) {
                    _state.value = PlantsListState(
                        plants = emptyList(),
                        isLoading = false,
                        error = R.string.network_error_connection
                    )
                } else {
                    _state.value = PlantsListState(
                        plants = plants,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.value = PlantsListState(
                    plants = emptyList(),
                    isLoading = false,
                    error = R.string.network_error_connection
                )
            }
        }
    }
}
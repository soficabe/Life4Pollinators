package com.example.life4pollinators.ui.screens.plants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.life4pollinators.data.database.entities.PlantsGeneralInfo
import com.example.life4pollinators.data.repositories.PlantsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlantsGeneralInfoState(
    val info: PlantsGeneralInfo? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class PlantsGeneralInfoViewModel(
    private val repository: PlantsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PlantsGeneralInfoState())
    val state: StateFlow<PlantsGeneralInfoState> = _state.asStateFlow()

    init {
        loadInfo()
    }

    private fun loadInfo() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val info = repository.getGeneralInfo()
                _state.value = PlantsGeneralInfoState(info = info, isLoading = false)
            } catch (e: Exception) {
                _state.value = PlantsGeneralInfoState(
                    info = null,
                    isLoading = false,
                    error = "Errore di caricamento"
                )
            }
        }
    }
}
package com.example.life4pollinators.ui.screens.insects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.life4pollinators.R
import com.example.life4pollinators.data.database.entities.InsectGroup
import com.example.life4pollinators.data.repositories.InsectsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Stato della schermata lista gruppi insetti.
 */
data class InsectGroupsListState(
    val insectGroups: List<InsectGroup> = emptyList(),
    val isLoading: Boolean = false,
    val error: Int? = null
)

/**
 * ViewModel per la schermata lista gruppi insetti.
 *
 * Carica automaticamente tutti i gruppi all'avvio.
 */
class InsectGroupsListViewModel(
    private val repository: InsectsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(InsectGroupsListState())
    val state = _state.asStateFlow()

    init {
        loadInsectGroups()
    }

    /**
     * Carica i gruppi di insetti dal repository.
     */
    private fun loadInsectGroups() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val insectGroups = repository.getInsectGroups()
                if (insectGroups.isEmpty()) {
                    _state.value = InsectGroupsListState(
                        insectGroups = emptyList(),
                        isLoading = false,
                        error = R.string.network_error_connection
                    )
                } else {
                    _state.value = InsectGroupsListState(
                        insectGroups = insectGroups,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.value = InsectGroupsListState(
                    insectGroups = emptyList(),
                    isLoading = false,
                    error = R.string.network_error_connection
                )
            }
        }
    }
}
package com.example.life4pollinators.ui.screens.insects

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.life4pollinators.R
import com.example.life4pollinators.data.database.entities.Insect
import com.example.life4pollinators.data.database.entities.InsectGroup
import com.example.life4pollinators.data.repositories.InsectsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InsectsListState(
    val insects: List<Insect> = emptyList(),
    val group: InsectGroup? = null,
    val isLoading: Boolean = false,
    val error: Int? = null
)

class InsectsListViewModel(
    private val repository: InsectsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(InsectsListState())
    val state = _state.asStateFlow()

    init {
        val groupId: String? = savedStateHandle["groupId"]
        if (groupId != null) {
            loadInsectsAndGroup(groupId)
        }
    }

    private fun loadInsectsAndGroup(groupId: String) {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val group = repository.getInsectGroupById(groupId)
                val insects = repository.getInsectsByGroup(groupId)

                if (group == null || insects.isEmpty()) {
                    _state.value = InsectsListState(
                        insects = emptyList(),
                        group = null,
                        isLoading = false,
                        error = R.string.network_error_connection
                    )
                } else {
                    _state.value = InsectsListState(
                        insects = insects,
                        group = group,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.value = InsectsListState(
                    insects = emptyList(),
                    group = null,
                    isLoading = false,
                    error = R.string.network_error_connection
                )
            }
        }
    }
}
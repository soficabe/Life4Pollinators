package com.example.life4pollinators.ui.screens.insects

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.life4pollinators.R
import com.example.life4pollinators.data.database.entities.InsectGroup
import com.example.life4pollinators.data.repositories.InsectsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InsectGroupInfoState(
    val group: InsectGroup? = null,
    val isLoading: Boolean = false,
    val error: Int? = null
)

class InsectGroupInfoViewModel(
    private val repository: InsectsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(InsectGroupInfoState())
    val state = _state.asStateFlow()

    init {
        val groupId: String? = savedStateHandle["groupId"]
        if (groupId != null) {
            loadGroup(groupId)
        }
    }

    private fun loadGroup(groupId: String) {
        _state.value = InsectGroupInfoState(isLoading = true)
        viewModelScope.launch {
            try {
                val group = repository.getInsectGroupById(groupId)
                if (group == null) {
                    _state.value = InsectGroupInfoState(
                        isLoading = false,
                        error = R.string.network_error_connection
                    )
                } else {
                    _state.value = InsectGroupInfoState(
                        group = group,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.value = InsectGroupInfoState(
                    isLoading = false,
                    error = R.string.network_error_connection
                )
            }
        }
    }
}
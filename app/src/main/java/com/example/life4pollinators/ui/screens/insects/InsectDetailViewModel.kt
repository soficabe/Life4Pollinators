package com.example.life4pollinators.ui.screens.insects

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.life4pollinators.R
import com.example.life4pollinators.data.database.entities.Insect
import com.example.life4pollinators.data.repositories.InsectsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InsectDetailState(
    val insect: Insect? = null,
    val isLoading: Boolean = false,
    val error: Int? = null
)

class InsectDetailViewModel(
    private val repository: InsectsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(InsectDetailState())
    val state = _state.asStateFlow()

    init {
        val insectId: String? = savedStateHandle["insectId"]
        if (insectId != null) {
            loadInsect(insectId)
        }
    }

    private fun loadInsect(insectId: String) {
        _state.value = InsectDetailState(isLoading = true)
        viewModelScope.launch {
            try {
                val insect = repository.getInsectById(insectId)
                if (insect == null) {
                    _state.value = InsectDetailState(
                        isLoading = false,
                        error = R.string.network_error_connection
                    )
                } else {
                    _state.value = InsectDetailState(
                        insect = insect,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.value = InsectDetailState(
                    isLoading = false,
                    error = R.string.network_error_connection
                )
            }
        }
    }
}
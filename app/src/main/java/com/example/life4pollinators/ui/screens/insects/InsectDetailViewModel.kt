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

/**
 * Stato della schermata dettaglio insetto.
 *
 * @property insect Entity dell'insetto selezionato
 * @property isLoading Indica se è in corso il caricamento dei dati
 * @property error ID risorsa stringa dell'errore (null se nessun errore)
 */
data class InsectDetailState(
    val insect: Insect? = null,
    val isLoading: Boolean = false,
    val error: Int? = null
)

/**
 * ViewModel per la schermata di dettaglio insetto.
 *
 * Recupera l'ID insetto dai navigation arguments e carica i dati.
 *
 * Utilizza SavedStateHandle per recuperare il parametro `insectId`
 * passato tramite navigation type-safe (L4PRoute.InsectDetail).
 *
 * @property insectsRepository Repository per accesso ai dati degli insetti
 * @param savedStateHandle Handle per recuperare parametri di navigazione
 */
class InsectDetailViewModel(
    private val insectsRepository: InsectsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(InsectDetailState())
    val state = _state.asStateFlow()

    init {
        // Recupera insectId dalla navigation
        val insectId: String? = savedStateHandle["insectId"]
        if (insectId != null) {
            loadInsect(insectId)
        }
    }

    /**
     * Carica i dati dell'insetto dal repository.
     *
     * @param insectId ID univoco dell'insetto da caricare
     */
    private fun loadInsect(insectId: String) {
        _state.value = InsectDetailState(isLoading = true)
        viewModelScope.launch {
            try {
                val insect = insectsRepository.getInsectById(insectId)
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
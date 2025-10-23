package com.example.life4pollinators.ui.screens.plants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.life4pollinators.R
import com.example.life4pollinators.data.database.entities.Plant
import com.example.life4pollinators.data.repositories.PlantsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Stato della schermata lista piante.
 *
 * Rappresenta lo stato UI per la visualizzazione della lista completa di piante.
 *
 * @property plants Lista di piante da visualizzare
 * @property isLoading Indica se è in corso il caricamento dei dati
 * @property error ID risorsa stringa dell'errore (null se nessun errore)
 */
data class PlantsListState(
    val plants: List<Plant> = emptyList(),
    val isLoading: Boolean = false,
    val error: Int? = null
)

/**
 * ViewModel per la schermata lista piante.
 *
 * La lista viene ordinata alfabeticamente in base alla lingua del dispositivo
 * (ordinamento gestito a livello di repository).
 *
 * @property repository Repository per accesso ai dati delle piante
 */
class PlantsListViewModel(
    private val repository: PlantsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PlantsListState())
    val state = _state.asStateFlow()

    init {
        // Caricamento automatico all'inizializzazione
        loadPlants()
    }

    /**
     * Carica la lista di tutte le piante dal repository.
     */
    private fun loadPlants() {
        // Reset stato e attivazione loading
        _state.value = _state.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val plants = repository.getPlants()

                // Lista vuota considerata come errore (database dovrebbe avere dati)
                if (plants.isEmpty()) {
                    _state.value = PlantsListState(
                        plants = emptyList(),
                        isLoading = false,
                        error = R.string.network_error_connection
                    )
                } else {
                    // Caricamento riuscito
                    _state.value = PlantsListState(
                        plants = plants,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                // Gestione eccezioni (rete, parsing, timeout, ecc.)
                _state.value = PlantsListState(
                    plants = emptyList(),
                    isLoading = false,
                    error = R.string.network_error_connection
                )
            }
        }
    }
}
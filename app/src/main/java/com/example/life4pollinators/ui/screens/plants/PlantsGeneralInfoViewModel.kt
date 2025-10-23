package com.example.life4pollinators.ui.screens.plants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.life4pollinators.R
import com.example.life4pollinators.data.database.entities.PlantsGeneralInfo
import com.example.life4pollinators.data.repositories.PlantsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Stato della schermata di informazioni generali sulle piante.
 *
 * Rappresenta lo stato UI per la visualizzazione delle informazioni generali (testi, immagini).
 *
 * @property info Entità contenente le informazioni generali (testo, immagini localizzate)
 * @property isLoading Indica se è in corso il caricamento dei dati
 * @property error ID risorsa stringa dell'errore da visualizzare (null se nessun errore)
 */
data class PlantsGeneralInfoState(
    val info: PlantsGeneralInfo? = null,
    val isLoading: Boolean = false,
    val error: Int? = null
)

/**
 * ViewModel per la schermata di informazioni generali sulle piante.
 *
 * Gestisce il caricamento delle informazioni generali dal repository,
 * esponendo lo stato. Il caricamento avviene automaticamente
 * all'inizializzazione del ViewModel.
 *
 * @property repository Repository per l'accesso ai dati delle piante
 */
class PlantsGeneralInfoViewModel(
    private val repository: PlantsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PlantsGeneralInfoState())
    val state: StateFlow<PlantsGeneralInfoState> = _state.asStateFlow()

    init {
        loadInfo()
    }

    /**
     * Carica le informazioni generali sulle piante dal repository.
     *
     * Aggiorna lo stato con i dati ricevuti o con un errore in caso di fallimento.
     * Gestisce sia il caso di dati null che eccezioni di rete.
     */
    private fun loadInfo() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val info = repository.getGeneralInfo()
                if (info == null) {
                    _state.value = PlantsGeneralInfoState(
                        info = null,
                        isLoading = false,
                        error = R.string.network_error_connection
                    )
                } else {
                    _state.value = PlantsGeneralInfoState(
                        info = info,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.value = PlantsGeneralInfoState(
                    info = null,
                    isLoading = false,
                    error = R.string.network_error_connection
                )
            }
        }
    }
}
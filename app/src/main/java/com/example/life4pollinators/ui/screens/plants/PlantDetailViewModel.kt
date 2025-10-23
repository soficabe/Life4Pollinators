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

/**
 * Stato della schermata dettaglio pianta.
 *
 * Rappresenta lo stato UI per la visualizzazione dei dettagli di una specifica pianta.
 *
 * @property isLoading Indica se è in corso il caricamento dei dati
 * @property error ID risorsa stringa dell'errore (null se nessun errore)
 * @property plant Entity della pianta selezionata (null durante loading o errore)
 * @property pollinatorGroups Lista dei nomi localizzati dei gruppi di impollinatori associati
 */
data class PlantDetailState(
    val isLoading: Boolean = true,
    val error: Int? = null,
    val plant: Plant? = null,
    val pollinatorGroups: List<String> = emptyList()
)

/**
 * ViewModel per la schermata dettaglio pianta.
 *
 * Il ViewModel utilizza SavedStateHandle per recuperare il parametro `plantId`
 * passato tramite navigation type-safe (L4PRoute.PlantDetail).
 *
 * Se l'ID è presente, avvia automaticamente il caricamento nel blocco init{}.
 *
 * @property plantsRepository Repository per accesso ai dati delle piante
 * @property savedStateHandle Handle per recuperare parametri di navigazione
 */
class PlantDetailViewModel(
    private val plantsRepository: PlantsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(PlantDetailState())
    val state = _state.asStateFlow()

    init {
        // Recupera l'ID pianta dai navigation arguments (type-safe)
        val plantId: String? = savedStateHandle["plantId"]

        // Avvia caricamento solo se l'ID è valido
        if (plantId != null) {
            loadPlantDetail(plantId)
        }
    }

    /**
     * Carica i dettagli completi di una pianta specifica.
     *
     * @param plantId ID univoco della pianta da caricare
     */
    private fun loadPlantDetail(plantId: String) {
        viewModelScope.launch {
            try {
                // Recupera dati pianta
                val plant = plantsRepository.getPlantById(plantId)

                // Gestione caso pianta non trovata
                if (plant == null) {
                    _state.value = PlantDetailState(
                        isLoading = false,
                        error = R.string.network_error_connection
                    )
                } else {
                    // Recupera gruppi impollinatori associati
                    val pollinatorGroups = plantsRepository.getPollinatorGroupsForPlant(plantId)

                    // Aggiornamento stato con successo
                    _state.value = PlantDetailState(
                        isLoading = false,
                        plant = plant,
                        pollinatorGroups = pollinatorGroups
                    )
                }
            } catch (e: Exception) {
                // Gestione eccezioni generiche (rete, parsing, ecc.)
                _state.value = PlantDetailState(
                    isLoading = false,
                    error = R.string.network_error_connection
                )
            }
        }
    }
}
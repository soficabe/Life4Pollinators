package com.example.life4pollinators.ui.screens.sightings

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.life4pollinators.R
import com.example.life4pollinators.data.database.entities.Insect
import com.example.life4pollinators.data.database.entities.InsectGroup
import com.example.life4pollinators.data.database.entities.Plant
import com.example.life4pollinators.data.repositories.InsectsRepository
import com.example.life4pollinators.data.repositories.PlantsRepository
import com.example.life4pollinators.data.repositories.SightingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Rappresenta una singola specie (pianta o insetto) da visualizzare nella UI.
 *
 * @property id Identificatore univoco della specie
 * @property name Nome localizzato della specie
 * @property imageUrl URL dell'immagine della specie (può essere null)
 * @property isSighted True se l'utente corrente ha già avvistato questa specie
 * @property type Tipo di specie (PLANT o INSECT)
 */
data class SpeciesItem(
    val id: String,
    val name: String,
    val imageUrl: String?,
    val isSighted: Boolean,
    val type: SpeciesType
)

/**
 * Enum che rappresenta il tipo di specie.
 */
enum class SpeciesType {
    PLANT,
    INSECT
}

/**
 * Enum che rappresenta i filtri disponibili per la visualizzazione delle specie.
 *
 * Ogni filtro ha associato:
 * - Un ID risorsa stringa per il nome visualizzato
 * - Il nome del gruppo di insetti (null per le piante)
 *
 * @property displayNameRes ID della risorsa stringa per il nome del filtro
 * @property groupName Nome del gruppo di insetti nel database (null per PLANTS)
 */
enum class SpeciesFilter(
    @StringRes val displayNameRes: Int,
    val groupName: String? = null
) {
    PLANTS(R.string.filter_plants, null),
    BEES(R.string.filter_bees, "Bees"),
    BUTTERFLIES(R.string.filter_butterflies, "Butterflies"),
    MOTHS(R.string.filter_moths, "Moths"),
    BEEFLIES(R.string.filter_beeflies, "Beeflies"),
    HOVERFLIES(R.string.filter_hoverflies, "Hoverflies"),
    BEETLES(R.string.filter_beetles, "Beetles"),
    WASPS(R.string.filter_wasps, "Wasps")
}

/**
 * Stato della schermata Sightings.
 *
 * @property selectedFilter Filtro di categoria attualmente selezionato
 * @property allPlants Lista completa di tutte le piante dal database
 * @property allInsects Lista completa di tutti gli insetti dal database
 * @property insectGroups Lista dei gruppi di insetti (per mappare filtri)
 * @property sightedPlantIds Set di ID delle piante avvistate dall'utente
 * @property sightedInsectIds Set di ID degli insetti avvistati dall'utente
 * @property filteredSpecies Lista delle specie filtrate da visualizzare
 * @property isLoading True se sta caricando dati dal database
 * @property error ID risorsa stringa dell'errore (null se nessun errore)
 */
data class SightingsState(
    val selectedFilter: SpeciesFilter = SpeciesFilter.PLANTS,
    val allPlants: List<Plant> = emptyList(),
    val allInsects: List<Insect> = emptyList(),
    val insectGroups: List<InsectGroup> = emptyList(),
    val sightedPlantIds: Set<String> = emptySet(),
    val sightedInsectIds: Set<String> = emptySet(),
    val filteredSpecies: List<SpeciesItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: Int? = null
)

/**
 * Interfaccia delle azioni disponibili nella schermata Sightings.
 */
interface SightingsActions {
    // Seleziona un filtro di categoria
    fun selectFilter(filter: SpeciesFilter)
    // Ricarica tutte le specie e gli avvistamenti dell'utente
    fun refresh()
    // Carica gli avvistamenti per un utente specifico
    fun loadUserSightings(userId: String)
}

/**
 * ViewModel per la schermata Sightings.
 *
 * Responsabilità:
 * - Caricare tutte le specie (piante e insetti) dal database
 * - Caricare gli avvistamenti dell'utente autenticato
 * - Gestire i filtri per categoria
 * - Applicare blur alle specie non avvistate
 *
 * Il ViewModel si occupa di:
 * 1. Caricare piante, insetti e gruppi dal database
 * 2. Caricare gli avvistamenti personali dell'utente (quando userId viene fornito)
 * 3. Filtrare le specie in base al filtro selezionato
 * 4. Mappare le specie con lo stato "avvistato/non avvistato"
 *
 * @property sightingsRepository Repository per recupero avvistamenti utente
 * @property plantsRepository Repository per recupero piante
 * @property insectsRepository Repository per recupero insetti
 */
class SightingsViewModel(
    private val sightingsRepository: SightingsRepository,
    private val plantsRepository: PlantsRepository,
    private val insectsRepository: InsectsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SightingsState())
    val state: StateFlow<SightingsState> = _state.asStateFlow()

    // Determina se la lingua del dispositivo è italiano (per localizzazione nomi)
    private val isItalian: Boolean
        get() = Locale.getDefault().language == "it"

    // Mappa per trovare l'ID del gruppo dato il nome in inglese
    private val groupNameToIdMap: Map<String, String>
        get() = _state.value.insectGroups.associate { it.nameEn to it.id }

    val actions = object : SightingsActions {
        override fun selectFilter(filter: SpeciesFilter) {
            _state.update { it.copy(selectedFilter = filter) }
            updateFilteredSpecies()
        }

        override fun refresh() {
            loadAllSpecies()
        }

        override fun loadUserSightings(userId: String) {
            loadUserSightingsInternal(userId)
        }
    }

    init {
        loadAllSpecies()
    }

    /**
     * Carica tutte le specie (piante e insetti) dal database.
     *
     * Processo:
     * 1. Carica tutte le piante
     * 2. Carica i gruppi di insetti
     * 3. Per ogni gruppo, carica gli insetti corrispondenti
     * 4. Aggiorna lo stato con i dati caricati
     *
     * Gestisce errori di rete mostrando un messaggio di errore se entrambe
     * le liste (piante e gruppi) sono vuote.
     */
    private fun loadAllSpecies() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val plants = plantsRepository.getPlants()
                val insectGroups = insectsRepository.getInsectGroups()

                // Controlla se le liste sono vuote (probabile errore di rete)
                if (plants.isEmpty() && insectGroups.isEmpty()) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = R.string.network_error_connection
                        )
                    }
                    return@launch
                }

                val insects = insectGroups.flatMap { group ->
                    insectsRepository.getInsectsByGroup(group.id)
                }

                _state.update {
                    it.copy(
                        allPlants = plants,
                        allInsects = insects,
                        insectGroups = insectGroups,
                        isLoading = false,
                        error = null
                    )
                }

                updateFilteredSpecies()
            } catch (e: Exception) {
                Log.e("SightingsViewModel", "Error loading species", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = R.string.network_error_connection
                    )
                }
            }
        }
    }

    /**
     * Carica gli avvistamenti personali dell'utente autenticato.
     *
     * Recupera:
     * - Set di ID delle piante avvistate
     * - Set di ID degli insetti avvistati
     *
     * Questi set vengono poi usati per determinare quali specie mostrare
     * sfocate (non avvistate) e quali nitide (avvistate).
     *
     * @param userId ID dell'utente di cui caricare gli avvistamenti
     */
    private fun loadUserSightingsInternal(userId: String) {
        viewModelScope.launch {
            try {
                Log.d("SightingsVM", "Loading sightings for user: $userId")

                val sightedPlants = sightingsRepository.getUserSightedSpecies(userId, "plant")
                val sightedInsects = sightingsRepository.getUserSightedSpecies(userId, "insect")

                Log.d("SightingsVM", "Sighted plants: $sightedPlants")
                Log.d("SightingsVM", "Sighted insects: $sightedInsects")

                _state.update {
                    it.copy(
                        sightedPlantIds = sightedPlants,
                        sightedInsectIds = sightedInsects
                    )
                }

                Log.d("SightingsVM", "State updated. Plant IDs: ${_state.value.sightedPlantIds}")
                Log.d("SightingsVM", "State updated. Insect IDs: ${_state.value.sightedInsectIds}")

                updateFilteredSpecies()
            } catch (e: Exception) {
                Log.e("SightingsViewModel", "Error loading user sightings", e)
            }
        }
    }

    /**
     * Aggiorna la lista delle specie filtrate in base al filtro selezionato.
     *
     * Comportamento:
     * - Se filtro PLANTS: mostra tutte le piante con nomi localizzati
     * - Se altri filtri: mostra insetti del gruppo corrispondente
     *
     * Per ogni specie, determina se è stata avvistata dall'utente confrontando
     * l'ID con i set di avvistamenti (sightedPlantIds / sightedInsectIds).
     */
    private fun updateFilteredSpecies() {
        val currentState = _state.value

        val species = when (currentState.selectedFilter) {
            SpeciesFilter.PLANTS -> {
                currentState.allPlants.map { plant ->
                    SpeciesItem(
                        id = plant.id,
                        name = if (isItalian) plant.nameIt else plant.nameEn,
                        imageUrl = plant.imageUrl,
                        isSighted = currentState.sightedPlantIds.contains(plant.id),
                        type = SpeciesType.PLANT
                    )
                }
            }
            else -> {
                // Tutti gli altri casi sono insetti
                currentState.selectedFilter.groupName?.let { groupName ->
                    filterInsectsByGroupName(groupName, currentState)
                } ?: emptyList()
            }
        }

        _state.update { it.copy(filteredSpecies = species) }
    }

    /**
     * Filtra gli insetti per nome del gruppo e crea la lista di SpeciesItem.
     *
     * Processo:
     * 1. Trova l'ID del gruppo dalla mappa groupNameToIdMap
     * 2. Filtra gli insetti che appartengono a quel gruppo
     * 3. Mappa ogni insetto a SpeciesItem con stato "avvistato"
     *
     * @param groupName Nome inglese del gruppo di insetti (es. "Bees", "Butterflies")
     * @param state Stato corrente contenente insetti e avvistamenti
     * @return Lista di SpeciesItem filtrati per il gruppo specificato
     */
    private fun filterInsectsByGroupName(groupName: String, state: SightingsState): List<SpeciesItem> {
        val groupId = groupNameToIdMap[groupName] ?: return emptyList()

        return state.allInsects
            .filter { it.group == groupId }
            .map { insect ->
                SpeciesItem(
                    id = insect.id,
                    name = insect.name,
                    imageUrl = insect.insectImage,
                    isSighted = state.sightedInsectIds.contains(insect.id),
                    type = SpeciesType.INSECT
                )
            }
    }
}
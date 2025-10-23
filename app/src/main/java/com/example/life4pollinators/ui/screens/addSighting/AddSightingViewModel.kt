package com.example.life4pollinators.ui.screens.addSighting

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.life4pollinators.R
import com.example.life4pollinators.data.repositories.InsectsRepository
import com.example.life4pollinators.data.repositories.PlantsRepository
import com.example.life4pollinators.data.repositories.ImageRepository
import com.example.life4pollinators.data.repositories.SightingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import java.util.Locale

/**
 * Stato della schermata di aggiunta avvistamento.
 *
 * Gestisce tutti i campi del form e gli stati di validazione.
 *
 * @property imageUri URI dell'immagine selezionata localmente
 * @property imageUrl URL dell'immagine dopo l'upload
 * @property date Data dell'avvistamento
 * @property time Ora dell'avvistamento
 * @property latitude Latitudine della posizione
 * @property longitude Longitudine della posizione
 * @property pollinatorQuery Testo di ricerca per impollinatori
 * @property plantQuery Testo di ricerca per piante
 * @property pollinatorSuggestions Lista suggerimenti impollinatori (id, nome)
 * @property plantSuggestions Lista suggerimenti piante (id, nome)
 * @property selectedPollinatorId ID dell'impollinatore selezionato
 * @property selectedPollinatorName Nome dell'impollinatore selezionato
 * @property selectedPlantId ID della pianta selezionata
 * @property selectedPlantName Nome della pianta selezionata
 * @property isPollinatorInvalid Flag errore validazione campo impollinatore
 * @property isPlantInvalid Flag errore validazione campo pianta
 * @property isImageInvalid Flag errore validazione immagine
 * @property isDateInvalid Flag errore validazione data
 * @property isTimeInvalid Flag errore validazione ora
 * @property isLocationInvalid Flag errore validazione posizione
 * @property isLoadingSuggestions Indica se sta caricando i suggerimenti
 * @property suggestionsError Errore durante caricamento suggerimenti
 * @property isLoading Indica se sta inviando l'avvistamento
 * @property isSuccess Indica se l'invio è completato con successo
 * @property errorMessage Messaggio di errore generico
 */
data class AddSightingState(
    val imageUri: Uri? = null,
    val imageUrl: String? = null,
    val date: LocalDate? = null,
    val time: LocalTime? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val pollinatorQuery: String = "",
    val plantQuery: String = "",
    val pollinatorSuggestions: List<Pair<String, String>> = emptyList(),
    val plantSuggestions: List<Pair<String, String>> = emptyList(),
    val selectedPollinatorId: String? = null,
    val selectedPollinatorName: String? = null,
    val selectedPlantId: String? = null,
    val selectedPlantName: String? = null,
    val isPollinatorInvalid: Boolean = false,
    val isPlantInvalid: Boolean = false,
    val isImageInvalid: Boolean = false,
    val isDateInvalid: Boolean = false,
    val isTimeInvalid: Boolean = false,
    val isLocationInvalid: Boolean = false,
    val isLoadingSuggestions: Boolean = false,
    val suggestionsError: Int? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Azioni disponibili nella schermata di aggiunta avvistamento.
 */
interface AddSightingActions {
    fun setImageUri(uri: Uri?)
    fun setDate(date: LocalDate)
    fun setTime(time: LocalTime)
    fun setLocation(latitude: Double, longitude: Double)
    fun onPollinatorQueryChange(query: String)
    fun onPlantQueryChange(query: String)
    fun selectPollinator(id: String, name: String)
    fun selectPlant(id: String, name: String)
    fun clearPollinator()
    fun clearPlant()
    fun submitSighting(context: Context, userId: String)
    fun reset()
}

/**
 * ViewModel per la schermata di aggiunta avvistamento.
 *
 * Responsabilità:
 * - Gestione form multi-campo con validazione
 * - Autocomplete con ricerca real-time per specie
 * - Upload immagine e salvataggio avvistamento
 * - Validazione lato client con feedback per campo
 *
 * L'utente può selezionare O un impollinatore O una pianta, non entrambi.
 *
 * @property sightingsRepository Repository per salvare gli avvistamenti
 * @property imageRepository Repository per upload immagini
 * @property insectsRepository Repository per ricerca impollinatori
 * @property plantsRepository Repository per ricerca piante
 */
class AddSightingViewModel(
    private val sightingsRepository: SightingsRepository,
    private val imageRepository: ImageRepository,
    private val insectsRepository: InsectsRepository,
    private val plantsRepository: PlantsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddSightingState())
    val state: StateFlow<AddSightingState> = _state.asStateFlow()

    // Helper per determinare la lingua corrente
    private val isItalian: Boolean
        get() = Locale.getDefault().language == "it"

    val actions = object : AddSightingActions {

        /**
         * Imposta l'URI dell'immagine selezionata.
         */
        override fun setImageUri(uri: Uri?) {
            _state.update { it.copy(imageUri = uri, isImageInvalid = false) }
        }

        /**
         * Imposta la data dell'avvistamento.
         */
        override fun setDate(date: LocalDate) {
            _state.update { it.copy(date = date, isDateInvalid = false) }
        }

        /**
         * Imposta l'ora dell'avvistamento.
         */
        override fun setTime(time: LocalTime) {
            _state.update { it.copy(time = time, isTimeInvalid = false) }
        }

        /**
         * Imposta le coordinate geografiche dell'avvistamento.
         */
        override fun setLocation(latitude: Double, longitude: Double) {
            _state.update {
                it.copy(
                    latitude = latitude,
                    longitude = longitude,
                    isLocationInvalid = false
                )
            }
        }

        /**
         * Gestisce il cambio di testo nel campo ricerca impollinatori.
         *
         * Comportamento:
         * - Se query vuota: svuota suggerimenti
         * - Se query presente: avvia ricerca
         * - Carica TUTTI gli insetti e filtra client-side
         */
        override fun onPollinatorQueryChange(query: String) {
            _state.update {
                it.copy(
                    pollinatorQuery = query,
                    isPollinatorInvalid = false,
                    suggestionsError = null
                )
            }

            // Se query vuota, resetta suggerimenti
            if (query.isBlank()) {
                _state.update {
                    it.copy(
                        pollinatorSuggestions = emptyList(),
                        isLoadingSuggestions = false
                    )
                }
                return
            }

            _state.update { it.copy(isLoadingSuggestions = true, suggestionsError = null) }

            viewModelScope.launch {
                try {
                    // Carica tutti i gruppi di insetti
                    val allInsects = insectsRepository.getInsectGroups()
                        .flatMap { group -> insectsRepository.getInsectsByGroup(group.id) }

                    if (allInsects.isEmpty()) {
                        _state.update {
                            it.copy(
                                pollinatorSuggestions = emptyList(),
                                isLoadingSuggestions = false,
                                suggestionsError = R.string.network_error_connection
                            )
                        }
                        return@launch
                    }

                    // Filtra per nome (case-insensitive) e limita a 10 risultati
                    val suggestions = allInsects
                        .filter { it.name.contains(query, ignoreCase = true) }
                        .map { it.id to it.name }
                        .take(10)

                    _state.update {
                        it.copy(
                            pollinatorSuggestions = suggestions,
                            isLoadingSuggestions = false,
                            suggestionsError = null
                        )
                    }
                } catch (e: Exception) {
                    Log.e("AddSightingViewModel", "Error loading pollinator suggestions", e)
                    _state.update {
                        it.copy(
                            pollinatorSuggestions = emptyList(),
                            isLoadingSuggestions = false,
                            suggestionsError = R.string.network_error_connection
                        )
                    }
                }
            }
        }

        /**
         * Gestisce il cambio di testo nel campo ricerca piante.
         *
         * Comportamento simile a onPollinatorQueryChange, ma:
         * - Cerca sia in nameIt che nameEn
         * - Mostra il nome localizzato nei suggerimenti
         */
        override fun onPlantQueryChange(query: String) {
            _state.update {
                it.copy(
                    plantQuery = query,
                    isPlantInvalid = false,
                    suggestionsError = null
                )
            }

            if (query.isBlank()) {
                _state.update {
                    it.copy(
                        plantSuggestions = emptyList(),
                        isLoadingSuggestions = false
                    )
                }
                return
            }

            _state.update { it.copy(isLoadingSuggestions = true, suggestionsError = null) }

            viewModelScope.launch {
                try {
                    val allPlants = plantsRepository.getPlants()

                    if (allPlants.isEmpty()) {
                        _state.update {
                            it.copy(
                                plantSuggestions = emptyList(),
                                isLoadingSuggestions = false,
                                suggestionsError = R.string.network_error_connection
                            )
                        }
                        return@launch
                    }

                    // Filtra sia nome inglese che italiano
                    val suggestions = allPlants
                        .filter {
                            it.nameEn.contains(query, ignoreCase = true) ||
                                    it.nameIt.contains(query, ignoreCase = true)
                        }
                        .map { plant ->
                            // Mostra nome localizzato
                            val displayName = if (isItalian) plant.nameIt else plant.nameEn
                            plant.id to displayName
                        }
                        .take(10)

                    _state.update {
                        it.copy(
                            plantSuggestions = suggestions,
                            isLoadingSuggestions = false,
                            suggestionsError = null
                        )
                    }
                } catch (e: Exception) {
                    Log.e("AddSightingViewModel", "Error loading plant suggestions", e)
                    _state.update {
                        it.copy(
                            plantSuggestions = emptyList(),
                            isLoadingSuggestions = false,
                            suggestionsError = R.string.network_error_connection
                        )
                    }
                }
            }
        }

        /**
         * Seleziona un impollinatore dai suggerimenti.
         *
         * Chiude il dropdown e imposta il campo come readonly.
         */
        override fun selectPollinator(id: String, name: String) {
            _state.update {
                it.copy(
                    selectedPollinatorId = id,
                    selectedPollinatorName = name,
                    pollinatorQuery = name,
                    pollinatorSuggestions = emptyList(),
                    isPollinatorInvalid = false,
                    suggestionsError = null
                )
            }
        }

        /**
         * Seleziona una pianta dai suggerimenti.
         */
        override fun selectPlant(id: String, name: String) {
            _state.update {
                it.copy(
                    selectedPlantId = id,
                    selectedPlantName = name,
                    plantQuery = name,
                    plantSuggestions = emptyList(),
                    isPlantInvalid = false,
                    suggestionsError = null
                )
            }
        }

        /**
         * Cancella la selezione dell'impollinatore.
         *
         * Riattiva il campo per una nuova ricerca.
         */
        override fun clearPollinator() {
            _state.update {
                it.copy(
                    selectedPollinatorId = null,
                    selectedPollinatorName = null,
                    pollinatorQuery = "",
                    pollinatorSuggestions = emptyList(),
                    isPollinatorInvalid = false,
                    suggestionsError = null
                )
            }
        }

        /**
         * Cancella la selezione della pianta.
         */
        override fun clearPlant() {
            _state.update {
                it.copy(
                    selectedPlantId = null,
                    selectedPlantName = null,
                    plantQuery = "",
                    plantSuggestions = emptyList(),
                    isPlantInvalid = false,
                    suggestionsError = null
                )
            }
        }

        /**
         * Invia l'avvistamento al server.
         *
         * Processo:
         * 1. Validazione completa di tutti i campi obbligatori
         * 2. Upload dell'immagine su Supabase Storage
         * 3. Salvataggio avvistamento nel database
         *
         * Validazione:
         * - Immagine, data, ora, posizione: obbligatori
         * - Specie: solo una tra impollinatore e pianta
         *
         * In caso di errore, evidenzia i campi invalidi con bordo rosso.
         *
         * @param context Context Android per stringhe localizzate
         * @param userId ID dell'utente che sta creando l'avvistamento
         */
        override fun submitSighting(context: Context, userId: String) {
            val s = _state.value

            // Reset tutti gli errori precedenti
            _state.update {
                it.copy(
                    isImageInvalid = false,
                    isDateInvalid = false,
                    isTimeInvalid = false,
                    isLocationInvalid = false,
                    isPollinatorInvalid = false,
                    isPlantInvalid = false,
                    errorMessage = null
                )
            }

            // Validazione campi obbligatori
            var hasError = false

            if (s.imageUri == null) {
                _state.update { it.copy(isImageInvalid = true) }
                hasError = true
            }
            if (s.date == null) {
                _state.update { it.copy(isDateInvalid = true) }
                hasError = true
            }
            if (s.time == null) {
                _state.update { it.copy(isTimeInvalid = true) }
                hasError = true
            }
            if (s.latitude == null || s.longitude == null) {
                _state.update { it.copy(isLocationInvalid = true) }
                hasError = true
            }

            // Determina target: impollinatore O pianta
            val targetId: String?
            val targetType: String?

            when {
                s.selectedPollinatorId != null -> {
                    targetId = s.selectedPollinatorId
                    targetType = "insect"
                }
                s.selectedPlantId != null -> {
                    targetId = s.selectedPlantId
                    targetType = "plant"
                }
                else -> {
                    // Nessuna specie selezionata: errore su entrambi i campi
                    _state.update {
                        it.copy(
                            isPollinatorInvalid = true,
                            isPlantInvalid = true
                        )
                    }
                    hasError = true
                    targetId = null
                    targetType = null
                }
            }

            // Se ci sono errori, mostra messaggio e interrompi
            if (hasError) {
                _state.update {
                    it.copy(
                        errorMessage = context.getString(R.string.add_sighting_error_missing_fields)
                    )
                }
                return
            }

            // Avvia processo di salvataggio
            viewModelScope.launch {
                _state.update { it.copy(isLoading = true, errorMessage = null) }

                try {
                    // Step 1: Upload immagine
                    val imageUrl = imageRepository.uploadSightingImage(
                        userId,
                        s.imageUri!!,
                        context
                    )

                    if (imageUrl == null) {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = context.getString(R.string.network_error_connection)
                            )
                        }
                        return@launch
                    }

                    // Step 2: Salva avvistamento nel database
                    val success = sightingsRepository.addSighting(
                        userId = userId,
                        imageUrl = imageUrl,
                        targetId = targetId!!,
                        targetType = targetType!!,
                        date = s.date!!,
                        time = s.time!!,
                        latitude = s.latitude!!,
                        longitude = s.longitude!!
                    )

                    if (success) {
                        _state.update { it.copy(isLoading = false, isSuccess = true) }
                    } else {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = context.getString(R.string.network_error_connection)
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AddSightingViewModel", "Error submitting sighting", e)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = context.getString(R.string.network_error_connection)
                        )
                    }
                }
            }
        }

        /**
         * Resetta completamente lo stato del form.
         *
         * Usato dopo invio con successo o per cancellare tutto.
         */
        override fun reset() {
            _state.value = AddSightingState()
        }
    }
}
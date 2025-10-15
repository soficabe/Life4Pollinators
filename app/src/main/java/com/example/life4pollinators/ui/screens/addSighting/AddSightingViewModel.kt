package com.example.life4pollinators.ui.screens.addSighting

import android.content.Context
import android.net.Uri
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
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

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

class AddSightingViewModel(
    private val sightingsRepository: SightingsRepository,
    private val imageRepository: ImageRepository,
    private val insectsRepository: InsectsRepository,
    private val plantsRepository: PlantsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddSightingState())
    val state: StateFlow<AddSightingState> = _state.asStateFlow()

    private val isItalian: Boolean
        get() = Locale.getDefault().language == "it"

    val actions = object : AddSightingActions {
        override fun setImageUri(uri: Uri?) {
            _state.update { it.copy(imageUri = uri, isImageInvalid = false) }
        }

        override fun setDate(date: LocalDate) {
            _state.update { it.copy(date = date, isDateInvalid = false) }
        }

        override fun setTime(time: LocalTime) {
            _state.update { it.copy(time = time, isTimeInvalid = false) }
        }

        override fun setLocation(latitude: Double, longitude: Double) {
            _state.update { it.copy(latitude = latitude, longitude = longitude, isLocationInvalid = false) }
        }

        override fun onPollinatorQueryChange(query: String) {
            _state.update {
                it.copy(
                    pollinatorQuery = query,
                    isPollinatorInvalid = false
                )
            }
            if (query.isBlank()) {
                _state.update { it.copy(pollinatorSuggestions = emptyList()) }
                return
            }
            viewModelScope.launch {
                val allInsects = insectsRepository.getInsectGroups()
                    .flatMap { group -> insectsRepository.getInsectsByGroup(group.id) }
                val suggestions = allInsects
                    .filter { it.name.contains(query, ignoreCase = true) }
                    .map { it.id to it.name }
                    .take(10)
                _state.update { it.copy(pollinatorSuggestions = suggestions) }
            }
        }

        override fun onPlantQueryChange(query: String) {
            _state.update {
                it.copy(
                    plantQuery = query,
                    isPlantInvalid = false
                )
            }
            if (query.isBlank()) {
                _state.update { it.copy(plantSuggestions = emptyList()) }
                return
            }
            viewModelScope.launch {
                val allPlants = plantsRepository.getPlants()
                val suggestions = allPlants
                    .filter {
                        it.nameEn.contains(query, ignoreCase = true) ||
                                it.nameIt.contains(query, ignoreCase = true)
                    }
                    .map { plant ->
                        val displayName = if (isItalian) plant.nameIt else plant.nameEn
                        plant.id to displayName
                    }
                    .take(10)
                _state.update { it.copy(plantSuggestions = suggestions) }
            }
        }

        override fun selectPollinator(id: String, name: String) {
            _state.update {
                it.copy(
                    selectedPollinatorId = id,
                    selectedPollinatorName = name,
                    pollinatorQuery = name,
                    pollinatorSuggestions = emptyList(),
                    isPollinatorInvalid = false
                )
            }
        }

        override fun selectPlant(id: String, name: String) {
            _state.update {
                it.copy(
                    selectedPlantId = id,
                    selectedPlantName = name,
                    plantQuery = name,
                    plantSuggestions = emptyList(),
                    isPlantInvalid = false
                )
            }
        }

        override fun clearPollinator() {
            _state.update {
                it.copy(
                    selectedPollinatorId = null,
                    selectedPollinatorName = null,
                    pollinatorQuery = "",
                    pollinatorSuggestions = emptyList(),
                    isPollinatorInvalid = false
                )
            }
        }

        override fun clearPlant() {
            _state.update {
                it.copy(
                    selectedPlantId = null,
                    selectedPlantName = null,
                    plantQuery = "",
                    plantSuggestions = emptyList(),
                    isPlantInvalid = false
                )
            }
        }

        override fun submitSighting(context: Context, userId: String) {
            val s = _state.value

            // Reset tutti gli errori
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

            // Validazione campi obbligatori con evidenziazione errori
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

            // Controlla selezione specie
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
                    // Mostra errore su entrambi i campi se nessuno è selezionato
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

            if (hasError) {
                _state.update {
                    it.copy(errorMessage = context.getString(R.string.add_sighting_error_missing_fields))
                }
                return
            }

            viewModelScope.launch {
                _state.update { it.copy(isLoading = true, errorMessage = null) }

                val imageUrl = imageRepository.uploadSightingImage(userId, s.imageUri!!, context)
                if (imageUrl == null) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = context.getString(R.string.add_sighting_error_image_upload)
                        )
                    }
                    return@launch
                }

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
                            errorMessage = context.getString(R.string.add_sighting_error_db)
                        )
                    }
                }
            }
        }

        override fun reset() {
            _state.value = AddSightingState()
        }
    }
}
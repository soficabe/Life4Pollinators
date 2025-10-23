package com.example.life4pollinators.ui.screens.quiz

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.life4pollinators.R
import com.example.life4pollinators.data.database.entities.*
import com.example.life4pollinators.data.repositories.QuizRepository
import com.example.life4pollinators.data.repositories.InsectsRepository
import com.example.life4pollinators.data.repositories.SightingsRepository
import com.example.life4pollinators.data.repositories.ImageRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.component.KoinComponent

/**
 * Enum che rappresenta i diversi step del flusso quiz.
 *
 * Flussi possibili:
 *
 * PIANTE:
 * Start → Question → ... → TargetSelection? → Result
 *
 * INSETTI CON QUIZ (bee, butterfly, moth, wasp):
 * Start → InsectTypeSelection → Question → ... → TargetSelection? → Result
 *
 * INSETTI SENZA QUIZ (beetle, beefly, hoverfly):
 * Start → InsectTypeSelection → InsectsList → Result
 */
sealed class QuizStep {
    data object Start : QuizStep()
    data object InsectTypeSelection : QuizStep()
    data object InsectsList : QuizStep()
    data object Question : QuizStep()
    data object TargetSelection : QuizStep()
    data object Result : QuizStep()
}

/**
 * Data class che combina QuizAnswerTarget con i dettagli della specie.
 *
 * Necessaria perché QuizAnswerTarget contiene solo ID e tipo,
 * ma per mostrare i risultati servono nome e immagine.
 *
 * @property target Relazione originale answer-target dal database
 * @property name Nome della specie (per insetti, che hanno solo 1 nome)
 * @property nameEn Nome inglese (per piante)
 * @property nameIt Nome italiano (per piante)
 * @property imageUrl URL immagine per preview del risultato
 */
data class TargetWithDetails(
    val target: QuizAnswerTarget,
    val name: String? = "",
    val nameEn: String? = "",
    val nameIt: String? = "",
    val imageUrl: String? = null
)

/**
 * Stato del quiz con tutti i dati necessari per le varie schermate.
 *
 * @property quizType Tipo del quiz corrente ("plant", "bee", "butterfly", ecc.)
 * @property originalQuizType Tipo originale prima di eventuali modifiche (per reset)
 * @property step Step corrente nel flusso del quiz
 * @property photoUrl URL/Uri della foto caricata dall'utente
 * @property currentQuestion Domanda correntemente visualizzata
 * @property answers Lista di risposte disponibili per la domanda corrente
 * @property selectedAnswer Ultima risposta selezionata
 * @property possibleTargets Lista di target suggeriti (per selezione multipla)
 * @property selectedTarget Target finale selezionato dall'utente
 * @property insectGroups Lista gruppi insetti (per schermata selezione tipo)
 * @property selectedInsectType Nome gruppo insetto selezionato (es. "Bees")
 * @property selectedGroupId ID gruppo insetto selezionato
 * @property insectsForSelection Lista insetti per gruppi senza quiz
 * @property loading Indica caricamento in corso
 * @property error ID risorsa stringa di errore
 * @property isUploading Indica upload avvistamento in corso
 * @property uploadSuccess true se upload riuscito, false se fallito, null se non tentato
 * @property uploadError ID risorsa stringa di errore upload specifico
 */
data class QuizState(
    val quizType: String = "",
    val originalQuizType: String = "",
    val step: QuizStep = QuizStep.Start,
    val photoUrl: String? = null,
    val currentQuestion: QuizQuestion? = null,
    val answers: List<QuizAnswer> = emptyList(),
    val selectedAnswer: QuizAnswer? = null,
    val possibleTargets: List<TargetWithDetails> = emptyList(),
    val selectedTarget: TargetWithDetails? = null,
    val insectGroups: List<InsectGroup> = emptyList(),
    val selectedInsectType: String? = null,
    val selectedGroupId: String? = null,
    val insectsForSelection: List<Insect> = emptyList(),
    val loading: Boolean = false,
    val error: Int? = null,
    val isUploading: Boolean = false,
    val uploadSuccess: Boolean? = null,
    val uploadError: Int? = null
)

/**
 * Azioni disponibili durante il flusso del quiz.
 */
interface QuizActions {
    fun setQuizType(type: String)
    fun startQuiz(photoUrl: String?)
    fun loadInsectGroups(photoUrl: String?)
    fun selectInsectType(groupName: String, groupId: String)
    fun selectInsectFromList(insect: Insect)
    fun answerQuestion(answer: QuizAnswer)
    fun selectTarget(target: TargetWithDetails)
    fun resetQuiz()
    fun resetQuizKeepingPhoto()
    fun submitQuizSighting(context: Context, userId: String)
}

/**
 * ViewModel per la gestione completa del flusso quiz.
 *
 * Responsabilità:
 * - Navigazione nell'albero decisionale
 * - Gestione dei diversi tipi di quiz (plant vs insect)
 * - Caricamento progressivo domande/risposte
 * - Gestione risultati singoli vs multipli
 * - Upload avvistamento da risultato quiz
 * - Mantenimento stato durante configurazione changes
 *
 * Il ViewModel è condiviso tra TUTTE le schermate del quiz
 * (definito nel NavGraph e passato ad ogni screen).
 *
 * @property quizRepository Repository per navigazione quiz
 * @property insectsRepository Repository per gruppi e liste insetti
 * @property sightingsRepository Repository per upload avvistamenti
 * @property imageRepository Repository per upload immagini
 */
class QuizViewModel(
    private val quizRepository: QuizRepository,
    private val insectsRepository: InsectsRepository,
    private val sightingsRepository: SightingsRepository,
    private val imageRepository: ImageRepository
) : ViewModel(), KoinComponent {

    private val _state = MutableStateFlow(QuizState())
    val state = _state.asStateFlow()

    val actions = object : QuizActions {

        /**
         * Imposta il tipo di quiz all'avvio.
         *
         * Chiamato dalla navigation quando si accede a "quizStart/{type}".
         *
         * @param type "plant" o "insect"
         */
        override fun setQuizType(type: String) {
            _state.update { it.copy(quizType = type, originalQuizType = type) }
        }

        /**
         * Avvia il quiz dopo che l'utente ha caricato una foto.
         *
         * Comportamento diverso in base al tipo:
         * - "plant": Carica direttamente il quiz piante
         * - "insect": Mostra schermata selezione tipo insetto
         *
         * Processo per piante:
         * 1. Recupera Quiz entity per tipo "plant"
         * 2. Recupera root question dal rootQuestionId
         * 3. Recupera le risposte per la root question
         * 4. Naviga a QuizStep.Question
         *
         * @param photoUrl URI o URL della foto caricata
         */
        override fun startQuiz(photoUrl: String?) {
            val type = _state.value.quizType

            // Se insetti, vai direttamente alla selezione tipo
            if (type == "insect") {
                loadInsectGroups(photoUrl)
                return
            }

            viewModelScope.launch {
                _state.update {
                    it.copy(
                        step = QuizStep.Start,
                        photoUrl = photoUrl,
                        loading = true,
                        error = null
                    )
                }

                try {
                    // Step 1: Recupera Quiz entity
                    val quiz = quizRepository.getQuiz(type)
                    if (quiz == null) {
                        _state.update {
                            it.copy(error = R.string.network_error_connection, loading = false)
                        }
                        return@launch
                    }

                    // Step 2: Recupera root question
                    val rootId = quiz.rootQuestionId
                    val rootQuestion = quizRepository.getRootQuestion(rootId)

                    if (rootQuestion != null) {
                        // Step 3: Recupera risposte per root question
                        val answers = quizRepository.getAnswers(rootQuestion.id)

                        if (answers.isEmpty()) {
                            _state.update {
                                it.copy(error = R.string.network_error_connection, loading = false)
                            }
                            return@launch
                        }

                        // Step 4: Aggiorna stato e naviga
                        _state.update {
                            it.copy(
                                step = QuizStep.Question,
                                currentQuestion = rootQuestion,
                                answers = answers,
                                loading = false,
                                error = null
                            )
                        }
                    } else {
                        _state.update {
                            it.copy(error = R.string.network_error_connection, loading = false)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("QuizViewModel", "Error starting quiz", e)
                    _state.update {
                        it.copy(error = R.string.network_error_connection, loading = false)
                    }
                }
            }
        }

        /**
         * Carica i gruppi di insetti per la schermata di selezione tipo.
         *
         * Recupera tutti i gruppi disponibili:
         * - Bees, Butterflies, Moths, Wasps (con quiz)
         * - Beetles, Bee flies, Hoverflies (senza quiz, lista diretta)
         *
         * @param photoUrl URI/URL della foto caricata
         */
        override fun loadInsectGroups(photoUrl: String?) {
            viewModelScope.launch {
                _state.update { it.copy(loading = true, photoUrl = photoUrl, error = null) }

                try {
                    val groups = insectsRepository.getInsectGroups()

                    if (groups.isEmpty()) {
                        _state.update {
                            it.copy(error = R.string.quiz_error_load_groups, loading = false)
                        }
                        return@launch
                    }

                    _state.update {
                        it.copy(
                            insectGroups = groups,
                            step = QuizStep.InsectTypeSelection,
                            loading = false,
                            error = null
                        )
                    }
                } catch (e: Exception) {
                    Log.e("QuizViewModel", "Error loading insect groups", e)
                    _state.update {
                        it.copy(
                            error = R.string.quiz_error_load_groups,
                            loading = false
                        )
                    }
                }
            }
        }

        /**
         * Gestisce la selezione del tipo di insetto.
         *
         * Comportamento in base al gruppo selezionato:
         *
         * GRUPPI CON QUIZ (Bees, Butterflies, Moths, Wasps):
         * 1. Mappa nome gruppo a tipo quiz (es. "Bees" → "bee")
         * 2. Carica il quiz specifico
         * 3. Naviga a QuizStep.Question
         *
         * GRUPPI SENZA QUIZ (Beetles, Bee flies, Hoverflies):
         * 1. Carica lista completa insetti del gruppo
         * 2. Naviga a QuizStep.InsectsList
         *
         * @param groupName Nome inglese del gruppo (es. "Bees", "Beetles")
         * @param groupId ID del gruppo nel database
         */
        override fun selectInsectType(groupName: String, groupId: String) {
            viewModelScope.launch {
                _state.update {
                    it.copy(
                        loading = true,
                        selectedInsectType = groupName,
                        selectedGroupId = groupId,
                        error = null
                    )
                }

                // Mappa nome gruppo → tipo quiz
                val groupToQuizType = mapOf(
                    "Bees" to "bee",
                    "Butterflies" to "butterfly",
                    "Moths" to "moth",
                    "Wasps" to "wasp"
                )
                val quizType = groupToQuizType[groupName]

                try {
                    if (quizType != null) {
                        // CASO 1: Gruppo con quiz
                        val quiz = quizRepository.getQuiz(quizType)

                        if (quiz != null) {
                            val rootId = quiz.rootQuestionId
                            val rootQuestion = quizRepository.getRootQuestion(rootId)

                            if (rootQuestion != null) {
                                val answers = quizRepository.getAnswers(rootQuestion.id)

                                if (answers.isEmpty()) {
                                    _state.update {
                                        it.copy(
                                            error = R.string.network_error_connection,
                                            loading = false
                                        )
                                    }
                                    return@launch
                                }

                                _state.update {
                                    it.copy(
                                        quizType = quizType,
                                        originalQuizType = "insect",
                                        step = QuizStep.Question,
                                        currentQuestion = rootQuestion,
                                        answers = answers,
                                        loading = false,
                                        error = null
                                    )
                                }
                            } else {
                                _state.update {
                                    it.copy(error = R.string.quiz_error_not_found, loading = false)
                                }
                            }
                        } else {
                            _state.update {
                                it.copy(error = R.string.quiz_error_not_found, loading = false)
                            }
                        }
                    } else {
                        // CASO 2: Gruppo senza quiz (lista diretta)
                        val insects = insectsRepository.getInsectsByGroup(groupId)

                        if (insects.isEmpty()) {
                            _state.update {
                                it.copy(error = R.string.quiz_error_load_insects, loading = false)
                            }
                            return@launch
                        }

                        _state.update {
                            it.copy(
                                insectsForSelection = insects,
                                step = QuizStep.InsectsList,
                                loading = false,
                                error = null
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e("QuizViewModel", "Error selecting insect type", e)
                    _state.update {
                        it.copy(
                            error = R.string.network_error_connection,
                            loading = false
                        )
                    }
                }
            }
        }

        /**
         * Gestisce la selezione diretta di un insetto dalla lista.
         *
         * Usato per gruppi senza quiz (Beetles, Beeflies, Hoverflies).
         * L'utente seleziona direttamente la specie dalla lista completa.
         *
         * @param insect Insetto selezionato dall'utente
         */
        override fun selectInsectFromList(insect: Insect) {
            // Crea TargetWithDetails dal singolo insetto
            val targetWithDetails = TargetWithDetails(
                target = QuizAnswerTarget(
                    answerId = "", // Non rilevante per selezione diretta
                    targetId = insect.id,
                    targetType = "insect"
                ),
                name = insect.name,
                imageUrl = insect.insectImage
            )

            _state.update {
                it.copy(
                    selectedTarget = targetWithDetails,
                    step = QuizStep.Result
                )
            }
        }

        /**
         * Gestisce la risposta a una domanda del quiz.
         *
         * Processo:
         * 1. Controlla se la risposta ha nextQuestion
         * 2a. Se SÌ: Carica la prossima domanda e le sue risposte
         * 2b. Se NO: È una risposta foglia, carica i target
         * 3. Se target singolo: Vai direttamente al risultato
         * 4. Se target multipli: Vai alla selezione target
         *
         * Cuore della navigazione nell'albero decisionale.
         *
         * @param answer Risposta selezionata dall'utente
         */
        override fun answerQuestion(answer: QuizAnswer) {
            viewModelScope.launch {
                _state.update {
                    it.copy(loading = true, selectedAnswer = answer, error = null)
                }

                try {
                    // Controlla se c'è una prossima domanda
                    val nextQuestion = quizRepository.getNextQuestion(answer)

                    if (nextQuestion != null) {
                        // CASO 1: Risposta intermedia → carica prossima domanda
                        val answers = quizRepository.getAnswers(nextQuestion.id)

                        if (answers.isEmpty()) {
                            _state.update {
                                it.copy(error = R.string.network_error_connection, loading = false)
                            }
                            return@launch
                        }

                        _state.update {
                            it.copy(
                                currentQuestion = nextQuestion,
                                answers = answers,
                                selectedAnswer = null,
                                loading = false,
                                error = null
                            )
                        }
                    } else {
                        // CASO 2: Risposta foglia → recupera target
                        val targets = quizRepository.getTargets(answer.id)

                        if (targets.isEmpty()) {
                            _state.update {
                                it.copy(
                                    error = R.string.network_error_connection,
                                    loading = false
                                )
                            }
                            return@launch
                        }

                        // Carica dettagli completi dei target (nome, immagine)
                        val targetsWithDetails = loadTargetDetails(targets)

                        if (targetsWithDetails.isEmpty()) {
                            _state.update {
                                it.copy(
                                    error = R.string.network_error_connection,
                                    loading = false
                                )
                            }
                            return@launch
                        }

                        if (targetsWithDetails.size == 1) {
                            // CASO 2a: Target singolo → risultato diretto
                            _state.update {
                                it.copy(
                                    step = QuizStep.Result,
                                    possibleTargets = targetsWithDetails,
                                    selectedTarget = targetsWithDetails.first(),
                                    loading = false,
                                    error = null
                                )
                            }
                        } else {
                            // CASO 2b: Target multipli → selezione
                            _state.update {
                                it.copy(
                                    step = QuizStep.TargetSelection,
                                    possibleTargets = targetsWithDetails,
                                    selectedTarget = null,
                                    loading = false,
                                    error = null
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("QuizViewModel", "Error answering question", e)
                    _state.update {
                        it.copy(error = R.string.network_error_connection, loading = false)
                    }
                    return@launch
                }
            }
        }

        /**
         * Seleziona un target dalla lista multipla.
         *
         * Chiamato dalla schermata TargetSelection quando l'utente
         * sceglie tra più specie suggerite.
         */
        override fun selectTarget(target: TargetWithDetails) {
            _state.update {
                it.copy(
                    selectedTarget = target,
                    step = QuizStep.Result
                )
            }
        }

        /**
         * Reset completo del quiz.
         *
         * Riporta tutto allo stato iniziale vuoto.
         * Usato quando si esce dal quiz o si torna alla home.
         */
        override fun resetQuiz() {
            _state.value = QuizState()
        }

        /**
         * Reset del quiz mantenendo la foto caricata.
         *
         * Usato dal pulsante "Riprova" nella schermata risultato.
         * Permette di rifare il quiz con la stessa foto senza ricaricarla.
         */
        override fun resetQuizKeepingPhoto() {
            val currentPhotoUrl = _state.value.photoUrl
            val currentOriginalQuizType = _state.value.originalQuizType

            _state.value = QuizState(
                photoUrl = currentPhotoUrl,
                quizType = currentOriginalQuizType,
                originalQuizType = currentOriginalQuizType,
                step = QuizStep.Start
            )
        }

        /**
         * Carica un avvistamento dal risultato del quiz.
         *
         * Permette all'utente di salvare rapidamente un avvistamento
         * usando i dati del quiz completato.
         *
         * Processo:
         * 1. Upload immagine su Supabase Storage
         * 2. Inserimento avvistamento nel database
         * 3. Usa data/ora corrente e coordinate 0,0 (placeholder)
         *
         * Le coordinate sono 0,0 perché nel quiz non viene
         * richiesta la posizione.
         *
         * @param context Context Android per accesso risorse
         * @param userId ID utente autenticato
         */
        override fun submitQuizSighting(context: Context, userId: String) {
            val s = _state.value

            // Validazione: servono target e foto
            if (s.selectedTarget == null || s.photoUrl.isNullOrBlank()) {
                _state.update {
                    it.copy(uploadSuccess = false, uploadError = R.string.quiz_error_missing_data)
                }
                return
            }

            viewModelScope.launch {
                _state.update {
                    it.copy(isUploading = true, uploadSuccess = null, uploadError = null)
                }

                try {
                    val uri = Uri.parse(s.photoUrl)

                    // Determina se foto è già su server (remote) o locale
                    val isRemote = s.photoUrl.startsWith("http")

                    // Upload immagine solo se locale
                    val imageUrl: String? = if (isRemote) {
                        s.photoUrl
                    } else {
                        imageRepository.uploadSightingImage(userId, uri, context)
                    }

                    if (imageUrl == null) {
                        _state.update {
                            it.copy(
                                isUploading = false,
                                uploadSuccess = false,
                                uploadError = R.string.quiz_error_upload_image
                            )
                        }
                        return@launch
                    }

                    // Recupera data/ora corrente
                    val now = Clock.System.now()
                        .toLocalDateTime(TimeZone.currentSystemDefault())

                    // Normalizza targetType (bee/butterfly/moth/wasp → insect)
                    val normalizedTargetType = when (s.selectedTarget.target.targetType) {
                        "bee", "butterfly", "moth", "wasp" -> "insect"
                        else -> s.selectedTarget.target.targetType
                    }

                    // Inserisci avvistamento nel database
                    val success = sightingsRepository.addSighting(
                        userId = userId,
                        imageUrl = imageUrl,
                        targetId = s.selectedTarget.target.targetId,
                        targetType = normalizedTargetType,
                        date = now.date,
                        time = now.time,
                        latitude = 0.0,  // Placeholder: quiz non richiede posizione
                        longitude = 0.0  // Placeholder
                    )

                    _state.update {
                        it.copy(
                            isUploading = false,
                            uploadSuccess = success,
                            uploadError = if (!success) R.string.quiz_error_database else null
                        )
                    }
                } catch (e: Exception) {
                    Log.e("QuizViewModel", "Error submitting quiz sighting", e)
                    _state.update {
                        it.copy(
                            isUploading = false,
                            uploadSuccess = false,
                            uploadError = R.string.network_error_connection
                        )
                    }
                }
            }
        }
    }

    /**
     * Funzione privata per caricare i dettagli completi dei target.
     *
     * Trasforma QuizAnswerTarget (solo ID e tipo) in TargetWithDetails
     * (con nome, immagini, ecc.) caricando i dati dalle tabelle Plant o Insect.
     *
     * Gestisce sia piante che insetti con mapping corretto dei campi:
     * - Plant: nameEn, nameIt, imageUrl
     * - Insect: name (singolo), insectImage
     *
     * @param targets Lista di QuizAnswerTarget da arricchire
     * @return Lista di TargetWithDetails con dati completi (esclude target non trovati)
     */
    private suspend fun loadTargetDetails(targets: List<QuizAnswerTarget>): List<TargetWithDetails> {
        return targets.mapNotNull { target ->
            try {
                when (target.targetType) {
                    "plant" -> {
                        val plant = quizRepository.getPlant(target.targetId)
                        plant?.let {
                            TargetWithDetails(
                                target = target,
                                nameEn = it.nameEn,
                                nameIt = it.nameIt,
                                imageUrl = it.imageUrl
                            )
                        }
                    }
                    "insect", "bee", "wasp", "butterfly", "moth" -> {
                        val insect = quizRepository.getInsect(target.targetId)
                        insect?.let {
                            TargetWithDetails(
                                target = target,
                                name = it.name,
                                imageUrl = insect.insectImage
                            )
                        }
                    }
                    else -> null
                }
            } catch (e: Exception) {
                Log.e("QuizViewModel", "Error loading target details: ${target.targetId}", e)
                null // Filtra target con errori
            }
        }
    }
}
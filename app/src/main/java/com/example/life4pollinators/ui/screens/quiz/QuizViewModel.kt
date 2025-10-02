package com.example.life4pollinators.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.life4pollinators.data.database.entities.*
import com.example.life4pollinators.data.repositories.QuizRepository
import com.example.life4pollinators.data.repositories.InsectsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class QuizStep {
    data object Start : QuizStep()
    data object InsectTypeSelection : QuizStep()
    data object InsectsList : QuizStep()
    data object Question : QuizStep()
    data object TargetSelection : QuizStep()
    data object Result : QuizStep()
}

data class TargetWithDetails(
    val target: QuizAnswerTarget,
    val name: String? = "",
    val nameEn: String? = "",
    val nameIt: String? = "",
    val imageUrl: String? = null
)

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
    val error: String? = null
)

interface QuizActions {
    fun setQuizType(type: String)
    fun startQuiz(photoUrl: String?)
    fun loadInsectGroups(photoUrl: String?)
    fun selectInsectType(groupName: String, groupId: String)
    fun selectInsectFromList(insect: Insect)
    fun answerQuestion(answer: QuizAnswer)
    fun selectTarget(target: TargetWithDetails)
    fun resetQuiz()
    fun resetQuizKeepingPhoto() // NUOVO: reset mantenendo la foto
}

class QuizViewModel(
    private val quizRepository: QuizRepository,
    private val insectsRepository: InsectsRepository
) : ViewModel() {
    private val _state = MutableStateFlow(QuizState())
    val state = _state.asStateFlow()

    val actions = object : QuizActions {
        override fun setQuizType(type: String) {
            _state.update { it.copy(quizType = type, originalQuizType = type) }
        }

        override fun startQuiz(photoUrl: String?) {
            val type = _state.value.quizType

            // Se è insect generico, carica i gruppi per la selezione
            if (type == "insect") {
                loadInsectGroups(photoUrl)
                return
            }

            // Altrimenti avvia il quiz normale per plant o per tipo specifico
            viewModelScope.launch {
                _state.update { it.copy(step = QuizStep.Start, photoUrl = photoUrl, loading = true) }
                val quiz = quizRepository.getQuiz(type)
                println("QUIZ: $quiz")
                val rootId = quiz?.rootQuestionId
                println("Root Question Id: $rootId")
                val rootQuestion = rootId?.let { quizRepository.getRootQuestion(it) }
                println("Root Question loaded: $rootQuestion")
                if (rootQuestion != null) {
                    val answers = quizRepository.getAnswers(rootQuestion.id)
                    _state.update {
                        it.copy(
                            step = QuizStep.Question,
                            currentQuestion = rootQuestion,
                            answers = answers,
                            loading = false
                        )
                    }
                } else {
                    _state.update { it.copy(error = "Quiz not found", loading = false) }
                }
            }
        }

        override fun loadInsectGroups(photoUrl: String?) {
            viewModelScope.launch {
                _state.update { it.copy(loading = true, photoUrl = photoUrl) }
                try {
                    val groups = insectsRepository.getInsectGroups()
                    _state.update {
                        it.copy(
                            insectGroups = groups,
                            step = QuizStep.InsectTypeSelection,
                            loading = false
                        )
                    }
                } catch (e: Exception) {
                    _state.update {
                        it.copy(
                            error = "Failed to load insect groups",
                            loading = false
                        )
                    }
                }
            }
        }

        override fun selectInsectType(groupName: String, groupId: String) {
            viewModelScope.launch {
                _state.update { it.copy(loading = true, selectedInsectType = groupName, selectedGroupId = groupId) }

                // Mappa dei gruppi che hanno un quiz (nome gruppo -> tipo quiz)
                val groupToQuizType = mapOf(
                    "Bees" to "bee",
                    "Butterflies" to "butterfly",
                    "Moths" to "moth",
                    "Wasps" to "wasp"
                )

                val quizType = groupToQuizType[groupName]

                if (quizType != null) {
                    // Avvia il quiz per questo tipo
                    val quiz = quizRepository.getQuiz(quizType)
                    println("QUIZ for $groupName (type: $quizType): $quiz")

                    if (quiz != null) {
                        val rootId = quiz.rootQuestionId
                        val rootQuestion = quizRepository.getRootQuestion(rootId)

                        if (rootQuestion != null) {
                            val answers = quizRepository.getAnswers(rootQuestion.id)
                            _state.update {
                                it.copy(
                                    quizType = quizType,
                                    originalQuizType = "insect",
                                    step = QuizStep.Question,
                                    currentQuestion = rootQuestion,
                                    answers = answers,
                                    loading = false
                                )
                            }
                        } else {
                            _state.update { it.copy(error = "Quiz not found", loading = false) }
                        }
                    } else {
                        _state.update { it.copy(error = "Quiz not found", loading = false) }
                    }
                } else {
                    // Carica la lista degli insetti per questo gruppo
                    try {
                        val insects = insectsRepository.getInsectsByGroup(groupId)
                        _state.update {
                            it.copy(
                                insectsForSelection = insects,
                                step = QuizStep.InsectsList,
                                loading = false
                            )
                        }
                    } catch (e: Exception) {
                        _state.update {
                            it.copy(
                                error = "Failed to load insects",
                                loading = false
                            )
                        }
                    }
                }
            }
        }

        override fun selectInsectFromList(insect: Insect) {
            // Crea un TargetWithDetails dall'insetto selezionato
            val targetWithDetails = TargetWithDetails(
                target = QuizAnswerTarget(
                    answerId = "",
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

        override fun answerQuestion(answer: QuizAnswer) {
            viewModelScope.launch {
                println("Answer selected: $answer")
                println("Answer.nextQuestion: ${answer.nextQuestion}")
                _state.update { it.copy(loading = true, selectedAnswer = answer) }
                val nextQuestion = quizRepository.getNextQuestion(answer)
                println("Next question loaded: $nextQuestion")
                if (nextQuestion != null) {
                    val answers = quizRepository.getAnswers(nextQuestion.id)
                    _state.update {
                        it.copy(
                            currentQuestion = nextQuestion,
                            answers = answers,
                            selectedAnswer = null,
                            loading = false
                        )
                    }
                } else {
                    val targets = quizRepository.getTargets(answer.id)
                    println("DEBUG: Answer ID: ${answer.id}")
                    println("DEBUG: Targets found: ${targets.size}")
                    targets.forEach { target ->
                        println("DEBUG: Target - ID: ${target.targetId}, Type: ${target.targetType}")
                    }
                    val targetsWithDetails = loadTargetDetails(targets)
                    println("DEBUG: Targets with details: ${targetsWithDetails.size}")

                    if (targetsWithDetails.size == 1) {
                        _state.update {
                            it.copy(
                                step = QuizStep.Result,
                                possibleTargets = targetsWithDetails,
                                selectedTarget = targetsWithDetails.first(),
                                loading = false
                            )
                        }
                    } else if (targetsWithDetails.size > 1) {
                        _state.update {
                            it.copy(
                                step = QuizStep.TargetSelection,
                                possibleTargets = targetsWithDetails,
                                selectedTarget = null,
                                loading = false
                            )
                        }
                    } else {
                        _state.update {
                            it.copy(
                                step = QuizStep.Result,
                                possibleTargets = emptyList(),
                                selectedTarget = null,
                                loading = false,
                                error = "No classification found."
                            )
                        }
                    }
                }
            }
        }

        override fun selectTarget(target: TargetWithDetails) {
            _state.update {
                it.copy(
                    selectedTarget = target,
                    step = QuizStep.Result
                )
            }
        }

        override fun resetQuiz() {
            _state.value = QuizState()
        }

        // NUOVO: Reset mantenendo foto e tipo quiz originale
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
    }

    private suspend fun loadTargetDetails(targets: List<QuizAnswerTarget>): List<TargetWithDetails> {
        return targets.mapNotNull { target ->
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
        }
    }
}
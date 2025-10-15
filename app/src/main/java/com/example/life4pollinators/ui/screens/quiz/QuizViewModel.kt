package com.example.life4pollinators.ui.screens.quiz

import android.content.Context
import android.net.Uri
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
    val error: Int? = null,  // Cambiato da String? a Int? per ID risorsa
    val isUploading: Boolean = false,
    val uploadSuccess: Boolean? = null,
    val uploadError: Int? = null  // Cambiato da String? a Int? per ID risorsa
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
    fun resetQuizKeepingPhoto()
    fun submitQuizSighting(context: Context, userId: String)
}

class QuizViewModel(
    private val quizRepository: QuizRepository,
    private val insectsRepository: InsectsRepository,
    private val sightingsRepository: SightingsRepository,
    private val imageRepository: ImageRepository
) : ViewModel(), KoinComponent {

    private val _state = MutableStateFlow(QuizState())
    val state = _state.asStateFlow()

    val actions = object : QuizActions {
        override fun setQuizType(type: String) {
            _state.update { it.copy(quizType = type, originalQuizType = type) }
        }

        override fun startQuiz(photoUrl: String?) {
            val type = _state.value.quizType
            if (type == "insect") {
                loadInsectGroups(photoUrl)
                return
            }
            viewModelScope.launch {
                _state.update { it.copy(step = QuizStep.Start, photoUrl = photoUrl, loading = true) }
                val quiz = quizRepository.getQuiz(type)
                val rootId = quiz?.rootQuestionId
                val rootQuestion = rootId?.let { quizRepository.getRootQuestion(it) }
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
                    _state.update { it.copy(error = R.string.quiz_error_not_found, loading = false) }
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
                            error = R.string.quiz_error_load_groups,
                            loading = false
                        )
                    }
                }
            }
        }

        override fun selectInsectType(groupName: String, groupId: String) {
            viewModelScope.launch {
                _state.update { it.copy(loading = true, selectedInsectType = groupName, selectedGroupId = groupId) }
                val groupToQuizType = mapOf(
                    "Bees" to "bee",
                    "Butterflies" to "butterfly",
                    "Moths" to "moth",
                    "Wasps" to "wasp"
                )
                val quizType = groupToQuizType[groupName]
                if (quizType != null) {
                    val quiz = quizRepository.getQuiz(quizType)
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
                            _state.update { it.copy(error = R.string.quiz_error_not_found, loading = false) }
                        }
                    } else {
                        _state.update { it.copy(error = R.string.quiz_error_not_found, loading = false) }
                    }
                } else {
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
                                error = R.string.quiz_error_load_insects,
                                loading = false
                            )
                        }
                    }
                }
            }
        }

        override fun selectInsectFromList(insect: Insect) {
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
                _state.update { it.copy(loading = true, selectedAnswer = answer) }
                val nextQuestion = quizRepository.getNextQuestion(answer)
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
                    val targetsWithDetails = loadTargetDetails(targets)
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
                                error = R.string.quiz_error_no_classification
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

        override fun submitQuizSighting(context: Context, userId: String) {
            val s = _state.value
            if (s.selectedTarget == null || s.photoUrl.isNullOrBlank()) {
                _state.update { it.copy(uploadSuccess = false, uploadError = R.string.quiz_error_missing_data) }
                return
            }
            viewModelScope.launch {
                _state.update { it.copy(isUploading = true, uploadSuccess = null, uploadError = null) }
                val uri = Uri.parse(s.photoUrl)
                val isRemote = s.photoUrl.startsWith("http")
                val imageUrl: String? = if (isRemote) s.photoUrl
                else imageRepository.uploadSightingImage(userId, uri, context)
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
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val success = sightingsRepository.addSighting(
                    userId = userId,
                    imageUrl = imageUrl,
                    targetId = s.selectedTarget.target.targetId,
                    targetType = s.selectedTarget.target.targetType,
                    date = now.date,
                    time = now.time,
                    latitude = 0.0,
                    longitude = 0.0
                )
                _state.update {
                    it.copy(
                        isUploading = false,
                        uploadSuccess = success,
                        uploadError = if (!success) R.string.quiz_error_database else null
                    )
                }
            }
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
package com.example.life4pollinators.ui.screens.plantQuiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.life4pollinators.data.database.entities.*
import com.example.life4pollinators.data.repositories.QuizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class PlantQuizStep {
    data object Start : PlantQuizStep()
    data object Question : PlantQuizStep()
    data object TargetSelection : PlantQuizStep()
    data object Result : PlantQuizStep()
}

data class PlantQuizState(
    val step: PlantQuizStep = PlantQuizStep.Start,
    val photoUrl: String? = null,
    val currentQuestion: QuizQuestion? = null,
    val answers: List<QuizAnswer> = emptyList(),
    val selectedAnswer: QuizAnswer? = null,
    val possibleTargets: List<QuizAnswerTarget> = emptyList(),
    val selectedTarget: QuizAnswerTarget? = null,
    val loading: Boolean = false,
    val error: String? = null
)

interface PlantQuizActions {
    fun startQuiz(photoUrl: String?)
    fun answerQuestion(answer: QuizAnswer)
    fun selectTarget(target: QuizAnswerTarget)
    fun resetQuiz()
}

class QuizViewModel(
    private val repository: QuizRepository
) : ViewModel() {
    private val _state = MutableStateFlow(PlantQuizState())
    val state = _state.asStateFlow()

    private var quizType: String = "plant"

    val actions = object : PlantQuizActions {
        override fun startQuiz(photoUrl: String?) {
            viewModelScope.launch {
                _state.update { it.copy(step = PlantQuizStep.Start, photoUrl = photoUrl, loading = true) }
                val quiz = repository.getQuiz(quizType)
                val rootQuestion = quiz?.rootQuestion?.let { repository.getRootQuestion(it) }
                if (rootQuestion != null) {
                    val answers = repository.getAnswers(rootQuestion.id)
                    _state.update {
                        it.copy(
                            step = PlantQuizStep.Question,
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
        override fun answerQuestion(answer: QuizAnswer) {
            viewModelScope.launch {
                _state.update { it.copy(loading = true, selectedAnswer = answer) }
                val nextQuestion = answer.nextQuestion?.let { repository.getRootQuestion(it) }
                if (nextQuestion != null) {
                    val answers = repository.getAnswers(nextQuestion.id)
                    _state.update {
                        it.copy(
                            currentQuestion = nextQuestion,
                            answers = answers,
                            selectedAnswer = null,
                            loading = false
                        )
                    }
                } else {
                    val targets = repository.getTargets(answer.id)
                    if (targets.size == 1) {
                        _state.update {
                            it.copy(
                                step = PlantQuizStep.Result,
                                possibleTargets = targets,
                                selectedTarget = targets.first(),
                                loading = false
                            )
                        }
                    } else if (targets.size > 1) {
                        _state.update {
                            it.copy(
                                step = PlantQuizStep.TargetSelection,
                                possibleTargets = targets,
                                selectedTarget = null,
                                loading = false
                            )
                        }
                    } else {
                        _state.update {
                            it.copy(
                                step = PlantQuizStep.Result,
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
        override fun selectTarget(target: QuizAnswerTarget) {
            _state.update {
                it.copy(
                    selectedTarget = target,
                    step = PlantQuizStep.Result
                )
            }
        }
        override fun resetQuiz() {
            _state.value = PlantQuizState()
        }
    }
}
package com.example.life4pollinators.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.life4pollinators.data.database.entities.*
import com.example.life4pollinators.data.repositories.QuizRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class QuizStep {
    data object Start : QuizStep()
    data object Question : QuizStep()
    data object TargetSelection : QuizStep()
    data object Result : QuizStep()
}

data class QuizState(
    val quizType: String = "", // "plant", "insect", ...
    val step: QuizStep = QuizStep.Start,
    val photoUrl: String? = null,
    val currentQuestion: QuizQuestion? = null,
    val answers: List<QuizAnswer> = emptyList(),
    val selectedAnswer: QuizAnswer? = null,
    val possibleTargets: List<QuizAnswerTarget> = emptyList(),
    val selectedTarget: QuizAnswerTarget? = null,
    val loading: Boolean = false,
    val error: String? = null
)

interface QuizActions {
    fun setQuizType(type: String)
    fun startQuiz(photoUrl: String?)
    fun answerQuestion(answer: QuizAnswer)
    fun selectTarget(target: QuizAnswerTarget)
    fun resetQuiz()
}

class QuizViewModel(
    private val repository: QuizRepository
) : ViewModel() {
    private val _state = MutableStateFlow(QuizState())
    val state = _state.asStateFlow()

    val actions = object : QuizActions {
        override fun setQuizType(type: String) {
            _state.update { it.copy(quizType = type) }
        }

        override fun startQuiz(photoUrl: String?) {
            val type = _state.value.quizType
            viewModelScope.launch {
                _state.update { it.copy(step = QuizStep.Start, photoUrl = photoUrl, loading = true) }
                val quiz = repository.getQuiz(type)
                println("QUIZ: $quiz")
                val rootId = quiz?.rootQuestionId
                println("Root Question Id: $rootId")
                val rootQuestion = rootId?.let { repository.getRootQuestion(it) }
                println("Root Question loaded: $rootQuestion")
                if (rootQuestion != null) {
                    val answers = repository.getAnswers(rootQuestion.id)
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
        override fun answerQuestion(answer: QuizAnswer) {
            viewModelScope.launch {
                println("Answer selected: $answer")
                println("Answer.nextQuestion: ${answer.nextQuestion}")
                _state.update { it.copy(loading = true, selectedAnswer = answer) }
                val nextQuestion = repository.getNextQuestion(answer)
                println("Next question loaded: $nextQuestion")
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
                                step = QuizStep.Result,
                                possibleTargets = targets,
                                selectedTarget = targets.first(),
                                loading = false
                            )
                        }
                    } else if (targets.size > 1) {
                        _state.update {
                            it.copy(
                                step = QuizStep.TargetSelection,
                                possibleTargets = targets,
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
        override fun selectTarget(target: QuizAnswerTarget) {
            _state.update {
                it.copy(
                    selectedTarget = target,
                    step = QuizStep.Result
                )
            }
        }
        override fun resetQuiz() {
            _state.value = _state.value.copy(
                step = QuizStep.Start,
                photoUrl = null,
                currentQuestion = null,
                answers = emptyList(),
                selectedAnswer = null,
                possibleTargets = emptyList(),
                selectedTarget = null,
                loading = false,
                error = null
            )
        }
    }
}
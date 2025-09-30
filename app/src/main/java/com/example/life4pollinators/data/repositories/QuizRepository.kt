package com.example.life4pollinators.data.repositories

import com.example.life4pollinators.data.database.entities.*
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class QuizRepository(
    supabase: SupabaseClient
) {
    private val quizTable = supabase.from("quiz")
    private val questionTable = supabase.from("quiz_question")
    private val answerTable = supabase.from("quiz_answer")
    private val answerTargetTable = supabase.from("quiz_answer_target")
    private val plantTable = supabase.from("plant")
    private val insectTable = supabase.from("insect")

    // Recupera il quiz per tipo ("plant", "insect", ecc)
    suspend fun getQuiz(type: String): Quiz? {
        println("Cerco quiz type: '$type'")
        val quiz = quizTable.select {
            filter { Quiz::type eq type }
        }.decodeList<Quiz>()
        println("Quiz trovati: $quiz")
        return quiz.firstOrNull()
    }

    // Recupera la domanda root dal quiz
    suspend fun getRootQuestion(rootQuestionId: String): QuizQuestion? {
        val result = questionTable.select {
            filter { QuizQuestion::id eq rootQuestionId }
        }.decodeList<QuizQuestion>()
        println("Result for getRootQuestion($rootQuestionId): $result")
        return result.firstOrNull()
    }

    // Recupera tutte le risposte per una domanda
    suspend fun getAnswers(questionId: String): List<QuizAnswer> {
        return answerTable.select {
            filter { QuizAnswer::questionId eq questionId }
        }.decodeList<QuizAnswer>()
    }

    // Recupera la domanda successiva per una risposta, se esiste
    suspend fun getNextQuestion(answer: QuizAnswer): QuizQuestion? {
        val nextId = answer.nextQuestion
        return if (nextId != null) {
            questionTable.select {
                filter { QuizQuestion::id eq nextId }
            }.decodeList<QuizQuestion>().firstOrNull()
        } else null
    }

    // Recupera i target associati a una risposta foglia
    suspend fun getTargets(answerId: String): List<QuizAnswerTarget> {
        return answerTargetTable.select {
            filter { QuizAnswerTarget::answerId eq answerId }
        }.decodeList<QuizAnswerTarget>()
    }

    // Recupera oggetto target, ad esempio la pianta corrispondente
    suspend fun getTargetObject(targetId: String, targetType: String): Any? {
        return when (targetType) {
            "plant" -> {
                plantTable.select {
                    filter { Plant::id eq targetId }
                }.decodeList<Plant>().firstOrNull()
            }
            "insect" -> {
                insectTable.select {
                    filter { Insect::id eq targetId }
                }.decodeList<Insect>().firstOrNull()
            }
            else -> null
        }
    }
}
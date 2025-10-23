package com.example.life4pollinators.data.repositories

import android.util.Log
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

    // Recupera il quiz per tipo ("plant", "bee", "butterfly", "moth" o "wasp")
    // (per i gruppi beetle, beefly e hoverfly non esiste un quiz nel db)
    suspend fun getQuiz(type: String): Quiz? {
        return try {
            println("Cerco quiz type: '$type'")
            val quiz = quizTable.select {
                filter { Quiz::type eq type }
            }.decodeList<Quiz>()
            println("Quiz trovati: $quiz")
            quiz.firstOrNull()
        } catch (e: Exception) {
            Log.e("QuizRepository", "Error fetching quiz for type: $type", e)
            null
        }
    }

    // Recupera la domanda root dal quiz
    suspend fun getRootQuestion(rootQuestionId: String): QuizQuestion? {
        return try {
            val result = questionTable.select {
                filter { QuizQuestion::id eq rootQuestionId }
            }.decodeList<QuizQuestion>()
            println("Result for getRootQuestion($rootQuestionId): $result")
            result.firstOrNull()
        } catch (e: Exception) {
            Log.e("QuizRepository", "Error fetching root question: $rootQuestionId", e)
            null
        }
    }

    // Recupera tutte le risposte per una domanda
    suspend fun getAnswers(questionId: String): List<QuizAnswer> {
        return try {
            answerTable.select {
                filter { QuizAnswer::questionId eq questionId }
            }.decodeList<QuizAnswer>()
        } catch (e: Exception) {
            Log.e("QuizRepository", "Error fetching answers for question: $questionId", e)
            emptyList()
        }
    }

    // Recupera la domanda successiva per una risposta, se esiste
    suspend fun getNextQuestion(answer: QuizAnswer): QuizQuestion? {
        val nextId = answer.nextQuestion
        return if (nextId != null) {
            try {
                questionTable.select {
                    filter { QuizQuestion::id eq nextId }
                }.decodeList<QuizQuestion>().firstOrNull()
            } catch (e: Exception) {
                Log.e("QuizRepository", "Error fetching next question: $nextId", e)
                null
            }
        } else null
    }

    // Recupera i target associati a una risposta foglia
    suspend fun getTargets(answerId: String): List<QuizAnswerTarget> {
        return try {
            answerTargetTable.select {
                filter { QuizAnswerTarget::answerId eq answerId }
            }.decodeList<QuizAnswerTarget>()
        } catch (e: Exception) {
            Log.e("QuizRepository", "Error fetching targets for answer: $answerId", e)
            emptyList()
        }
    }

    // Recupera oggetto target, ad esempio la pianta corrispondente
    suspend fun getTargetObject(targetId: String, targetType: String): Any? {
        return try {
            when (targetType) {
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
        } catch (e: Exception) {
            Log.e("QuizRepository", "Error fetching target object: $targetId, type: $targetType", e)
            null
        }
    }

    // Recupera una pianta per ID
    suspend fun getPlant(plantId: String): Plant? {
        return try {
            plantTable.select {
                filter { Plant::id eq plantId }
            }.decodeList<Plant>().firstOrNull()
        } catch (e: Exception) {
            Log.e("QuizRepository", "Error fetching plant: $plantId", e)
            null
        }
    }

    // Recupera un insetto per ID
    suspend fun getInsect(insectId: String): Insect? {
        return try {
            insectTable.select {
                filter { Insect::id eq insectId }
            }.decodeList<Insect>().firstOrNull()
        } catch (e: Exception) {
            Log.e("QuizRepository", "Error fetching insect: $insectId", e)
            null
        }
    }

    // Recupera più piante per lista di ID
    suspend fun getPlants(plantIds: List<String>): List<Plant> {
        if (plantIds.isEmpty()) return emptyList()
        return try {
            plantTable.select {
                filter { Plant::id isIn plantIds }
            }.decodeList<Plant>()
        } catch (e: Exception) {
            Log.e("QuizRepository", "Error fetching plants list", e)
            emptyList()
        }
    }

    // Recupera più insetti per lista di ID
    suspend fun getInsects(insectIds: List<String>): List<Insect> {
        if (insectIds.isEmpty()) return emptyList()
        return try {
            insectTable.select {
                filter { Insect::id isIn insectIds }
            }.decodeList<Insect>()
        } catch (e: Exception) {
            Log.e("QuizRepository", "Error fetching insects list", e)
            emptyList()
        }
    }
}
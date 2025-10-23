package com.example.life4pollinators.data.repositories

import android.util.Log
import com.example.life4pollinators.data.database.entities.*
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

/**
 * Repository per la gestione dei quiz e degli alberi decisionali.
 *
 * Responsabilità:
 * - Navigazione nell'albero decisionale del quiz
 * - Recupero domande, risposte e target
 * - Caricamento entità correlate (Plant, Insect)
 * - Gestione dei diversi tipi di quiz
 *
 * Architettura Quiz:
 * Quiz → Root Question → Answers → Next Questions → ... → Leaf Answers → Target/s -> Result
 *
 * @param supabase Client Supabase per accesso al database
 */
class QuizRepository(
    supabase: SupabaseClient
) {
    // Riferimenti alle tabelle del database
    private val quizTable = supabase.from("quiz")
    private val questionTable = supabase.from("quiz_question")
    private val answerTable = supabase.from("quiz_answer")
    private val answerTargetTable = supabase.from("quiz_answer_target")
    private val plantTable = supabase.from("plant")
    private val insectTable = supabase.from("insect")

    /**
     * Recupera il quiz per tipo.
     *
     * Tipi supportati con quiz:
     * - "plant": Quiz per identificare piante
     * - "bee": Quiz per identificare api
     * - "butterfly": Quiz per identificare farfalle
     * - "moth": Quiz per identificare falene
     * - "wasp": Quiz per identificare vespe
     *
     * Tipi SENZA quiz (usano lista diretta):
     * - "beetle": Coleotteri
     * - "beefly": Bombilidi
     * - "hoverfly": Sirfidi
     *
     * @param type Tipo del quiz da recuperare
     * @return Quiz entity se trovato, null altrimenti
     */
    suspend fun getQuiz(type: String): Quiz? {
        return try {
            println("Cerco quiz type: '$type'") // Debug log
            val quiz = quizTable.select {
                filter { Quiz::type eq type }
            }.decodeList<Quiz>()
            println("Quiz trovati: $quiz") // Debug log
            quiz.firstOrNull()
        } catch (e: Exception) {
            Log.e("QuizRepository", "Error fetching quiz for type: $type", e)
            null
        }
    }

    /**
     * Recupera la domanda radice (root) del quiz.
     *
     * La root question è il punto di partenza dell'albero decisionale.
     * Tutte le altre domande sono raggiungibili da questa attraverso
     * le risposte e i nextQuestion link.
     *
     * @param rootQuestionId ID della domanda root (dal Quiz.rootQuestionId)
     * @return QuizQuestion entity se trovata, null altrimenti
     */
    suspend fun getRootQuestion(rootQuestionId: String): QuizQuestion? {
        return try {
            val result = questionTable.select {
                filter { QuizQuestion::id eq rootQuestionId }
            }.decodeList<QuizQuestion>()
            println("Result for getRootQuestion($rootQuestionId): $result") // Debug log
            result.firstOrNull()
        } catch (e: Exception) {
            Log.e("QuizRepository", "Error fetching root question: $rootQuestionId", e)
            null
        }
    }

    /**
     * Recupera tutte le risposte disponibili per una domanda.
     *
     * Ogni domanda ha almeno 2 risposte.
     * Le risposte vengono mostrate all'utente come opzioni cliccabili.
     *
     * @param questionId ID della domanda corrente
     * @return Lista di QuizAnswer per la domanda (vuota in caso di errore)
     */
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

    /**
     * Recupera la domanda successiva per una risposta selezionata.
     *
     * Determina il prossimo passo nell'albero decisionale:
     * - Se answer.nextQuestion != null: recupera la prossima domanda
     * - Se answer.nextQuestion == null: la risposta è una foglia (risultato)
     *
     * @param answer Risposta selezionata dall'utente
     * @return QuizQuestion successiva se esiste, null se risposta foglia o errore
     */
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

    /**
     * Recupera i target associati a una risposta foglia.
     *
     * Utilizzato quando l'utente arriva al risultato finale del quiz.
     * Una risposta foglia può avere:
     * - 1 target: risultato univoco (es. "Ape mellifera")
     * - N target: risultati multipli tra cui scegliere (es. "Possibili: Ape A, Ape B, Ape C")
     *
     * @param answerId ID della risposta foglia (answer.nextQuestion == null)
     * @return Lista di QuizAnswerTarget con le specie suggerite (vuota in caso di errore)
     */
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

    /**
     * Recupera una pianta specifica per ID.
     *
     * Usato per caricare i dettagli completi di una pianta target.
     */
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

    /**
     * Recupera un insetto specifico per ID.
     *
     * Usato per caricare i dettagli completi di un insetto target.
     */
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
}
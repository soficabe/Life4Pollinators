package com.example.life4pollinators.data.database.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Rappresenta una domanda nell'albero decisionale di un quiz.
 *
 * @property id Identificativo univoco della domanda.
 * @property quizId ID del quiz a cui la domanda appartiene.
 * @property questionTextEn Testo della domanda in inglese.
 * @property questionTextIt Testo della domanda in italiano.
 * @property imageUrl URL opzionale per un'immagine che illustra la domanda.
 */
@Serializable
data class QuizQuestion(
    @SerialName("id")
    val id: String,

    @SerialName("quiz_id")
    val quizId: String,

    @SerialName("question_text_en")
    val questionTextEn: String,

    @SerialName("question_text_it")
    val questionTextIt: String,

    @SerialName("image_url")
    val imageUrl: String? = null
)
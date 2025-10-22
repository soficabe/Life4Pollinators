package com.example.life4pollinators.data.database.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Rappresenta una possibile risposta per una domanda del quiz.
 *
 * @property id Identificativo univoco della risposta.
 * @property questionId ID della domanda a cui questa risposta appartiene (domanda a cui risponde).
 * @property answerTextEn Testo della risposta in inglese.
 * @property answerTextIt Testo della risposta in italiano.
 * @property imageUrl URL opzionale di un'immagine che illustra la risposta (può essere null).
 * @property nextQuestion ID opzionale della domanda successiva nell'albero decisionale;
 *                        se null indica che questa risposta porta a un risultato/leaf.
 */
@Serializable
data class QuizAnswer(
    @SerialName("id")
    val id: String,

    @SerialName("question_id")
    val questionId: String,

    @SerialName("answer_text_en")
    val answerTextEn: String,

    @SerialName("answer_text_it")
    val answerTextIt: String,

    @SerialName("image_url")
    val imageUrl: String? = null,

    @SerialName("next_question_id")
    val nextQuestion: String? = null
)
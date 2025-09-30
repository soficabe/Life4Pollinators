package com.example.life4pollinators.data.database.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
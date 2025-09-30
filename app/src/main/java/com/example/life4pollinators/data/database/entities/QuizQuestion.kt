package com.example.life4pollinators.data.database.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
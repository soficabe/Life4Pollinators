package com.example.life4pollinators.data.database.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuizAnswerTarget(
    @SerialName("answer_id")
    val answerId: String,

    @SerialName("target_id")
    val targetId: String,

    @SerialName("target_type")
    val targetType: String
)
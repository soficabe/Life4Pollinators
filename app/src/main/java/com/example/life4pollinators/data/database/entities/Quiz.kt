package com.example.life4pollinators.data.database.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Quiz(
    @SerialName("id")
    val id: String,

    @SerialName("type")
    val type: String,

    @SerialName("root_question")
    val rootQuestion: String,
)
package com.example.life4pollinators.data.database.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Relazione che associa una risposta del quiz a un target (specie) suggerito.
 *
 * @property answerId ID della risposta che mappa al target.
 * @property targetId ID del target suggerito (può essere ID di Plant o Insect).
 * @property targetType Tipo del target (es. "plant" o "bee", "beetle", "wasp" ecc.).
 *
 * Usata per ricavare i targets associati a una specifica risposta.
 */
@Serializable
data class QuizAnswerTarget(
    @SerialName("answer_id")
    val answerId: String,

    @SerialName("target_id")
    val targetId: String,

    @SerialName("target_type")
    val targetType: String
)
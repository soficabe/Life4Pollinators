package com.example.life4pollinators.data.database.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Rappresenta un quiz disponibile nell'app (es. quiz per piante o insetti (api, farfalle, falene, vespe)).
 *
 * @property id Identificativo univoco del quiz.
 * @property type Tipo del quiz (stringa che può essere usata per distinguere categorie, es. "plant"/"bee"/"wasp" ecc.).
 * @property rootQuestionId ID della domanda radice (root) dell'albero decisionale per questo quiz.
 *                          Questa entità è utile per caricare il percorso decisionale a partire dalla radice.
 */
@Serializable
data class Quiz(
    @SerialName("id")
    val id: String,

    @SerialName("type")
    val type: String,

    @SerialName("root_question_id")
    val rootQuestionId: String
)
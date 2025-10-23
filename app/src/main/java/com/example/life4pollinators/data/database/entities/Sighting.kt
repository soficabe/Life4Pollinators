package com.example.life4pollinators.data.database.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

/**
 * Rappresenta un avvistamento (sighting) salvato nel database.
 *
 * @property id Identificatore univoco dell'avvistamento.
 * @property userId ID dell'utente che ha registrato l'avvistamento.
 * @property imageUrl URL dell'immagine caricata nello storage.
 * @property targetId ID della specie target avvistata (plant/insect).
 * @property targetType Tipo del target ("plant" o "insect").
 * @property isValidated Flag che indica se l'avvistamento è stato validato da qualcuno con l'accesso al db.
 * @property createdAt Timestamp di creazione (Instant), fornito/gestito dal backend.
 * @property date Campo per la data.
 * @property time Campo per l'ora.
 * @property latitude Coordinate geografiche latitudine.
 * @property longitude Coordinate geografiche longitudine.
 *
 * Nota: assicurarsi che `createdAt` venga deserializzato coerentemente con il backend (ISO8601).
 *       salvare l'`imageUrl` solo dopo che l'upload è andato a buon fine per evitare inconsistenze.
 */
@Serializable
data class Sighting(
    @SerialName("id")
    val id: String,

    @SerialName("user_id")
    val userId: String,

    @SerialName("image_url")
    val imageUrl: String? = null,

    @SerialName("target_id")
    val targetId: String,

    @SerialName("target_type")
    val targetType: String,

    @SerialName("is_validated")
    val isValidated: Boolean = false,

    @SerialName("created_at")
    val createdAt: Instant,

    @SerialName("date")
    val date: String? = null,

    @SerialName("time")
    val time: String? = null,

    @SerialName("latitude")
    val latitude: Double? = null,

    @SerialName("longitude")
    val longitude: Double? = null
)
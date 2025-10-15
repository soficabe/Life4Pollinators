package com.example.life4pollinators.data.database.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

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
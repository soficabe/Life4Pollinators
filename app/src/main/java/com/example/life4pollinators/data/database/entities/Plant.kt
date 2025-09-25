package com.example.life4pollinators.data.database.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Plant(
    @SerialName("id")
    val id: String,

    @SerialName("name_en")
    val nameEn: String,

    @SerialName("name_it")
    val nameIt: String,

    @SerialName("image_url")
    val imageUrl: String?,

    @SerialName("common_genera")
    val commonGenera: String,

    @SerialName("invasive_flower")
    val invasiveFlower: String?,

    @SerialName("is_diverse")
    val isDiverse: Boolean,

    @SerialName("info_en")
    val infoEn: String?,

    @SerialName("info_it")
    val infoIt: String?
)
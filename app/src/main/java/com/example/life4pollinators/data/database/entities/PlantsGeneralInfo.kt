package com.example.life4pollinators.data.database.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlantsGeneralInfo(
    @SerialName("id")
    val id: String,

    @SerialName("name_en")
    val nameEn: String,

    @SerialName("name_it")
    val nameIt: String,

    @SerialName("image_url_en")
    val imageUrlEn: String? = null,

    @SerialName("image_url_it")
    val imageUrlIt: String? = null,

    @SerialName("info_en")
    val infoEn: String? = null,

    @SerialName("info_it")
    val infoIt: String? = null
)
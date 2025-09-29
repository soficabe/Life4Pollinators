package com.example.life4pollinators.data.database.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Insect (
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String,

    @SerialName("group")
    val group: String,

    @SerialName("image_en")
    val imageEn: String?,

    @SerialName("image_it")
    val imageIt: String?,

    @SerialName("info_en")
    val infoEn: String?,

    @SerialName("info_it")
    val infoIt: String?,

    @SerialName("insect_image")
    val insectImage: String?
)
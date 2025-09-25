package com.example.life4pollinators.data.database.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlantPollinatorGroupRelation(
    @SerialName("plant_id")
    val plantId: String,

    @SerialName("insect_group_id")
    val insectGroupId: String
)
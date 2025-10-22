package com.example.life4pollinators.data.database.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Relazione many-to-many tra piante e gruppi di impollinatori.
 *
 * @property plantId ID della pianta.
 * @property insectGroupId ID del gruppo di impollinatori (es. "Bees", "Butterflies").
 *
 * Mappa quali gruppi di insetti impollinano una determinata pianta.
 */
@Serializable
data class PlantPollinatorGroupRelation(
    @SerialName("plant_id")
    val plantId: String,

    @SerialName("insect_group_id")
    val insectGroupId: String
)
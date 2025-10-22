package com.example.life4pollinators.data.database.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Entity che rappresenta una specie di pianta.
 *
 * @property id Identificativo univoco della pianta.
 * @property nameEn Nome della specie in inglese.
 * @property nameIt Nome della specie in italiano.
 * @property imageUrl URL dell'immagine della specie.
 * @property commonGenera Stringa che elenca generi comuni associati alla specie (separati da virgole).
 * @property invasiveFlower Campo opzionale che indica informazioni su eventuale specie invasive.
 * @property isDiverse Flag booleano che indica se la pianta presenta diverse specie eterogenee.
 * @property infoEn Testo informativo in inglese.
 * @property infoIt Testo informativo in italiano.
 */
@Serializable
data class Plant(
    @SerialName("id")
    val id: String,

    @SerialName("name_en")
    val nameEn: String,

    @SerialName("name_it")
    val nameIt: String,

    @SerialName("image_url")
    val imageUrl: String? = null,

    @SerialName("common_genera")
    val commonGenera: String,

    @SerialName("invasive_flower")
    val invasiveFlower: String?,

    @SerialName("is_diverse")
    val isDiverse: Boolean,

    @SerialName("info_en")
    val infoEn: String? = null,

    @SerialName("info_it")
    val infoIt: String? = null
)
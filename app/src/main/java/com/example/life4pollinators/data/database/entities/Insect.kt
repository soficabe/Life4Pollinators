package com.example.life4pollinators.data.database.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Rappresenta una specie di insetto.
 *
 * @property id Identificativo univoco dell'insetto.
 * @property name Nome della specie.
 * @property group Gruppo/categoria (es. "butterfly", "bee", ...).
 * @property imageEn URL infografica in inglese.
 * @property imageIt URL infografica in italiano.
 * @property infoEn Testo informativo in inglese.
 * @property infoIt Testo informativo in italiano.
 * @property insectImage Imaggine rappresentativa della specie.
 */
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
    val imageIt: String? = null,

    @SerialName("info_en")
    val infoEn: String? = null,

    @SerialName("info_it")
    val infoIt: String? = null,

    @SerialName("insect_image")
    val insectImage: String? = null
)
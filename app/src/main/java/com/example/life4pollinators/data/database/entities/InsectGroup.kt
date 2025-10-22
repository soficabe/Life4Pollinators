package com.example.life4pollinators.data.database.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Rappresenta un gruppo di insetti (es. Bees, Butterflies, Beetles).
 *
 * @property id Identificativo univoco del gruppo.
 * @property nameEn Nome del gruppo in inglese.
 * @property nameIt Nome del gruppo in italiano.
 * @property imageUrlEn URL infografica in inglese.
 * @property imageUrlIt URL infografica in italiano.
 * @property infoEn Testo informativo sul gruppo in inglese.
 * @property infoIt Testo informativo sul gruppo in italiano.
 * @property groupImageUrl URL immagine rappresentativa del gruppo.
 */
@Serializable
data class InsectGroup(
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
    val infoIt: String? = null,

    @SerialName("group_image_url")
    val groupImageUrl: String? = null
)
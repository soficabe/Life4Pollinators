package com.example.life4pollinators.data.database.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Entity che rappresenta un utente (tabella `user` del database Supabase).
 *
 * @property id Identificativo univoco dell'utente.
 * @property username Nome utente scelto dall'utente.
 * @property firstName Nome proprio dell'utente.
 * @property lastName Cognome dell'utente.
 * @property email Indirizzo email associato all'utente.
 * @property image URL dell'immagine profilo dell'utente (opzionale).
 */
@Serializable
data class User(
    @SerialName("id")
    val id: String,

    @SerialName("username")
    val username: String,

    @SerialName("first_name")
    val firstName: String,

    @SerialName("last_name")
    val lastName: String,

    @SerialName("email")
    val email: String,

    @SerialName("image")
    val image: String? = null
)
package com.example.life4pollinators.data.repositories

import android.util.Log
import com.example.life4pollinators.data.database.entities.User
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Risultati possibili dell'aggiornamento del profilo utente
 */
sealed interface UpdateUserProfileResult {
    data object Success : UpdateUserProfileResult
    sealed interface Error : UpdateUserProfileResult {
        data object UsernameAlreadyExists : Error
        data object NetworkError : Error
        data class UnknownError(val exception: Throwable) : Error
    }
}

/**
 * Repository per la gestione dei dati utente nel database:
 * recupero e aggiornamento dei dati utente su Supabase.
 */
class UserRepository(
    private val supabase: SupabaseClient
) {

    /**
     * Recupera i dati di un utente specifico dal database.
     *
     * @param id ID univoco dell'utente (UUID da Supabase Auth)
     * @return User entity se trovato, null altrimenti
     */
    suspend fun getUser(id: String): User? {
        return withContext(Dispatchers.IO) {
            try {
                supabase.from("user")
                    .select {
                        filter {
                            eq("id", id)
                        }
                    }.decodeSingleOrNull<User>()
            } catch (e: Exception) {
                Log.e("UserRepository", "Error fetching user: ${e.message}", e)
                null
            }
        }
    }

    /**
     * Controllo unicità username (escludendo l'utente stesso)
     *
     * @param username di cui controllare l'unicità
     * @param excludeUserId username attuale da escludere nel controllo
     * @return Boolean
     */
    private suspend fun isUsernameExists(
        username: String,
        excludeUserId: String? = null
    ): Boolean {
        return try {
            val res = supabase.from("user")
                .select {
                    filter { eq("username", username) }
                }.decodeList<User>()
            if (excludeUserId != null) {
                res.any { it.username == username && it.id != excludeUserId }
            } else {
                res.isNotEmpty()
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error checking username existence", e)
            false
        }
    }

    /**
     * Funzione per l'aggiornamento del profilo (username, firstName, lastName, image)
     */
    suspend fun updateUserProfile(
        userId: String,
        username: String,
        firstName: String,
        lastName: String,
        image: String?
    ): UpdateUserProfileResult {
        return withContext(Dispatchers.IO) {
            try {
                if (isUsernameExists(username, excludeUserId = userId)) {
                    return@withContext UpdateUserProfileResult.Error.UsernameAlreadyExists
                }

                val updateData = mutableMapOf(
                    "username" to username,
                    "first_name" to firstName,
                    "last_name" to lastName
                )
                if (image != null) {
                    updateData["image"] = image
                }

                supabase.from("user")
                    .update(updateData) {
                        filter { eq("id", userId) }
                    }
                UpdateUserProfileResult.Success
            } catch (e: Exception) {
                Log.e("UserRepository", "Error updating user profile: ${e.message}", e)
                when {
                    e.message?.contains("network", ignoreCase = true) == true ||
                            e.message?.contains("unable to resolve host", ignoreCase = true) == true ||
                            e.message?.contains("failed to connect", ignoreCase = true) == true -> {
                        UpdateUserProfileResult.Error.NetworkError
                    }
                    else -> UpdateUserProfileResult.Error.UnknownError(e)
                }
            }
        }
    }

    /**
     * Recupera tutti gli utenti dal database
     */
    suspend fun getAllUsers(): List<User> {
        return withContext(Dispatchers.IO) {
            try {
                supabase.from("user")
                    .select()
                    .decodeList<User>()
            } catch (e: Exception) {
                Log.e("UserRepository", "Error fetching all users: ${e.message}", e)
                emptyList()
            }
        }
    }
}
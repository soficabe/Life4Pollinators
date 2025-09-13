package com.example.life4pollinators.data.repositories

import android.util.Log
import com.example.life4pollinators.data.database.entities.User
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository per la gestione dei dati utente nel database.
 */
class UserRepository(
    private val supabase: SupabaseClient,
    private val auth: Auth
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

//    /**
//     * Aggiorna il profilo di un utente esistente nel database.
//     *
//     * @param userId ID dell'utente da aggiornare
//     * @param name nuovo nome (opzionale)
//     * @param surname nuovo cognome (opzionale)
//     * @param imageUrl nuovo URL dell'immagine profilo (opzionale)
//     * @return UpdateUserProfileResult indicante l'esito dell'operazione
//     */
//    suspend fun updateUserProfile(
//        userId: String,
//        name: String? = null,
//        surname: String? = null,
//        imageUrl: String? = null
//    ): UpdateUserProfileResult {
//        return withContext(Dispatchers.IO) {
//            try {
//                //costruzione campi da aggiornare
//                val updateData = mutableMapOf<String, String?>()
//                name?.let { updateData["name"] = it }
//                surname?.let { updateData["surname"] = it }
//                imageUrl?.let { updateData["image"] = it }
//
//                //esecuzione update solo se ci sono campi da modificare
//                if (updateData.isNotEmpty()) {
//                    supabase.from("User")
//                        .update(updateData) {
//                            filter {
//                                eq("id", userId)
//                            }
//                        }
//                }
//
//                UpdateUserProfileResult.Success
//            } catch (e: Exception) {
//                Log.e("UserRepository", "Error updating user profile: ${e.message}", e)
//                UpdateUserProfileResult.Error
//            }
//        }
//    }
}
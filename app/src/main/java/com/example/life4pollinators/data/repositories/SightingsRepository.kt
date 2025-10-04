package com.example.life4pollinators.data.repositories

import android.util.Log
import com.example.life4pollinators.data.database.entities.Sighting
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SightingsRepository(
    private val supabase: SupabaseClient
) {
    // Data class per ricevere solo il target_id dalla query
    @Serializable
    private data class TargetIdOnly(
        @SerialName("target_id")  // ← Questo mappava snake_case a camelCase
        val targetId: String       // ← Nome Kotlin in camelCase
    )

    suspend fun addSighting(
        userId: String,
        imageUrl: String,
        targetId: String,
        targetType: String,
        date: LocalDate,
        time: LocalTime,
        latitude: Double,
        longitude: Double
    ): Boolean {
        return try {
            val sighting = buildJsonObject {
                put("user_id", userId)
                put("image_url", imageUrl)
                put("target_id", targetId)
                put("target_type", targetType)
                put("date", date.toString()) // Formato: YYYY-MM-DD
                put("time", time.toString()) // Formato: HH:MM:SS
                put("latitude", latitude)
                put("longitude", longitude)
                put("is_auto_validated", false)
            }

            Log.d("SightingsRepository", "Tentativo inserimento sighting: $sighting")
            supabase.from("sighting").insert(sighting)
            Log.d("SightingsRepository", "Sighting inserito con successo")
            true
        } catch (e: Exception) {
            Log.e("SightingsRepository", "Errore durante l'inserimento del sighting", e)
            Log.e("SightingsRepository", "Messaggio errore: ${e.message}")
            false
        }
    }

    /**
     * Recupera tutti i sightings dell'utente
     */
    suspend fun getUserSightings(userId: String): List<Sighting> {
        return try {
            supabase.from("sighting")
                .select(columns = Columns.ALL) {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<Sighting>()
        } catch (e: Exception) {
            Log.e("SightingsRepository", "Errore nel recupero sightings utente", e)
            emptyList()
        }
    }

    /**
     * Ottiene i target_id univoci avvistati dall'utente per tipo
     * (per sapere quali specie ha già avvistato)
     */
    suspend fun getUserSightedSpecies(userId: String, targetType: String): Set<String> {
        return try {
            // Seleziona solo target_id e decodifica con una data class dedicata
            val results = supabase.from("sighting")
                .select(columns = Columns.list("target_id")) {
                    filter {
                        eq("user_id", userId)
                        eq("target_type", targetType)
                    }
                }
                .decodeList<TargetIdOnly>()

            Log.d("SightingsRepository", "Sighted $targetType per user $userId: ${results.map { it.targetId }}")

            results.map { it.targetId }.toSet()
        } catch (e: Exception) {
            Log.e("SightingsRepository", "Errore nel recupero specie avvistate", e)
            Log.e("SightingsRepository", "Tipo: $targetType, UserId: $userId", e)
            emptySet()
        }
    }
}
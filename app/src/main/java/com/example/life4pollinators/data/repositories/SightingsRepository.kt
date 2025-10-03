package com.example.life4pollinators.data.repositories

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SightingsRepository(
    private val supabase: SupabaseClient
) {
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
}
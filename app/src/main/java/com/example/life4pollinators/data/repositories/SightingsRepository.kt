package com.example.life4pollinators.data.repositories

import android.util.Log
import com.example.life4pollinators.data.database.entities.Sighting
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SightingsRepository(
    private val supabase: SupabaseClient
) {

    private data class UserScore(
        val userId: String,
        val uniqueSpecies: Int,
        val totalSightings: Int,
        val score: Int
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
                put("date", date.toString())
                put("time", time.toString())
                put("latitude", latitude)
                put("longitude", longitude)
            }
            supabase.from("sighting").insert(sighting)
            true
        } catch (e: Exception) {
            Log.e("SightingsRepository", "Errore durante l'inserimento del sighting", e)
            Log.e("SightingsRepository", "Messaggio errore: ${e.message}")
            false
        }
    }

    suspend fun getUserSightedSpecies(
        userId: String,
        targetType: String
    ): Set<String> {
        return try {
            val results = supabase.from("sighting")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("target_type", targetType)
                    }
                }
                .decodeList<Sighting>()

            results.map { it.targetId }.toSet()
        } catch (e: Exception) {
            Log.e("SightingsRepository", "Errore nel recupero specie avvistate", e)
            Log.e("SightingsRepository", "Tipo: $targetType, UserId: $userId", e)
            emptySet()
        }
    }

    /**
     * Conta il numero totale di avvistamenti di un utente
     */
    suspend fun getUserTotalSightingsCount(userId: String): Int {
        return try {
            val sightings = supabase.from("sighting")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<Sighting>()

            sightings.size
        } catch (e: Exception) {
            Log.e("SightingsRepository", "Errore conteggio sightings utente", e)
            0
        }
    }

    /**
     * Calcola il ranking globale basato su specie uniche e avvistamenti totali
     * Formula: (specie uniche * 10) + avvistamenti totali
     * @return Pair(rank, score) dove rank è la posizione e score è il punteggio
     */
    suspend fun getGlobalRanking(userId: String): Pair<Int, Int> {
        return try {
            // Recupera tutti gli avvistamenti di tutti gli utenti
            val allSightings = supabase.from("sighting")
                .select()
                .decodeList<Sighting>()

            // Raggruppa per utente e calcola punteggi
            val userScores = allSightings
                .groupBy { it.userId }
                .map { (uid, sightings) ->
                    val uniqueSpecies = sightings
                        .map { it.targetId }
                        .toSet()
                        .size
                    val totalSightings = sightings.size
                    val score = (uniqueSpecies * 10) + totalSightings

                    UserScore(uid, uniqueSpecies, totalSightings, score)
                }
                .sortedByDescending { it.score }

            // Trova la posizione dell'utente
            val userScore = userScores.find { it.userId == userId }

            // Cioè se l'utente non ha fatto nessun avvistamento
            if (userScore == null) {
                val rank = if (userScores.isEmpty()) 1 else userScores.size + 1
                return Pair(rank, 0)
            }

            // Conta quanti utenti hanno un punteggio STRETTAMENTE MAGGIORE
            val betterUsers = userScores.filter { it.score > userScore.score }
            val rank = betterUsers.size + 1

            Pair(rank, userScore.score)

        } catch (e: Exception) {
            Log.e("SightingsRepository", "ERRORE nel calcolo ranking globale", e)
            Log.e("SightingsRepository", "Stack trace:", e)
            Pair(-1, 0)
        }
    }
}
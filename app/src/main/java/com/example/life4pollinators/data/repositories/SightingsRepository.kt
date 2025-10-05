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
    @Serializable
    private data class TargetIdOnly(
        @SerialName("target_id")
        val targetId: String
    )

    @Serializable
    private data class UserIdOnly(
        @SerialName("user_id")
        val userId: String
    )

    @Serializable
    private data class SightingCount(
        @SerialName("user_id")
        val userId: String,
        @SerialName("count")
        val count: Int
    )

    @Serializable
    private data class IdOnly(
        @SerialName("id")
        val id: String
    )

    @Serializable
    private data class SightingForRanking(
        @SerialName("user_id")
        val userId: String,
        @SerialName("target_id")
        val targetId: String,
        @SerialName("target_type")
        val targetType: String
    )

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

    suspend fun getUserSightedSpecies(userId: String, targetType: String): Set<String> {
        return try {
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

    /**
     * Conta il numero totale di avvistamenti di un utente
     */
    suspend fun getUserTotalSightingsCount(userId: String): Int {
        return try {
            val sightings = supabase.from("sighting")
                .select(columns = Columns.list("id")) {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<IdOnly>()

            sightings.size
        } catch (e: Exception) {
            Log.e("SightingsRepository", "Errore conteggio sightings utente", e)
            0
        }
    }

    /**
     * Conta le specie uniche avvistate dall'utente (piante + insetti)
     */
    suspend fun getUserUniqueSpeciesCount(userId: String): Int {
        return try {
            val plants = getUserSightedSpecies(userId, "plant")
            val insects = getUserSightedSpecies(userId, "insect")
            plants.size + insects.size
        } catch (e: Exception) {
            Log.e("SightingsRepository", "Errore conteggio specie uniche", e)
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
            Log.d("SightingsRepository", "========== INIZIO CALCOLO RANKING GLOBALE ==========")
            Log.d("SightingsRepository", "Calcolo ranking globale per userId: $userId")

            // Recupera tutti gli avvistamenti di tutti gli utenti
            val allSightings = supabase.from("sighting")
                .select(columns = Columns.list("user_id", "target_id", "target_type"))
                .decodeList<SightingForRanking>()

            Log.d("SightingsRepository", "Trovati ${allSightings.size} avvistamenti totali nel database")

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

            Log.d("SightingsRepository", "Numero utenti con avvistamenti: ${userScores.size}")
            Log.d("SightingsRepository", "TUTTI gli utenti con score:")
            userScores.forEach {
                Log.d("SightingsRepository", "  User ${it.userId.take(8)}: ${it.score} pts (${it.uniqueSpecies} species, ${it.totalSightings} sightings)")
            }

            // Trova la posizione dell'utente
            val userScore = userScores.find { it.userId == userId }

            if (userScore == null) {
                val rank = if (userScores.isEmpty()) 1 else userScores.size + 1
                Log.d("SightingsRepository", "Utente senza avvistamenti, rank assegnato: $rank")
                Log.d("SightingsRepository", "========== FINE CALCOLO RANKING GLOBALE ==========")
                return Pair(rank, 0)
            }

            Log.d("SightingsRepository", "Score utente corrente: ${userScore.score}")

            // Conta quanti utenti hanno un punteggio STRETTAMENTE MAGGIORE
            val betterUsers = userScores.filter { it.score > userScore.score }
            Log.d("SightingsRepository", "Utenti con score maggiore di ${userScore.score}:")
            betterUsers.forEach {
                Log.d("SightingsRepository", "  User ${it.userId.take(8)}: ${it.score} pts")
            }

            val rank = betterUsers.size + 1

            Log.d("SightingsRepository", "Utente trovato - Specie uniche: ${userScore.uniqueSpecies}, " +
                    "Avvistamenti: ${userScore.totalSightings}, Score: ${userScore.score}")
            Log.d("SightingsRepository", "Utenti con score maggiore: ${betterUsers.size}")
            Log.d("SightingsRepository", "Rank finale: $rank di ${userScores.size}")
            Log.d("SightingsRepository", "========== FINE CALCOLO RANKING GLOBALE ==========")

            Pair(rank, userScore.score)

        } catch (e: Exception) {
            Log.e("SightingsRepository", "ERRORE nel calcolo ranking globale", e)
            Log.e("SightingsRepository", "Stack trace:", e)
            Pair(-1, 0)
        }
    }
}
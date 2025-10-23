package com.example.life4pollinators.data.repositories

import android.util.Log
import com.example.life4pollinators.data.database.entities.Sighting
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Data class per calcolo punteggi utenti.
 *
 * @property userId ID univoco dell'utente
 * @property uniqueSpecies Numero di specie (piante/insetti) uniche avvistate
 * @property totalSightings Numero totale di avvistamenti
 * @property score Punteggio calcolato
 */
data class UserScore(
    val userId: String,
    val uniqueSpecies: Int,
    val totalSightings: Int,
    val score: Int
)

/**
 * Data class per il ranking di un utente con la sua posizione.
 *
 * @property userScore Punteggio dell'utente
 * @property position Posizione in classifica (1-based)
 */
data class UserRanking(
    val userScore: UserScore,
    val position: Int
)

/**
 * Repository per la gestione degli avvistamenti nel database.
 *
 * Responsabilità:
 * - Inserimento nuovi avvistamenti
 * - Recupero specie di piante e di insetti avvistate da un utente
 * - Conteggio avvistamenti totali
 * - Calcolo ranking globale utenti
 *
 * @property supabase Client Supabase per accesso al database
 */
class SightingsRepository(
    private val supabase: SupabaseClient
) {

    /**
     * Calcola il punteggio utente.
     *
     * Formula:
     * score = (uniqueSpecies * SPECIES_WEIGHT) + (totalSightings * SIGHTING_WEIGHT)
     *
     * Valori attuali:
     * - SPECIES_WEIGHT = 10 (incentiva scoperta)
     * - SIGHTING_WEIGHT = 1 (premia attività)
     */
    companion object {
        private const val SPECIES_WEIGHT = 10 // incentiva scoperta
        private const val SIGHTING_WEIGHT = 1 // premia attività

        fun calculateScore(uniqueSpecies: Int, totalSightings: Int): Int {
            return (uniqueSpecies * SPECIES_WEIGHT) + (totalSightings * SIGHTING_WEIGHT)
        }
    }

    /**
     * Aggiunge un nuovo avvistamento al database.
     */
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

    /**
     * Recupera l'insieme delle specie (piante/insetti) avvistate da un utente.
     */
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
     * Conta il numero totale di avvistamenti di un utente.
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
     * Calcola i punteggi di tutti gli utenti ordinati per punteggio decrescente.
     *
     * @return Lista di UserScore ordinata per punteggio decrescente
     */
    private suspend fun calculateAllUserScores(): List<UserScore> {
        return try {
            val allSightings = supabase.from("sighting")
                .select()
                .decodeList<Sighting>()

            allSightings
                .groupBy { it.userId }
                .map { (uid, sightings) ->
                    val uniqueSpecies = sightings.map { it.targetId }.toSet().size
                    val totalSightings = sightings.size
                    val score = calculateScore(uniqueSpecies, totalSightings)
                    UserScore(uid, uniqueSpecies, totalSightings, score)
                }
                .sortedByDescending { it.score }
        } catch (e: Exception) {
            Log.e("SightingsRepository", "Errore calcolo punteggi utenti", e)
            emptyList()
        }
    }

    /**
     * Calcola il ranking completo di tutti gli utenti con avvistamenti.
     *
     * La posizione si basa sul numero di punteggi DISTINTI, gestendo correttamente i pari merito.
     *
     * Esempio: A=11pts, B=11pts, C=5pts, D=5pts
     * Risultato: A=1°, B=1°, C=2°, D=2°
     *
     * @return Map da userId a UserRanking
     */
    suspend fun calculateAllUserRankings(): Map<String, UserRanking> {
        return try {
            val userScores = calculateAllUserScores()

            if (userScores.isEmpty()) {
                return emptyMap()
            }

            val rankingsMap = mutableMapOf<String, UserRanking>()
            val scoreGroups = userScores.groupBy { it.score }
            val sortedScores = scoreGroups.keys.sortedDescending()

            var currentPosition = 1

            sortedScores.forEach { score ->
                val usersWithScore = scoreGroups[score] ?: emptyList()

                usersWithScore.forEach { userScore ->
                    rankingsMap[userScore.userId] = UserRanking(userScore, currentPosition)
                }

                // Incrementa la posizione di 1 per ogni punteggio distinto
                currentPosition++
            }

            rankingsMap
        } catch (e: Exception) {
            Log.e("SightingsRepository", "Errore calcolo ranking utenti", e)
            emptyMap()
        }
    }

    /**
     * Calcola la posizione dopo l'ultimo utente con avvistamenti.
     * Utilizzata per gli utenti senza avvistamenti.
     *
     * @return Posizione per utenti senza avvistamenti
     */
    suspend fun getPositionForUsersWithoutSightings(): Int {
        return try {
            val userScores = calculateAllUserScores()

            if (userScores.isEmpty()) {
                return 1
            }

            val distinctScoresCount = userScores.map { it.score }.distinct().size
            distinctScoresCount + 1
        } catch (e: Exception) {
            Log.e("SightingsRepository", "Errore calcolo posizione utenti senza sightings", e)
            1
        }
    }

    /**
     * Calcola il ranking globale dell'utente tra tutti gli utenti.
     *
     * Processo:
     * 1. Recupera tutti i ranking tramite calculateAllUserRankings()
     * 2. Cerca la posizione dell'utente specificato
     * 3. Se l'utente non ha avvistamenti, usa getPositionForUsersWithoutSightings()
     *
     * Gestione casi edge:
     * - Utente senza avvistamenti: posizione = (numero di punteggi distinti) + 1, score = 0
     * - Utenti con stesso punteggio: stesso rank (pari merito)
     * - Nessun avvistamento nel database: rank = 1, score = 0
     *
     * Esempio: A=11pts, B=11pts, C=0pts, D=0pts
     * Risultato: A=1°, B=1°, C=2°, D=2°
     *
     * @param userId ID dell'utente di cui calcolare il ranking
     * @return Pair(rank, score) dove rank è la posizione (1-based) e score il punteggio
     *         Ritorna (-1, 0) in caso di errore
     */
    suspend fun getGlobalRanking(userId: String): Pair<Int, Int> {
        return try {
            val allRankings = calculateAllUserRankings()

            // Caso: nessun avvistamento nel database
            if (allRankings.isEmpty()) {
                return Pair(1, 0)
            }

            // Cerca il ranking dell'utente
            val userRanking = allRankings[userId]

            // Caso: utente senza avvistamenti
            if (userRanking == null) {
                val position = getPositionForUsersWithoutSightings()
                return Pair(position, 0)
            }

            Pair(userRanking.position, userRanking.userScore.score)

        } catch (e: Exception) {
            Log.e("SightingsRepository", "ERRORE nel calcolo ranking globale", e)
            Pair(-1, 0)
        }
    }
}
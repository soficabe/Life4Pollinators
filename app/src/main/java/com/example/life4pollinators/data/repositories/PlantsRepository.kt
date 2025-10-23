package com.example.life4pollinators.data.repositories

import android.util.Log
import com.example.life4pollinators.data.database.entities.InsectGroup
import com.example.life4pollinators.data.database.entities.Plant
import com.example.life4pollinators.data.database.entities.PlantPollinatorGroupRelation
import com.example.life4pollinators.data.database.entities.PlantsGeneralInfo
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import java.util.Locale

/**
 * Repository per la gestione dei dati delle piante nel database Supabase.
 *
 * Utilizza Supabase Postgrest per le query al database.
 *
 * @param supabase Client Supabase per accesso al database
 */
class PlantsRepository(
    supabase: SupabaseClient
) {
    // Riferimenti alle tabelle del database
    private val plantsTable = supabase.from("plant")
    private val plantsGeneralInfo = supabase.from("plants_general_info")
    private val relationsTable = supabase.from("plant_pollinator_group_relation")
    private val insectGroupTable = supabase.from("insect_group")

    /**
     * Conta il numero totale di piante nel database.
     *
     * Utilizzato per statistiche o validazioni.
     *
     * @return Numero di piante presenti, 0 in caso di errore
     */
    suspend fun getTotalPlantsCount(): Int {
        return try {
            plantsTable.select().decodeList<Plant>().size
        } catch (e: Exception) {
            Log.e("PlantsRepository", "Error counting plants", e)
            0
        }
    }

    /**
     * Recupera tutte le piante dal database.
     *
     * Le piante vengono automaticamente ordinate alfabeticamente
     * in base alla lingua del dispositivo (nameIt per italiano, nameEn per inglese).
     *
     * @return Lista di piante ordinate alfabeticamente, lista vuota in caso di errore
     */
    suspend fun getPlants(): List<Plant> {
        return try {
            plantsTable.select().decodeList<Plant>().sortedBy {
                // Ordinamento dinamico in base alla lingua del dispositivo
                if (Locale.getDefault().language == "it") it.nameIt else it.nameEn
            }
        } catch (e: Exception) {
            Log.e("PlantsRepository", "Error fetching plants", e)
            emptyList()
        }
    }

    /**
     * Recupera le informazioni generali sulle piante.
     *
     * Nel database per ora  esiste un solo record di tipo PlantsGeneralInfo,
     * per questo viene restituito il primo elemento.
     *
     * @return Entity PlantsGeneralInfo se presente, null in caso di errore o assenza dati
     */
    suspend fun getGeneralInfo(): PlantsGeneralInfo? {
        return try {
            plantsGeneralInfo.select().decodeList<PlantsGeneralInfo>().firstOrNull()
        } catch (e: Exception) {
            Log.e("PlantsRepository", "Error fetching plant general info", e)
            null
        }
    }

    /**
     * Recupera una pianta specifica tramite il suo ID univoco.
     *
     * Utilizzato nelle schermate di dettaglio per visualizzare
     * tutte le informazioni di una singola pianta.
     *
     * @param plantId ID univoco della pianta (UUID da database)
     * @return Entity Plant se trovata, null in caso di errore o pianta non esistente
     */
    suspend fun getPlantById(plantId: String): Plant? {
        return try {
            plantsTable.select {
                filter { Plant::id eq plantId }
            }.decodeList<Plant>().firstOrNull()
        } catch (e: Exception) {
            Log.e("PlantsRepository", "Error fetching plant by ID: $plantId", e)
            null
        }
    }

    /**
     * Recupera i gruppi di impollinatori associati a una specifica pianta,
     * cioè gli impollinatori che impollinano quella pianta.
     *
     * @param plantId ID univoco della pianta
     * @return Lista di nomi localizzati dei gruppi impollinatori, lista vuota in caso di errore
     */
    suspend fun getPollinatorGroupsForPlant(plantId: String): List<String> {
        return try {
            // Step 1: Recupera le relazioni pianta-gruppo
            val relations = relationsTable
                .select {
                    filter { PlantPollinatorGroupRelation::plantId eq plantId }
                }
                .decodeList<PlantPollinatorGroupRelation>()

            // Estrai gli ID dei gruppi dalle relazioni
            val groupIds = relations.map { it.insectGroupId }

            // Se non ci sono relazioni, ritorna lista vuota
            if (groupIds.isEmpty()) return emptyList()

            // Step 2: Recupera i dati dei gruppi tramite gli ID
            val language = Locale.getDefault().language
            val groups = insectGroupTable.select {
                filter { "id" to groupIds } // Filtra per lista di ID
            }.decodeList<InsectGroup>()

            // Step 3: Mappa i gruppi ai loro nomi localizzati
            groups.map { if (language == "it") it.nameIt else it.nameEn }
        } catch (e: Exception) {
            Log.e("PlantsRepository", "Error fetching pollinator groups for plant: $plantId", e)
            emptyList()
        }
    }
}
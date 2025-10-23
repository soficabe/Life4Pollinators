package com.example.life4pollinators.data.repositories

import android.util.Log
import com.example.life4pollinators.data.database.entities.Insect
import com.example.life4pollinators.data.database.entities.InsectGroup
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import java.util.Locale

/**
 * Repository per la gestione degli insetti e dei loro gruppi nel database.
 *
 * Gestisce:
 * - Lista gruppi di insetti (Api, Farfalle, ecc.)
 * - Lista insetti per gruppo
 * - Dettagli singolo insetto
 * - Conteggio totale insetti
 *
 * @param supabase Client per accesso al database Supabase
 */
class InsectsRepository(
    supabase: SupabaseClient
) {
    // Riferimenti alle tabelle del database
    private val insectsTable = supabase.from("insect")
    private val insectGroupsTable = supabase.from("insect_group")

    /**
     * Conta il numero totale di insetti nel database.
     */
    suspend fun getTotalInsectsCount(): Int {
        return try {
            insectsTable.select().decodeList<Insect>().size
        } catch (e: Exception) {
            Log.e("InsectsRepository", "Error counting insects", e)
            0
        }
    }

    /**
     * Recupera tutti i gruppi di insetti, ordinati alfabeticamente in base alla lingua.
     */
    suspend fun getInsectGroups(): List<InsectGroup> {
        return try {
            insectGroupsTable.select().decodeList<InsectGroup>().sortedBy {
                if (Locale.getDefault().language == "it") it.nameIt else it.nameEn
            }
        } catch (e: Exception) {
            Log.e("InsectsRepository", "Error fetching insect groups", e)
            emptyList()
        }
    }

    /**
     * Recupera tutti gli insetti appartenenti a un gruppo specifico.
     *
     * @param groupId ID del gruppo di insetti
     * @return Lista di insetti del gruppo, ordinati per nome
     */
    suspend fun getInsectsByGroup(groupId: String): List<Insect> {
        return try {
            insectsTable.select {
                filter { Insect::group eq groupId }
            }.decodeList<Insect>().sortedBy { it.name }
        } catch (e: Exception) {
            Log.e("InsectsRepository", "Error fetching insects by group: $groupId", e)
            emptyList()
        }
    }

    /**
     * Recupera un insetto specifico tramite il suo ID.
     *
     * @param insectId ID univoco dell'insetto
     * @return Entity Insect se trovato
     */
    suspend fun getInsectById(insectId: String): Insect? {
        return try {
            insectsTable.select {
                filter { Insect::id eq insectId }
            }.decodeList<Insect>().firstOrNull()
        } catch (e: Exception) {
            Log.e("InsectsRepository", "Error fetching insect by ID: $insectId", e)
            null
        }
    }

    /**
     * Recupera le informazioni di un gruppo di insetti tramite il suo ID.
     *
     * @param groupId ID univoco del gruppo di insetti
     * @return Entity InsectGroup se trovato
     */
    suspend fun getInsectGroupById(groupId: String): InsectGroup? {
        return try {
            insectGroupsTable.select {
                filter { InsectGroup::id eq groupId }
            }.decodeList<InsectGroup>().firstOrNull()
        } catch (e: Exception) {
            Log.e("InsectsRepository", "Error fetching insect group by ID: $groupId", e)
            null
        }
    }
}
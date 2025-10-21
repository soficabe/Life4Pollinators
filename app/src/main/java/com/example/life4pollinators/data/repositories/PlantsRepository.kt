package com.example.life4pollinators.data.repositories

import android.util.Log
import com.example.life4pollinators.data.database.entities.InsectGroup
import com.example.life4pollinators.data.database.entities.Plant
import com.example.life4pollinators.data.database.entities.PlantPollinatorGroupRelation
import com.example.life4pollinators.data.database.entities.PlantsGeneralInfo
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import java.util.Locale

class PlantsRepository (
    supabase: SupabaseClient
) {
    private val plantsTable = supabase.from("plant")
    private val plantsGeneralInfo = supabase.from("plants_general_info")
    private val relationsTable = supabase.from("plant_pollinator_group_relation")
    private val insectGroupTable = supabase.from("insect_group")

    suspend fun getTotalPlantsCount(): Int {
        return try {
            plantsTable.select().decodeList<Plant>().size
        } catch (e: Exception) {
            Log.e("PlantsRepository", "Error counting plants", e)
            0
        }
    }

    suspend fun getPlants(): List<Plant> {
        return try {
            plantsTable.select().decodeList<Plant>().sortedBy {
                if (Locale.getDefault().language == "it") it.nameIt else it.nameEn
            }
        } catch (e: Exception) {
            Log.e("PlantsRepository", "Error fetching plants", e)
            emptyList()
        }
    }

    suspend fun getGeneralInfo(): PlantsGeneralInfo? {
        return try {
            plantsGeneralInfo.select().decodeList<PlantsGeneralInfo>().firstOrNull()
        } catch (e: Exception) {
            Log.e("PlantsRepository", "Error fetching plant general info", e)
            null
        }
    }

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

    suspend fun getPollinatorGroupsForPlant(plantId: String): List<String> {
        return try {
            val relations = relationsTable
                .select {
                    filter { PlantPollinatorGroupRelation::plantId eq plantId }
                }
                .decodeList<PlantPollinatorGroupRelation>()

            val groupIds = relations.map { it.insectGroupId }
            if (groupIds.isEmpty()) return emptyList()

            val language = Locale.getDefault().language
            val groups = insectGroupTable.select {
                filter { "id" to groupIds }
            }.decodeList<InsectGroup>()

            groups.map { if (language == "it") it.nameIt else it.nameEn }
        } catch (e: Exception) {
            Log.e("PlantsRepository", "Error fetching pollinator groups for plant: $plantId", e)
            emptyList()
        }
    }
}
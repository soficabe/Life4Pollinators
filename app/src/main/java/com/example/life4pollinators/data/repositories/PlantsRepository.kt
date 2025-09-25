package com.example.life4pollinators.data.repositories

import android.util.Log
import com.example.life4pollinators.data.database.entities.Plant
import com.example.life4pollinators.data.database.entities.PlantsGeneralInfo
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import java.util.Locale

class PlantsRepository (
    supabase: SupabaseClient
) {
    private val plantsTable = supabase.from("plant")
    private val plantsGeneralInfo = supabase.from("plants_general_info")

    suspend fun getPlants(): List<Plant> {
        return try {
            plantsTable.select().decodeList<Plant>().sortedBy { if (Locale.getDefault().language == "it") it.nameIt else it.nameEn }
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
}
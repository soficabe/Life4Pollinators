package com.example.life4pollinators.data.repositories

import android.util.Log
import com.example.life4pollinators.data.database.entities.InsectGroup
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import java.util.Locale

class InsectsRepository (
    supabase: SupabaseClient
) {
    private val insectsTable = supabase.from("insect")
    private val insectGroupsTable = supabase.from("insect_group")

    suspend fun getInsectGroups(): List<InsectGroup> {
        return try {
            insectGroupsTable.select().decodeList<InsectGroup>().sortedBy { if (Locale.getDefault().language == "it") it.nameIt else it.nameEn }
        } catch (e: Exception) {
            Log.e("InsectsRepository", "Error fetching insect groups", e)
            emptyList()
        }
    }
}
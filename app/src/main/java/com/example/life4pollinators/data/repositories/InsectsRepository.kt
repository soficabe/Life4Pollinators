package com.example.life4pollinators.data.repositories

import android.util.Log
import com.example.life4pollinators.data.database.entities.Insect
import com.example.life4pollinators.data.database.entities.InsectGroup
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import java.util.Locale

class InsectsRepository (
    supabase: SupabaseClient
) {
    private val insectsTable = supabase.from("insect")
    private val insectGroupsTable = supabase.from("insect_group")

    suspend fun getTotalInsectsCount(): Int {
        return try {
            insectsTable.select().decodeList<Insect>().size
        } catch (e: Exception) {
            Log.e("InsectsRepository", "Error counting insects", e)
            0
        }
    }

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
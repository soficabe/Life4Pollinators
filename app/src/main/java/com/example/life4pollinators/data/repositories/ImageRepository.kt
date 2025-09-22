package com.example.life4pollinators.data.repositories

import android.content.Context
import android.net.Uri
import android.util.Log
import io.github.jan.supabase.storage.Storage
import java.util.UUID

/**
 * Repository per la gestione dell’upload delle immagini su Supabase Storage
 */
class ImageRepository(private val storage: Storage) {
    private suspend fun uploadImage(
        userId: String,
        uri: Uri,
        context: Context,
        bucket: String
    ): String? {
        return try {
            val extension = ".jpg"
            val fileName = "${userId}_${UUID.randomUUID()}$extension"
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bytes = inputStream.readBytes()
            if (bytes.isEmpty()) return null
            storage.from(bucket).upload(fileName, bytes) { upsert = true }
            storage.from(bucket).publicUrl(fileName)
        } catch (e: Exception) {
            Log.e("ImageRepository", "Exception during upload: ${e.message}", e)
            null
        }
    }

    /**
     * Funzione per l'upload dell'immagine profilo
     */
    suspend fun uploadProfileImage(
        userId: String,
        uri: Uri,
        context: Context
    ): String? =
        uploadImage(userId, uri, context, bucket = "profile-images")

    // In futuro:
    // suspend fun uploadInsectPhoto(...) = uploadImage(..., bucket = "insect-photos")
    // suspend fun uploadPlantPhoto(...) = uploadImage(..., bucket = "plant-photos")
    // suspend fun uploadSightingPhoto(...) = uploadImage(..., bucket = "sighting-photos")
}
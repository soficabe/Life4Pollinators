package com.example.life4pollinators.data.repositories

import android.content.Context
import android.net.Uri
import android.util.Log
import io.github.jan.supabase.storage.Storage

/**
 * Repository per la gestione dell’upload delle immagini su Supabase Storage.
 */
class ImageRepository(private val storage: Storage) {
    private suspend fun uploadImage(
        uri: Uri,
        context: Context,
        bucket: String,
        fileName: String
    ): String? {
        return try {
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
     * Funzione per l'upload dell'immagine profilo.
     * Sovrascrive sempre la foto profilo dell'utente (nessuna foto vecchia rimane nello storage).
     */
    suspend fun uploadProfileImage(
        userId: String,
        uri: Uri,
        context: Context
    ): String? =
        uploadImage(
            uri,
            context,
            bucket = "profile-images",
            fileName = "${userId}/profile_image.jpg"
        )

    // In futuro:
    // suspend fun uploadInsectPhoto(...) = uploadImage(..., bucket = "insect-photos", fileName = ...)
    // suspend fun uploadPlantPhoto(...) = uploadImage(..., bucket = "plant-photos", fileName = ...)
    // suspend fun uploadSightingPhoto(...) = uploadImage(..., bucket = "sighting-photos", fileName = ...)
}
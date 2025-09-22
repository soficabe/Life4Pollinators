package com.example.life4pollinators.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.File
import java.io.OutputStream

/**
 * Copia una foto nella galleria (MediaStore) del dispositivo e restituisce l'Uri pubblico.
 * @param context Context
 * @param sourceFile File temporaneo da copiare
 * @param displayName Nome desiderato del file (es: "profile_IMG.jpg")
 * @return Uri dell'immagine nella galleria, oppure null in caso di errore
 */
fun saveImageToGallery(context: Context, sourceFile: File, displayName: String = "profile_IMG.jpg"): Uri? {
    val resolver = context.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Life4Pollinators")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.IS_PENDING, 1) // Indica che il file è in scrittura (Android 10+)
        }
    }

    // Scegli la collection giusta a seconda della versione di Android
    val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } else {
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    // Inserisci il record "vuoto" nel MediaStore per ottenere l'URI dove scrivere la foto
    val imageUri = resolver.insert(collection, contentValues) ?: return null

    try {
        // Copia i dati dal file temporaneo nel MediaStore (cioè nella galleria)
        val outputStream: OutputStream? = resolver.openOutputStream(imageUri)
        val inputStream = sourceFile.inputStream()
        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output!!)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Rendi il file "non più in scrittura", quindi visibile alla galleria (solo Android 10+)
            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(imageUri, contentValues, null, null)
        }
        return imageUri // Restituisci l'Uri pubblico dell'immagine salvata in galleria
    } catch (e: Exception) {
        // In caso di errore cancella il record appena creato
        resolver.delete(imageUri, null, null)
        return null
    }
}
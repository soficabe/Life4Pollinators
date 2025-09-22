package com.example.life4pollinators.utils

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

/**
 * Restituisce una lambda che lancia il selettore immagini di sistema.
 * (Utility composable per aprire il selettore di immagini e restituire l’Uri dell’immagine scelta.)
 *
 * @param onImageSelected Callback chiamato con l'Uri dell'immagine selezionata.
 */
@Composable
fun rememberGalleryLauncher(
    onImageSelected: (Uri) -> Unit
): () -> Unit {
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) onImageSelected(uri)
    }
    return { galleryLauncher.launch("image/*") }
}
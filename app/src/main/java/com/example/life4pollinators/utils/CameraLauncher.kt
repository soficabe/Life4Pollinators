package com.example.life4pollinators.utils

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.life4pollinators.R

/**
 * Restituisce una lambda che lancia la fotocamera di sistema,
 * gestisce la ricezione della foto e invoca il callback.
 *
 * La foto viene salvata in galleria con nome parlante e il file temporaneo viene eliminato.
 * Il callback riceve l'Uri pubblico della foto in galleria.
 *
 * @param onPhotoReady Callback chiamato con l'Uri della foto salvata se tutto va a buon fine.
 * @param onError Callback opzionale chiamato con una risorsa di errore se qualcosa va storto.
 */
@Composable
fun rememberCameraLauncher(
    onPhotoReady: (Uri) -> Unit,
    onError: ((Int) -> Unit)? = null
): () -> Unit {
    val context = LocalContext.current
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var photoFile by remember { mutableStateOf<File?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher per la fotocamera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        photoUri?.let { uri ->
            if (success) pendingCameraUri = uri
            else onError?.invoke(R.string.photo_not_saved)
        }
    }

    // Effetto per confermare che la foto sia effettivamente salvata sul dispositivo
    LaunchedEffect(pendingCameraUri) {
        pendingCameraUri?.let { uri ->
            var retries = 5
            var found = false
            while (retries > 0 && !found) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    if (inputStream != null) {
                        inputStream.close()
                        found = true
                    } else {
                        delay(200)
                        retries--
                    }
                } catch (e: Exception) {
                    delay(200)
                    retries--
                }
            }
            if (found && photoFile != null) {
                // Nome parlante per la foto in galleria: Life4Pollinators_yyyyMMdd_HHmmss.jpg
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val galleryFileName = "Life4Pollinators_${timestamp}.jpg"
                // Salva la foto nella galleria
                val galleryUri = saveImageToGallery(context, photoFile!!, displayName = galleryFileName)
                // Elimina il file temporaneo dalla cache
                photoFile?.delete()
                if (galleryUri != null) {
                    onPhotoReady(galleryUri)
                } else {
                    // fallback: restituisci comunque l'uri temporaneo
                    onPhotoReady(uri)
                }
            } else {
                onError?.invoke(R.string.photo_error)
            }
            pendingCameraUri = null
        }
    }

    // Funzione che crea il file temporaneo e lancia la fotocamera
    val launchCamera = {
        val imageFile = File.createTempFile("tmp_image", ".jpg", context.externalCacheDir)
        val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", imageFile)
        photoUri = uri
        photoFile = imageFile // PATCH: salviamo il file temporaneo per copiarlo nella galleria dopo
        cameraLauncher.launch(uri)
    }
    return launchCamera
}
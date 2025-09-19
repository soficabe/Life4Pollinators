package com.example.life4pollinators.utils

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun rememberCameraLauncher(
    onPhotoReady: (Uri) -> Unit,
    onError: ((String) -> Unit)? = null
): () -> Unit {
    val context = LocalContext.current
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        photoUri?.let { uri ->
            if (success) pendingCameraUri = uri
            else onError?.invoke("Foto non salvata")
        }
    }

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
            if (found) onPhotoReady(uri)
            else onError?.invoke("Errore nella foto")
            pendingCameraUri = null
        }
    }

    val launchCamera = {
        val imageFile = File.createTempFile("tmp_image", ".jpg", context.externalCacheDir)
        val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", imageFile)
        photoUri = uri
        cameraLauncher.launch(uri)
    }
    return launchCamera
}
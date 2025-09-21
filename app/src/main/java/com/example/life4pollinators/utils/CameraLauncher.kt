package com.example.life4pollinators.utils

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import kotlinx.coroutines.delay
import java.io.File
import com.example.life4pollinators.R

@Composable
fun rememberCameraLauncher(
    onPhotoReady: (Uri) -> Unit,
    onError: ((Int) -> Unit)? = null   // PATCH: ora prende un Int
): () -> Unit {
    val context = LocalContext.current
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        photoUri?.let { uri ->
            if (success) pendingCameraUri = uri
            else onError?.invoke(R.string.photo_not_saved)   // PATCH: passa id risorsa
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
            else onError?.invoke(R.string.photo_error)   // PATCH: passa id risorsa
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
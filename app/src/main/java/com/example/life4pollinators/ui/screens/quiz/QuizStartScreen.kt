package com.example.life4pollinators.ui.screens.quiz

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.life4pollinators.ui.screens.plantQuiz.PlantQuizActions
import com.example.life4pollinators.ui.screens.plantQuiz.PlantQuizState
import com.example.life4pollinators.utils.rememberCameraLauncher
import com.example.life4pollinators.utils.rememberGalleryLauncher

@Composable
fun QuizStartScreen(
    state: PlantQuizState,
    actions: PlantQuizActions,
    navController: NavHostController
) {
    val context = LocalContext.current
    var showImagePicker by remember { mutableStateOf(false) }
    var localPhoto by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val launchCamera = rememberCameraLauncher(
        onPhotoReady = { uri ->
            localPhoto = uri
            showImagePicker = false
            errorMessage = null
        },
        onError = { resId ->
            // Puoi tradurre il resId in stringa se vuoi
            errorMessage = context.getString(resId)
            showImagePicker = false
        }
    )

    val launchGallery = rememberGalleryLauncher { uri ->
        localPhoto = uri
        showImagePicker = false
        errorMessage = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Scatta una foto della pianta o caricane una", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { showImagePicker = true }) {
            Text("Carica o scatta foto")
        }

        if (showImagePicker) {
            AlertDialog(
                onDismissRequest = { showImagePicker = false },
                title = { Text("Scegli una foto") },
                text = {
                    Column {
                        Button(
                            onClick = { launchCamera() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Scatta foto")
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(imageVector = Icons.Outlined.PhotoCamera, contentDescription = "Camera")
                        }
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { launchGallery() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Galleria")
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(imageVector = Icons.Outlined.Photo, contentDescription = "Gallery")
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showImagePicker = false }) { Text("Annulla") }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        localPhoto?.let { photoUri ->
            Image(
                painter = rememberAsyncImagePainter(photoUri),
                contentDescription = "Foto selezionata",
                modifier = Modifier
                    .size(220.dp)
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                actions.startQuiz(photoUri.toString())
                navController.navigate("plantQuizQuestion")
            }) {
                Text("Inizia il quiz")
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}
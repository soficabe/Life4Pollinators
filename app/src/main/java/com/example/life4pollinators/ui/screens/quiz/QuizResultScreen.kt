package com.example.life4pollinators.ui.screens.quiz

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.life4pollinators.R
import com.example.life4pollinators.ui.composables.AppBar
import com.example.life4pollinators.ui.navigation.L4PRoute
import java.util.Locale

@Composable
fun QuizResultScreen(
    state: QuizState,
    actions: QuizActions,
    isAuthenticated: Boolean,
    userId: String,
    navController: NavHostController
) {
    val locale = Locale.getDefault().language
    val context = LocalContext.current
    var showLensDialog by rememberSaveable { mutableStateOf(false) }

    BackHandler {
        actions.resetQuiz()
        navController.navigate(L4PRoute.Home) {
            popUpTo(L4PRoute.Home) { inclusive = false }
        }
    }

    // Controllo di sicurezza all'inizio
    val selectedTarget = state.selectedTarget
    if (selectedTarget == null) {
        // Fallback - non dovrebbe mai accadere con i nostri fix
        Scaffold(
            topBar = {
                AppBar(
                    navController = navController,
                    showBackButton = false,
                    showSettingsButton = false
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.quiz_error_no_classification),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        actions.resetQuiz()
                        navController.navigate(L4PRoute.Home) {
                            popUpTo(L4PRoute.Home) { inclusive = false }
                        }
                    }) {
                        Text(stringResource(R.string.back_to_home))
                    }
                }
            }
        }
        return
    }

    Scaffold (
        topBar = {
            AppBar(
                navController = navController,
                showBackButton = false,
                showSettingsButton = false
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Badge successo
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.quiz_result_identified),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nome del target - safe access
            val displayName = if (!selectedTarget.name.isNullOrEmpty()) {
                selectedTarget.name
            } else {
                if (locale == "it") selectedTarget.nameIt else selectedTarget.nameEn
            }

            displayName?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Immagine del target
            selectedTarget.imageUrl?.let { imageUrl ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(imageUrl),
                                contentDescription = stringResource(R.string.quiz_identified_photo),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Text(
                            text = stringResource(R.string.quiz_identified_photo),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bottone Google Lens
            if (state.photoUrl != null) {
                Button(
                    onClick = { showLensDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ImageSearch,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.quiz_open_with_lens),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Bottone carica avvistamento
            if (isAuthenticated && state.photoUrl != null) {
                Button(
                    onClick = {
                        actions.submitQuizSighting(context, userId)
                    },
                    enabled = !state.isUploading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Upload,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.upload_sighting),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                if (state.isUploading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                if (state.uploadSuccess == true) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.sighting_uploaded),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (state.uploadSuccess == false) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.uploadError?.let { stringResource(it) } ?: stringResource(R.string.sighting_upload_error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Bottone riprova
            OutlinedButton(
                onClick = {
                    actions.resetQuizKeepingPhoto()
                    navController.navigate("quizStart/${state.originalQuizType}") {
                        popUpTo(L4PRoute.Home) { inclusive = false }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.quiz_try_again),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottone home
            OutlinedButton(
                onClick = {
                    actions.resetQuiz()
                    navController.navigate(L4PRoute.Home) {
                        popUpTo(L4PRoute.Home) { inclusive = false }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Home,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.back_to_home),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Dialog Google Lens
    if (showLensDialog && state.photoUrl != null) {
        AlertDialog(
            onDismissRequest = { showLensDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.quiz_open_with_lens),
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.quiz_open_with_lens_info),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(onClick = {
                    showLensDialog = false
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/*"
                        putExtra(Intent.EXTRA_STREAM, Uri.parse(state.photoUrl))
                    }
                    val chooser = Intent.createChooser(intent, context.getString(R.string.quiz_open_with_lens_chooser))
                    context.startActivity(chooser)
                }) {
                    Text(stringResource(R.string.quiz_open_with_lens_continue))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLensDialog = false }) {
                    Text(stringResource(R.string.quiz_cancel))
                }
            }
        )
    }
}
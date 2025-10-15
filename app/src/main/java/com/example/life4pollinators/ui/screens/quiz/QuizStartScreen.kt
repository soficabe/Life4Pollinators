package com.example.life4pollinators.ui.screens.quiz

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.life4pollinators.R
import com.example.life4pollinators.data.models.NavBarTab
import com.example.life4pollinators.ui.composables.AppBar
import com.example.life4pollinators.ui.composables.BottomNavBar
import com.example.life4pollinators.ui.composables.ImagePickerDialog
import com.example.life4pollinators.ui.navigation.L4PRoute
import com.example.life4pollinators.utils.rememberCameraLauncher
import com.example.life4pollinators.utils.rememberGalleryLauncher

@Composable
fun QuizStartScreen(
    state: QuizState,
    actions: QuizActions,
    isAuthenticated: Boolean,
    navController: NavHostController
) {
    val context = LocalContext.current
    var showImagePicker by rememberSaveable { mutableStateOf(false) }

    // FIX: Usa rememberSaveable per mantenere la foto durante la rotazione
    var localPhoto by rememberSaveable(stateSaver = UriSaver) {
        mutableStateOf(state.photoUrl?.let { Uri.parse(it) })
    }

    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var quizStarted by rememberSaveable { mutableStateOf(false) }

    // Gestione del back button - resetta il quiz quando si va indietro
    BackHandler {
        actions.resetQuiz()
        navController.popBackStack()
    }

    // Effect per sincronizzare localPhoto con state.photoUrl
    LaunchedEffect(state.photoUrl) {
        if (state.photoUrl != null && localPhoto == null) {
            localPhoto = Uri.parse(state.photoUrl)
        }
    }

    val launchCamera = rememberCameraLauncher(
        onPhotoReady = { uri ->
            localPhoto = uri
            showImagePicker = false
            errorMessage = null
        },
        onError = { resId ->
            errorMessage = context.getString(resId)
            showImagePicker = false
        }
    )

    val launchGallery = rememberGalleryLauncher { uri ->
        localPhoto = uri
        showImagePicker = false
        errorMessage = null
    }

    val titleRes = when (state.quizType) {
        "plant" -> R.string.quiz_start_title_plant
        "insect" -> R.string.quiz_start_title_insect
        else -> R.string.quiz_start_title_default
    }

    Scaffold (
        topBar = {
            AppBar(
                navController = navController,
                personalizedTitle = stringResource(R.string.title_quiz_start),
                onBackClick = {
                    actions.resetQuiz()
                    navController.popBackStack()
                }
            )
        },
        bottomBar = {
            BottomNavBar(
                isAuthenticated = isAuthenticated,
                selectedTab = NavBarTab.Home,
                navController = navController
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
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Photo display area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (localPhoto != null) {
                    Image(
                        painter = rememberAsyncImagePainter(localPhoto),
                        contentDescription = stringResource(R.string.quiz_selected_photo),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.PhotoCamera,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Bottom buttons
            if (localPhoto == null) {
                Button(
                    onClick = { showImagePicker = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.quiz_upload_button),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                Button(
                    onClick = {
                        if (state.quizType == "insect") {
                            actions.loadInsectGroups(localPhoto.toString())
                        } else {
                            actions.startQuiz(localPhoto.toString())
                        }
                        quizStarted = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.quiz_start_button),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { showImagePicker = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.quiz_change_photo),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Image picker dialog
    if (showImagePicker) {
        ImagePickerDialog(
            onDismiss = { showImagePicker = false },
            onCameraClick = {
                launchCamera()
                showImagePicker = false
            },
            onGalleryClick = {
                launchGallery()
                showImagePicker = false
            }
        )
    }

    // Navigate when quiz is ready
    LaunchedEffect(state.step, state.loading, quizStarted) {
        if (quizStarted && !state.loading) {
            when (state.step) {
                QuizStep.Question -> {
                    navController.navigate(L4PRoute.QuizQuestion)
                    quizStarted = false
                }
                QuizStep.InsectTypeSelection -> {
                    navController.navigate(L4PRoute.QuizInsectTypeSelection)
                    quizStarted = false
                }
                else -> {}
            }
        }
    }
}

// Saver personalizzato per Uri
private val UriSaver = androidx.compose.runtime.saveable.Saver<Uri?, String>(
    save = { it?.toString() },
    restore = { it.let { Uri.parse(it) } }
)
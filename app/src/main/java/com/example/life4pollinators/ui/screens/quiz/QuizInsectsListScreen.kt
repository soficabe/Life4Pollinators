package com.example.life4pollinators.ui.screens.quiz

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.life4pollinators.R
import com.example.life4pollinators.ui.composables.AppBar
import com.example.life4pollinators.ui.composables.ExitQuizDialog
import com.example.life4pollinators.ui.composables.InsectCard
import com.example.life4pollinators.ui.navigation.L4PRoute

@Composable
fun QuizInsectsListScreen(
    state: QuizState,
    actions: QuizActions,
    navController: NavHostController
) {
    var showExitDialog by remember { mutableStateOf(false) }

    // Gestione del back button con dialog di conferma
    BackHandler {
        showExitDialog = true
    }

    LaunchedEffect(state.step) {
        if (state.step == QuizStep.Result) {
            navController.navigate(L4PRoute.QuizResult)
        }
    }

    Scaffold(
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
        ) {
            // Photo preview at top
            state.photoUrl?.let { photoUrl ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(Uri.parse(photoUrl)),
                                contentDescription = stringResource(R.string.quiz_your_photo),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Text(
                            text = stringResource(R.string.quiz_your_photo),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // Instructions
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.quiz_select_insect_from_list),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Insects list
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                when {
                    state.loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                    state.error != null -> Text(
                        stringResource(state.error),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(state.insectsForSelection) { insect ->
                                InsectCard(
                                    insect = insect,
                                    onClick = {
                                        actions.selectInsectFromList(insect)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog di conferma uscita
    if (showExitDialog) {
        ExitQuizDialog(
            onDismiss = { showExitDialog = false },
            onConfirm = {
                showExitDialog = false
                actions.resetQuiz()
                navController.navigate("quizStart/${state.originalQuizType}") {
                    popUpTo(L4PRoute.Home) { inclusive = false }
                }
            }
        )
    }
}
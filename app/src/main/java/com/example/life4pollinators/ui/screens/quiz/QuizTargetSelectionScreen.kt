package com.example.life4pollinators.ui.screens.quiz

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.life4pollinators.R
import com.example.life4pollinators.ui.composables.AppBar
import com.example.life4pollinators.ui.composables.ExitQuizDialog
import com.example.life4pollinators.ui.navigation.L4PRoute
import java.util.Locale

/**
 * Schermata selezione target multipli.
 *
 * Mostrata quando una risposta foglia ha più di un target associato.
 * Permette all'utente di scegliere quale specie corrisponde meglio
 * alla sua osservazione.
 *
 * Al click → selectTarget() → QuizStep.Result
 *
 * @param state Stato quiz con possibleTargets popolato
 * @param actions Azioni quiz
 * @param navController Controller navigazione
 */
@Composable
fun QuizTargetSelectionScreen(
    state: QuizState,
    actions: QuizActions,
    navController: NavHostController
) {
    val locale = Locale.getDefault().language
    var showExitDialog by remember { mutableStateOf(false) }

    // BackHandler con dialog - l'utente potrebbe voler ripensarci
    BackHandler {
        showExitDialog = true
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
        ) {
            // Preview foto
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
                                .height(240.dp)
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

            // Lista target
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Istruzioni
                Text(
                    text = stringResource(R.string.quiz_select_match),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Card per ogni target suggerito
                state.possibleTargets.forEach { targetWithDetails ->
                    Card(
                        onClick = {
                            actions.selectTarget(targetWithDetails)
                            navController.navigate(L4PRoute.QuizResult)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Immagine target
                            targetWithDetails.imageUrl?.let { imageUrl ->
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White)
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(imageUrl),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                            }

                            // Nome target (gestisce sia insetti che piante)
                            val displayName = targetWithDetails.name?.takeIf { it.isNotEmpty() }
                                ?: if (locale == "it") targetWithDetails.nameIt else targetWithDetails.nameEn

                            displayName?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Dialog uscita
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
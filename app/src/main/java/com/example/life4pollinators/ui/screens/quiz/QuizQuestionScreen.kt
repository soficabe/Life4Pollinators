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
import com.example.life4pollinators.ui.composables.AppBar
import com.example.life4pollinators.ui.composables.ExitQuizDialog
import com.example.life4pollinators.ui.navigation.L4PRoute
import java.util.Locale

/**
 * Schermata domanda del quiz (cuore dell'albero decisionale).
 *
 * Mostra:
 * - Preview foto dell'utente in alto
 * - Testo domanda corrente (localizzato IT/EN)
 * - Immagine illustrativa della domanda (opzionale)
 * - Lista di risposte cliccabili con immagini opzionali
 *
 * Comportamento al click risposta:
 * 1. actions.answerQuestion(answer) chiamata
 * 2. ViewModel determina il prossimo step:
 *    - Se answer.nextQuestion != null → carica nuova domanda (rimane su questa screen)
 *    - Se answer.nextQuestion == null (foglia):
 *      a. Un solo target → QuizStep.Result (navigazione automatica)
 *      b. Più target → QuizStep.TargetSelection (navigazione automatica)
 *
 * La navigazione è automatica tramite LaunchedEffect che osserva state.step.
 *
 * @param state Stato quiz condiviso
 * @param actions Azioni quiz
 * @param navController Controller navigazione
 */
@Composable
fun QuizQuestionScreen(
    state: QuizState,
    actions: QuizActions,
    navController: NavHostController
) {
    val locale = Locale.getDefault().language
    val context = LocalContext.current
    var showExitDialog by rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // BackHandler con dialog conferma uscita
    BackHandler {
        showExitDialog = true
    }

    // Mostra errori con Snackbar
    LaunchedEffect(state.error) {
        if (state.error != null) {
            snackbarHostState.showSnackbar(context.getString(state.error))
        }
    }

    // Navigazione automatica basata su step
    LaunchedEffect(state.step) {
        when (state.step) {
            QuizStep.TargetSelection -> navController.navigate(L4PRoute.QuizTargetSelection)
            QuizStep.Result -> navController.navigate(L4PRoute.QuizResult)
            else -> {}
        }
    }

    Scaffold (
        topBar = {
            AppBar(
                navController = navController,
                showBackButton = false,
                showSettingsButton = false
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Preview foto in alto
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

            // Sezione domanda e risposte
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                if (state.loading || state.currentQuestion == null) {
                    // Stato loading
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Testo domanda (localizzato)
                    Text(
                        text = if (locale == "it")
                            state.currentQuestion.questionTextIt
                        else
                            state.currentQuestion.questionTextEn,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Lista risposte
                    state.answers.forEach { answer ->
                        Card(
                            onClick = { actions.answerQuestion(answer) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                // Immagine risposta (opzionale)
                                if (answer.imageUrl != null) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(120.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                    ) {
                                        Image(
                                            painter = rememberAsyncImagePainter(answer.imageUrl),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                }

                                // Testo risposta (localizzato)
                                Text(
                                    text = if (locale == "it")
                                        answer.answerTextIt
                                    else
                                        answer.answerTextEn,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
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
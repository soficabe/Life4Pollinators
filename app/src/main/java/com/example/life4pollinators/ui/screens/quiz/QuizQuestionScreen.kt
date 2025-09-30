package com.example.life4pollinators.ui.screens.quiz

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.life4pollinators.ui.navigation.L4PRoute
import java.util.Locale

@Composable
fun QuizQuestionScreen(
    state: QuizState,
    actions: QuizActions,
    navController: NavHostController
) {
    val locale = Locale.getDefault().language

    // PATCH: Usa L4PRoute invece di stringhe
    LaunchedEffect(state.step) {
        when (state.step) {
            QuizStep.TargetSelection -> navController.navigate(L4PRoute.QuizTargetSelection)
            QuizStep.Result -> navController.navigate(L4PRoute.QuizResult)
            else -> {}
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        state.photoUrl?.let {
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (state.loading || state.currentQuestion == null) {
            CircularProgressIndicator()
        } else {
            Text(
                if (locale == "it")
                    state.currentQuestion.questionTextIt
                else
                    state.currentQuestion.questionTextEn,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            state.answers.forEach { answer ->
                Button(
                    onClick = { actions.answerQuestion(answer) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        if (locale == "it")
                            answer.answerTextIt
                        else
                            answer.answerTextEn
                    )
                }
            }
        }
    }
}
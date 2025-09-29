package com.example.life4pollinators.ui.screens.quiz

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.life4pollinators.ui.screens.plantQuiz.PlantQuizActions
import com.example.life4pollinators.ui.screens.plantQuiz.PlantQuizState
import com.example.life4pollinators.ui.screens.plantQuiz.PlantQuizStep

@Composable
fun QuizQuestionScreen(
    state: PlantQuizState,
    actions: PlantQuizActions,
    navController: NavHostController
) {
    // Navigation effect: NAVIGATE when step changes
    LaunchedEffect(state.step) {
        when (state.step) {
            PlantQuizStep.TargetSelection -> navController.navigate("plantQuizTargetSelection")
            PlantQuizStep.Result -> navController.navigate("plantQuizResult")
            else -> {} // Stay here for Step.Question
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        state.photoUrl?.let {
            // Image(painter = rememberImagePainter(it), ...)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(state.currentQuestion?.questionTextEn ?: "No question", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        state.answers.forEach { answer ->
            Button(
                onClick = { actions.answerQuestion(answer) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(answer.answerTextEn)
            }
        }
    }
}
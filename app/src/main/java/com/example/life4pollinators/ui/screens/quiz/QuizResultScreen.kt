package com.example.life4pollinators.ui.screens.quiz

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.life4pollinators.ui.screens.plantQuiz.PlantQuizActions
import com.example.life4pollinators.ui.screens.plantQuiz.PlantQuizState

@Composable
fun QuizResultScreen(
    state: PlantQuizState,
    actions: PlantQuizActions,
    navController: NavHostController
) {
    Column(modifier = Modifier.padding(16.dp)) {
        state.photoUrl?.let {
            // Image(painter = rememberImagePainter(it), ...)
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (state.selectedTarget != null) {
            Text("The plant belongs to the ${state.selectedTarget.targetType} family!", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                actions.resetQuiz()
                navController.navigate("plantQuizStart")
            }) {
                Text("Try again")
            }
            // Button for AI validation in futuro
        } else {
            Text("No classification found.", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = {
                actions.resetQuiz()
                navController.navigate("plantQuizStart")
            }) {
                Text("Restart Quiz")
            }
        }
    }
}
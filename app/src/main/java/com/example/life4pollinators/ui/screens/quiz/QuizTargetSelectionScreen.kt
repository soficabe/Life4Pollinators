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
fun QuizTargetSelectionScreen(
    state: PlantQuizState,
    actions: PlantQuizActions,
    navController: NavHostController
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Select the correct plant from possible matches:", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        state.possibleTargets.forEach { target ->
            Button(
                onClick = {
                    actions.selectTarget(target)
                    navController.navigate("plantQuizResult")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text("${target.targetType}: ${target.targetId}")
            }
        }
    }
}
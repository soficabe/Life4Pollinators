package com.example.life4pollinators.ui.composables

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.life4pollinators.R

/**
 * Dialog di conferma uscita dal quiz.
 *
 * Mostra un alert dialog che chiede conferma all'utente prima di uscire dal quiz,
 * prevenendo perdite accidentali di progressi.
 *
 * Utilizzato in tutte le schermate del quiz per gestire:
 * - BackHandler (pulsante indietro fisico/gesture)
 * - Tentativi di navigazione fuori dal flusso quiz
 * - Uscita volontaria dall'utente
 *
 * @param onDismiss Callback invocata quando l'utente annulla (vuole restare nel quiz)
 * @param onConfirm Callback invocata quando l'utente conferma l'uscita.
 *                  Tipicamente esegue: actions.resetQuiz() + navigazione
 */
@Composable
fun ExitQuizDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss, // Tap fuori dal dialog
        icon = {
            Icon(
                imageVector = Icons.Outlined.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = stringResource(R.string.quiz_exit_title),
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Text(
                text = stringResource(R.string.quiz_exit_message),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.quiz_exit_confirm),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.quiz_exit_cancel))
            }
        }
    )
}
package com.example.life4pollinators.ui.composables

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.life4pollinators.R
import kotlinx.datetime.LocalTime

/**
 * Dialog Material TimePicker.
 *
 * Permette la selezione dell'ora dell'avvistamento.
 *
 * @param initialValue Ora iniziale selezionata (se presente)
 * @param onTimeSelected Callback con l'ora selezionata
 * @param onDismiss Callback di chiusura dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialTimePickerDialog(
    initialValue: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    // Valori iniziali (default 12:00)
    val initialHour = initialValue?.hour ?: 12
    val initialMinute = initialValue?.minute ?: 0

    val pickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false // Formato 12 ore (AM/PM)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected(LocalTime(pickerState.hour, pickerState.minute))
                    onDismiss()
                }
            ) { Text("Ok") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
        text = {
            TimePicker(state = pickerState)
        }
    )
}
package com.example.life4pollinators.ui.composables

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.life4pollinators.R
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.ZoneId

/**
 * Dialog Material DatePicker con restrizioni.
 *
 * Permette selezione solo di date passate o odierne
 * (non si può registrare un avvistamento nel futuro).
 *
 * @param initialValue Data iniziale selezionata (se presente)
 * @param onDateSelected Callback con la data selezionata
 * @param onDismiss Callback di chiusura dialog
 */
@Composable
fun MaterialDatePickerDialog(
    initialValue: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    // Data odierna come limite massimo
    val today = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).date

    val pickerState = rememberDatePickerState(
        // Converti LocalDate iniziale in millisecondi
        initialSelectedDateMillis = initialValue?.let {
            java.time.LocalDate.of(it.year, it.monthNumber, it.dayOfMonth)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        },
        // Restrizione: solo date <= oggi
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val date = java.time.Instant.ofEpochMilli(utcTimeMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                val kotlinDate = LocalDate(date.year, date.monthValue, date.dayOfMonth)
                return kotlinDate <= today
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = pickerState.selectedDateMillis
                    if (millis != null) {
                        // Converti millisecondi in LocalDate
                        val date = java.time.Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(LocalDate(date.year, date.monthValue, date.dayOfMonth))
                    }
                    onDismiss()
                }
            ) { Text("Ok") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    ) {
        DatePicker(state = pickerState)
    }
}
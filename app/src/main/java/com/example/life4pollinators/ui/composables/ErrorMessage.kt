package com.example.life4pollinators.ui.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Composable riutilizzabile per mostrare messaggi di errore centrati.
 *
 * Visualizza un'icona "CloudOff" e un messaggio localizzato in posizione centrale.
 * Utilizzato quando si verificano errori di rete o quando i dati non sono disponibili.
 *
 * @param errorResId ID della risorsa stringa da visualizzare come messaggio d'errore
 * @param modifier Modificatore opzionale per personalizzare layout e stile
 */
@Composable
fun ErrorMessage(
    errorResId: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icona di errore
        Icon(
            imageVector = Icons.Outlined.CloudOff,
            contentDescription = "No connection Icon",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Messaggio d'errore localizzato
        Text(
            text = stringResource(errorResId),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
    }
}
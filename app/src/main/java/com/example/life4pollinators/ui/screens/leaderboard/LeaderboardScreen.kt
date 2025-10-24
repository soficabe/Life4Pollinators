package com.example.life4pollinators.ui.screens.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.life4pollinators.R
import com.example.life4pollinators.data.models.NavBarTab
import com.example.life4pollinators.ui.composables.AppBar
import com.example.life4pollinators.ui.composables.BottomNavBar

/**
 * Schermata della classifica globale degli utenti.
 *
 * Caratteristiche:
 * - Lista ordinata di tutti gli utenti con punteggio
 * - Medaglie colorate per i primi 3 posti (oro, argento, bronzo)
 * - Evidenziazione dell'utente corrente con card colorata
 * - Gestione parimerito (utenti con stesso punteggio ordinati alfabeticamente)
 * - Gestione stati loading/error/empty
 *
 * @param state Stato corrente della leaderboard
 * @param actions Interfaccia delle azioni disponibili
 * @param navController Controller di navigazione Compose
 */
@Composable
fun LeaderboardScreen(
    state: LeaderboardState,
    actions: LeaderboardActions,
    navController: NavHostController
) {
    // Carica la leaderboard all'apertura della schermata
    LaunchedEffect(Unit) {
        actions.loadLeaderboard()
    }

    Scaffold(
        topBar = {
            AppBar(navController)
        },
        bottomBar = {
            BottomNavBar(
                navController = navController,
                selectedTab = NavBarTab.Profile
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                // Loading state
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                // Error state
                state.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(state.error),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { actions.refresh() }) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
                // Empty state
                state.entries.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.leaderboard_empty),
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                // Success state - mostra la lista
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.entries) { entry ->
                            LeaderboardEntryCard(entry = entry)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card che rappresenta un singolo utente nella classifica.
 *
 * Caratteristiche:
 * - Badge con posizione (numero o medaglia per top 3)
 * - Username con etichetta "Tu" per l'utente corrente
 * - Punteggio evidenziato a destra
 * - Card colorata ed elevata per l'utente corrente
 * - Medaglie colorate: oro (1°), argento (2°), bronzo (3°)
 *
 * @param entry Dati dell'utente da visualizzare
 */
@Composable
fun LeaderboardEntryCard(entry: LeaderboardEntry) {
    // Sfondo diverso per l'utente corrente
    val backgroundColor = if (entry.isCurrentUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    // Colori medaglie per top 3
    val medalColor = when (entry.position) {
        1 -> MaterialTheme.colorScheme.tertiary // Oro
        2 -> MaterialTheme.colorScheme.secondary // Argento
        3 -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f) // Bronzo
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (entry.isCurrentUser) 6.dp else 2.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Posizione + Username
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Badge circolare con posizione
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    if (entry.position <= 3) {
                        // Mostra medaglia per top 3
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Medal",
                            tint = medalColor,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        // Mostra numero posizione
                        Text(
                            text = "${entry.position}°",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (entry.isCurrentUser)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Username ed etichetta "Tu" se è l'utente corrente
                Column {
                    Text(
                        text = "@${entry.username}",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (entry.isCurrentUser) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (entry.isCurrentUser) {
                        Text(
                            text = stringResource(R.string.you),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Punteggio
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${entry.score}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.points),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
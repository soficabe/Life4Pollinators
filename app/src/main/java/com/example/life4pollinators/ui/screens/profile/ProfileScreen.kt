package com.example.life4pollinators.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.life4pollinators.R
import com.example.life4pollinators.data.models.NavBarTab
import com.example.life4pollinators.ui.composables.AppBar
import com.example.life4pollinators.ui.composables.BottomNavBar
import com.example.life4pollinators.ui.composables.ProfileIcon
import com.example.life4pollinators.ui.navigation.L4PRoute

/**
 * Schermata profilo utente.
 *
 * Visualizza i dati principali dell'utente autenticato (username, nome, cognome, email, immagine).
 * Mostra statistiche (avvistamenti, quiz svolti) e badge di ranking giornaliero e settimanale.
 * Permette di accedere all'editing del profilo tramite bottone.
 *
 * @param state Stato corrente del profilo utente
 * @param actions Interfaccia delle azioni disponibili
 * @param navController Controller di navigazione Compose
 */
@Composable
fun ProfileScreen(
    state: ProfileState,
    actions: ProfileActions,
    navController: NavHostController
) {
    val scrollState = rememberScrollState()

    // Aggiorna il profilo quando la schermata viene caricata
    LaunchedEffect(Unit) {
        actions.refreshProfile()
    }

    Scaffold(
        topBar = { AppBar(navController) },
        bottomBar = { BottomNavBar(selectedTab = NavBarTab.Profile, navController = navController) }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))

            // PATCH: aggiungi parametro fittizio all'URL dell'immagine per forzare il refresh
            val profileImageUrl = state.user?.image?.let { img ->
                if (img.contains("?t=")) img else "$img?t=${System.currentTimeMillis()}"
            }

            // Avatar utente (con loader se necessario)
            ProfileIcon(
                imageUrl = profileImageUrl,
                isClickable = false,
                showLoader = state.isRefreshing
            )

            Spacer(Modifier.height(12.dp))

            // Username
            Text(
                "@${state.user?.username ?: ""}",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(10.dp))

            // Dati anagrafici
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("${stringResource(R.string.first_name)}: ${state.user?.firstName ?: ""}", style = MaterialTheme.typography.bodyMedium)
                Text("${stringResource(R.string.last_name)}: ${state.user?.lastName ?: ""}", style = MaterialTheme.typography.bodyMedium)
                Text("${stringResource(R.string.email)}: ${state.user?.email ?: ""}", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(16.dp))

            // Statistiche utente
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                ProfileStatCard(
                    value = "10/39", // TODO: Rendi dinamico!
                    label = stringResource(R.string.title_sightings),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.weight(1f)
                )
                ProfileStatCard(
                    value = "8", // TODO: Rendi dinamico!
                    label = stringResource(R.string.tests_taken),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(20.dp))

            // Ranking
            Text(
                stringResource(R.string.ranking),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) {
                RankingBadge(
                    period = stringResource(R.string.day),
                    rank = 5, // TODO: Rendi dinamico!
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                RankingBadge(
                    period = stringResource(R.string.week),
                    rank = 2, // TODO: Rendi dinamico!
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            // Bottone modifica profilo
            FilledTonalButton(
                onClick = { navController.navigate(L4PRoute.EditProfile) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(imageVector = Icons.Outlined.Edit, contentDescription = "Edit Profile")
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.title_edit_profile),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

/**
 * Card per mostrare una statistica del profilo (ad esempio avvistamenti o quiz svolti)
 *
 * @param value Valore numerico/stringa da mostrare
 * @param label Etichetta della statistica
 * @param modifier Modifier Compose
 * @param color Colore di sfondo della card
 */
@Composable
fun ProfileStatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.secondaryContainer
) {
    Surface(
        modifier = modifier
            .height(82.dp)
            .widthIn(min = 110.dp),
        color = color,
        tonalElevation = 3.dp,
        shadowElevation = 4.dp,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(11.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Badge per il ranking utente in un determinato periodo.
 *
 * @param modifier Modifier Compose
 * @param period Periodo di riferimento (giorno, settimana, ecc.)
 * @param rank Posizione in classifica
 * @param color Colore principale del badge
 */
@Composable
fun RankingBadge(
    modifier: Modifier = Modifier,
    period: String,
    rank: Int,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Box(
                modifier = Modifier
                    .size(55.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                color.copy(alpha = 0.25f),
                                color.copy(alpha = 0.10f)
                            )
                        ),
                        shape = CircleShape
                    )
                    .border(2.dp, color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${rank}°",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = color
                )
            }

            // Label periodo
            Text(
                text = period,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
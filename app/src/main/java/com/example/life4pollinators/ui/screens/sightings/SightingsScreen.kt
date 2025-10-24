package com.example.life4pollinators.ui.screens.sightings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.life4pollinators.data.models.NavBarTab
import com.example.life4pollinators.ui.composables.AppBar
import com.example.life4pollinators.ui.composables.BottomNavBar
import com.example.life4pollinators.ui.composables.ErrorMessage
import com.example.life4pollinators.ui.navigation.L4PRoute

/**
 * Schermata per visualizzare tutte le specie (piante e insetti) avvistate dall'utente.
 * Funge da collezione personale degli avvistamenti dell'utente (badge system).
 *
 * Caratteristiche:
 * - Griglia 3x3 con specie circolari
 * - Filtri per categoria (piante, api, farfalle, ecc.)
 * - Blur su specie non ancora avvistate dall'utente
 * - FAB per aggiungere nuovi avvistamenti
 * - Gestione stati loading/error/success
 *
 * La schermata mostra tutte le specie del database, evidenziando visivamente
 * quali sono già state avvistate dall'utente autenticato (sfocando le altre).
 *
 * @param state Stato corrente della schermata gestito dal ViewModel
 * @param actions Interfaccia delle azioni disponibili (filtri, refresh)
 * @param userId ID dell'utente autenticato
 * @param navController Controller di navigazione Compose
 */
@Composable
fun SightingsScreen(
    state: SightingsState,
    actions: SightingsActions,
    userId: String,
    navController: NavHostController
) {
    // Carica gli avvistamenti quando userId è disponibile
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            actions.loadUserSightings(userId)
        }
    }

    Scaffold(
        topBar = {
            AppBar(
                navController = navController
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(L4PRoute.AddSighting) }) {
                Icon(Icons.Outlined.Add, contentDescription = "Add Sighting")
            }
        },
        bottomBar = {
            BottomNavBar(
                selectedTab = NavBarTab.Sightings,
                navController = navController
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Barra dei filtri orizzontale
            FilterChips(
                selectedFilter = state.selectedFilter,
                onFilterSelected = { actions.selectFilter(it) },
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Gestione stati: loading, error, content
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    state.error != null -> {
                        ErrorMessage(errorResId = state.error)
                    }
                    else -> {
                        // Griglia 3x3 delle piante o gruppo di insetti selezionato
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.filteredSpecies) { species ->
                                SpeciesCircleItem(species = species)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Barra di filtri orizzontale scorrevole per selezionare la categoria di specie da visualizzare.
 *
 * Mostra chip filtro per ogni categoria disponibile (piante, api, farfalle, ecc.).
 * Il filtro selezionato è evidenziato visivamente.
 *
 * @param selectedFilter Filtro attualmente selezionato
 * @param onFilterSelected Callback invocato quando l'utente seleziona un filtro
 * @param modifier Modifier Compose opzionale
 */
@Composable
fun FilterChips(
    selectedFilter: SpeciesFilter,
    onFilterSelected: (SpeciesFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SpeciesFilter.entries.forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(stringResource(filter.displayNameRes)) }
            )
        }
    }
}

/**
 * Composable che rappresenta una singola specie nella griglia.
 *
 * Caratteristiche:
 * - Immagine circolare della specie
 * - Blur automatico se non ancora avvistata
 * - Placeholder con iniziali se manca l'immagine
 * - Nome specie sotto l'immagine
 *
 * @param species Dati della specie da visualizzare
 * @param modifier Modifier Compose opzionale
 */
@Composable
fun SpeciesCircleItem(
    species: SpeciesItem,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    if (species.isSighted) Color.Transparent
                    else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (species.imageUrl != null) {
                // Immagine della specie
                Image(
                    painter = rememberAsyncImagePainter(species.imageUrl),
                    contentDescription = species.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .then(
                            // Applica blur SOLO se NON è avvistata
                            if (!species.isSighted) Modifier.blur(10.dp)
                            else Modifier
                        ),
                    contentScale = ContentScale.Fit
                )
            } else {
                // Placeholder con iniziali se non c'è immagine
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = species.name.take(2).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // Nome della specie
        Text(
            text = species.name,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 100.dp)
        )
    }
}
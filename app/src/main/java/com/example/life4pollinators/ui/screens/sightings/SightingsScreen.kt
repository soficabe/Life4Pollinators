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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.life4pollinators.data.models.NavBarTab
import com.example.life4pollinators.ui.composables.AppBar
import com.example.life4pollinators.ui.composables.BottomNavBar
import com.example.life4pollinators.ui.navigation.L4PRoute

@Composable
fun SightingsScreen(
    state: SightingsState,
    actions: SightingsActions,
    isAuthenticated: Boolean,
    navController: NavHostController
) {
    Scaffold(
        topBar = {
            AppBar(
                navController = navController,
                personalizedTitle = "Sightings"
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(L4PRoute.AddSighting) }) {
                Icon(Icons.Outlined.Add, contentDescription = "Add Sighting")
            }
        },
        bottomBar = {
            BottomNavBar(
                isAuthenticated = isAuthenticated,
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
            // Filter tabs - ora con scroll orizzontale
            FilterChips(
                selectedFilter = state.selectedFilter,
                onFilterSelected = { actions.selectFilter(it) },
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Loading indicator
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Grid di specie
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
                label = { Text(filter.displayName) }
            )
        }
    }
}

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
                Image(
                    painter = rememberAsyncImagePainter(species.imageUrl),
                    contentDescription = species.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            // Applica blur SOLO se NON è avvistata
                            if (!species.isSighted) Modifier.blur(10.dp)
                            else Modifier
                        ),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder se non c'è immagine
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
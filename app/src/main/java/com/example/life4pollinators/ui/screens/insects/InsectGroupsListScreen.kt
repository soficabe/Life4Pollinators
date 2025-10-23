package com.example.life4pollinators.ui.screens.insects

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.life4pollinators.data.database.entities.InsectGroup
import com.example.life4pollinators.data.models.NavBarTab
import com.example.life4pollinators.ui.composables.AppBar
import com.example.life4pollinators.ui.composables.BottomNavBar
import com.example.life4pollinators.ui.composables.ErrorMessage
import com.example.life4pollinators.ui.navigation.L4PRoute
import java.util.Locale

/**
 * Schermata lista gruppi di insetti.
 *
 * Mostra tutti i gruppi disponibili (Api, Farfalle, ecc.) in una lista scrollabile.
 * FAB per navigare alle info generali sugli insetti.
 */
@Composable
fun InsectGroupsListScreen(
    state: InsectGroupsListState,
    isAuthenticated: Boolean,
    navController: NavHostController
) {
    Scaffold(
        topBar = {
            AppBar(navController)
        },
        // FAB per info generali
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(L4PRoute.InsectsGeneralInfo) }) {
                Icon(Icons.Outlined.Info, contentDescription = "Info")
            }
        },
        bottomBar = {
            BottomNavBar(
                isAuthenticated = isAuthenticated,
                selectedTab = NavBarTab.Home,
                navController = navController
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                state.error != null -> {
                    ErrorMessage(errorResId = state.error)
                }
                else -> {
                    // Lista gruppi
                    LazyColumn(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(state.insectGroups) { insectGroup ->
                            InsectGroupCard(
                                insectGroup = insectGroup,
                                onClick = {
                                    // Naviga alla lista insetti del gruppo
                                    navController.navigate(L4PRoute.InsectsList(insectGroup.id))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card per visualizzare un singolo gruppo di insetti nella lista.
 */
@Composable
fun InsectGroupCard(
    insectGroup: InsectGroup,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(16.dp)
        ) {
            // Nome gruppo localizzato
            val insectGroupName = if (Locale.getDefault().language == "it") insectGroup.nameIt else insectGroup.nameEn

            Text(
                insectGroupName,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Immagine gruppo
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = insectGroup.groupImageUrl,
                    contentDescription = insectGroupName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}
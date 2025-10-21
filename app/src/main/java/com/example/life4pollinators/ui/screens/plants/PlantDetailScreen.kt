package com.example.life4pollinators.ui.screens.plants

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.life4pollinators.R
import com.example.life4pollinators.data.models.NavBarTab
import com.example.life4pollinators.ui.composables.AppBar
import com.example.life4pollinators.ui.composables.BottomNavBar
import com.example.life4pollinators.ui.composables.ErrorMessage
import com.example.life4pollinators.ui.composables.ZoomOverlayImage
import java.util.Locale

@Composable
fun PlantDetailScreen(
    state: PlantDetailState,
    isAuthenticated: Boolean,
    navController: NavHostController,
) {
    val plant = state.plant
    val pollinatorGroups = state.pollinatorGroups
    val locale = Locale.getDefault().language
    val scrollState = rememberScrollState()
    val plantName = if (locale == "it") plant?.nameIt else plant?.nameEn

    Scaffold(
        topBar = {
            AppBar(
                navController = navController,
                personalizedTitle = plantName
            )
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
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.error != null -> {
                    ErrorMessage(errorResId = state.error)
                }
                plant != null -> {
                    var showZoom by remember { mutableStateOf(false) }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(24.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            elevation = CardDefaults.cardElevation(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(230.dp)
                                .align(Alignment.CenterHorizontally)
                        ) {
                            AsyncImage(
                                model = state.plant.imageUrl,
                                contentDescription = plantName,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable { showZoom = true },
                                contentScale = ContentScale.Fit
                            )
                        }
                        Spacer(Modifier.height(20.dp))

                        Text("${stringResource(R.string.most_common_genera)}:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(plant.commonGenera, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(16.dp))

                        if (pollinatorGroups.isNotEmpty()) {
                            Text("${stringResource(R.string.pollinator_groups)}:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(pollinatorGroups.joinToString(", "), style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(16.dp))
                        }

                        if (plant.isDiverse) {
                            Text("🌱 ${stringResource(R.string.diverse_plants)}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))
                        }
                        if (!plant.invasiveFlower.isNullOrBlank()) {
                            Text("🚫 ${stringResource(R.string.invasive_plants)}: ${plant.invasiveFlower}", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if(showZoom) {
                        ZoomOverlayImage(
                            imageUrl = plant.imageUrl,
                            contentDescription = plantName,
                            onClose = { showZoom = false }
                        )
                    }
                }
            }
        }
    }
}
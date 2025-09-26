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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.life4pollinators.data.models.NavBarTab
import com.example.life4pollinators.ui.composables.AppBar
import com.example.life4pollinators.ui.composables.BottomNavBar
import com.example.life4pollinators.ui.composables.ZoomOverlayImage
import java.util.Locale

@Composable
fun PlantGeneralInfoScreen(
    state: PlantsGeneralInfoState,
    isAuthenticated: Boolean,
    navController: NavHostController
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = { AppBar(navController) },
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
                state.isLoading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                state.error != null -> {
                    Text(
                        state.error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.info != null -> {
                    val locale = Locale.getDefault().language
                    val title = if (locale == "it") state.info.nameIt else state.info.nameEn
                    val description = if (locale == "it") state.info.infoIt else state.info.infoEn
                    val imageUrl = if (locale == "it") state.info.imageUrlIt else state.info.imageUrlEn

                    // Overlay state
                    var showZoom by remember { mutableStateOf(false) }

                    // Layer 1: contenuto normale scrollabile
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
                                model = imageUrl,
                                contentDescription = title,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable { showZoom = true },
                                contentScale = ContentScale.Fit
                            )
                        }
                        /*
                        CardImage(
                            imageUrl = imageUrl,
                            contentDescription = title,
                            onClick = { showZoom = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(230.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                        */
                        Spacer(Modifier.height(16.dp))
                        Text(
                            title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            description ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Justify
                        )
                    }

                    // Layer 2: overlay MODALE con immagine zoommabile
                    if (showZoom) {
                        ZoomOverlayImage(
                            imageUrl = imageUrl,
                            contentDescription = title,
                            onClose = { showZoom = false }
                        )
                    }
                }
            }
        }
    }
}

/*
@Composable
fun CardImage(
    modifier: Modifier = Modifier,
    imageUrl: String?,
    contentDescription: String?,
    onClick: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(12.dp),
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .then(
                if (onClick != null) Modifier.clickable { onClick() }
                else Modifier
            )
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}
*/
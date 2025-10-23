package com.example.life4pollinators.ui.screens.insects

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.life4pollinators.data.models.NavBarTab
import com.example.life4pollinators.ui.composables.AppBar
import com.example.life4pollinators.ui.composables.BottomNavBar
import com.example.life4pollinators.ui.composables.ErrorMessage
import com.example.life4pollinators.ui.composables.ZoomOverlayImage
import java.util.Locale

/**
 * Schermata di dettaglio di un insetto specifico.
 *
 * Mostra l'immagine localizzata (IT/EN) dell'insetto centrata.
 * L'immagine è cliccabile per attivare lo zoom fullscreen.
 *
 * @param state Stato contenente i dati dell'insetto
 * @param isAuthenticated Indica se l'utente è autenticato
 * @param navController Controller di navigazione
 */
@Composable
fun InsectDetailScreen(
    state: InsectDetailState,
    isAuthenticated: Boolean,
    navController: NavHostController
) {
    val insect = state.insect
    val locale = Locale.getDefault().language

    // Titolo nella AppBar (nome insetto)
    val title = insect?.name ?: ""

    // Stato per gestire overlay zoom
    var showZoom by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { AppBar(navController, personalizedTitle = title) },
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
                // Caricamento
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                // Errore
                state.error != null -> {
                    ErrorMessage(errorResId = state.error)
                }

                // Visualizzazione insetto
                insect != null -> {
                    // Selezione immagine localizzata
                    val imageUrl = if (locale == "it") insect.imageIt else insect.imageEn

                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Card con immagine insetto
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            elevation = CardDefaults.cardElevation(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(230.dp)
                        ) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = insect.name,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { showZoom = true } // Click per zoom
                            )
                        }
                    }

                    // Overlay zoom se attivo
                    if (showZoom) {
                        ZoomOverlayImage(
                            imageUrl = imageUrl,
                            contentDescription = insect.name,
                            onClose = { showZoom = false }
                        )
                    }
                }
            }
        }
    }
}
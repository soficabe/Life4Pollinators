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
import com.example.life4pollinators.ui.composables.ErrorMessage
import com.example.life4pollinators.ui.composables.ZoomOverlayImage
import java.util.Locale

/**
 * Schermata di informazioni generali sulle piante per impollinatori.
 *
 * Visualizza contenuti educativi generali sul tema delle piante.
 *
 * @param state Stato contenente le informazioni generali (testi, immagini localizzate)
 * @param isAuthenticated Indica se l'utente è autenticato (per bottom bar)
 * @param navController Controller di navigazione per back navigation
 */
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
                // Stato di caricamento
                state.isLoading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                // Gestione errore (es. rete non disponibile)
                state.error != null -> {
                    ErrorMessage(errorResId = state.error)
                }

                // Visualizzazione contenuti
                state.info != null -> {
                    // Recupera lingua corrente del dispositivo
                    val locale = Locale.getDefault().language

                    // Selezione contenuti localizzati in base alla lingua
                    val title = if (locale == "it") state.info.nameIt else state.info.nameEn
                    val description = if (locale == "it") state.info.infoIt else state.info.infoEn
                    val imageUrl = if (locale == "it") state.info.imageUrlIt else state.info.imageUrlEn

                    // Stato per controllare visibilità overlay zoom
                    var showZoom by remember { mutableStateOf(false) }

                    // Layer 1: Contenuto principale scrollabile
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(24.dp)
                    ) {
                        // Card con immagine principale (cliccabile)
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
                                    .clickable { showZoom = true }, // Attiva overlay zoom
                                contentScale = ContentScale.Fit
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // Titolo della sezione
                        Text(
                            title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(8.dp))

                        // Testo informativo
                        Text(
                            description ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Justify
                        )
                    }

                    // Layer 2: Overlay MODALE con immagine zoommabile
                    // Si posiziona sopra tutto il contenuto quando showZoom è true
                    if (showZoom) {
                        ZoomOverlayImage(
                            imageUrl = imageUrl,
                            contentDescription = title,
                            onClose = { showZoom = false } // Chiusura tramite callback
                        )
                    }
                }
            }
        }
    }
}
package com.example.life4pollinators.ui.screens.insects

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.life4pollinators.R
import com.example.life4pollinators.data.models.NavBarTab
import com.example.life4pollinators.ui.composables.AppBar
import com.example.life4pollinators.ui.composables.BottomNavBar
import com.example.life4pollinators.ui.composables.ZoomOverlayImage
import java.util.Locale

/**
 * Schermata info generali sugli insetti.
 *
 * Mostra un'immagine di comparazione tra i gruppi di insetti.
 * L'immagine è localizzata (drawable diverso per IT/EN) e cliccabile per zoom.
 *
 * Non usa ViewModel perché l'immagine è locale (drawable) e non servono dati dal DB.
 */
@Composable
fun InsectsGeneralInfoScreen(
    isAuthenticated: Boolean,
    navController: NavHostController
) {
    val scrollState = rememberScrollState()
    val locale = Locale.getDefault().language

    // Selezione drawable in base alla lingua
    val imageRes = if (locale == "it") R.drawable.groups_comparison_it else R.drawable.groups_comparison_en
    val imageDesc = if (locale == "it") "Comparazione gruppi - Italiano" else "Group comparison - English"

    // Stato per overlay zoom
    var showZoom by remember { mutableStateOf(false) }

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
            // Contenuto normale
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    // Immagine locale (drawable)
                    Image(
                        painter = painterResource(imageRes),
                        contentDescription = imageDesc,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { showZoom = true },
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Overlay zoom per immagine locale
            if (showZoom) {
                ZoomOverlayImage(
                    imageRes = imageRes,
                    contentDescription = imageDesc,
                    onClose = { showZoom = false }
                )
            }
        }
    }
}
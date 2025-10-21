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

@Composable
fun InsectDetailScreen(
    state: InsectDetailState,
    isAuthenticated: Boolean,
    navController: NavHostController
) {
    val insect = state.insect
    val locale = Locale.getDefault().language

    val title = insect?.name ?: ""

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
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.error != null -> {
                    ErrorMessage(errorResId = state.error)
                }
                insect != null -> {
                    val imageUrl = if (locale == "it") insect.imageIt else insect.imageEn
                    Column(
                        Modifier
                            .fillMaxSize()
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
                        ) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = insect.name,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { showZoom = true }
                            )
                        }
                    }

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
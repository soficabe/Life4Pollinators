package com.example.life4pollinators.ui.screens.insects

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
import com.example.life4pollinators.data.models.NavBarTab
import com.example.life4pollinators.ui.composables.AppBar
import com.example.life4pollinators.ui.composables.BottomNavBar
import com.example.life4pollinators.ui.composables.ZoomOverlayImage
import java.util.Locale

@Composable
fun InsectGroupInfoScreen(
    state: InsectGroupInfoState,
    isAuthenticated: Boolean,
    navController: NavHostController
) {
    val group = state.group
    val locale = Locale.getDefault().language

    // Gestione zoom overlay
    var showZoom by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppBar(
                navController,
                personalizedTitle = group?.let {
                    if (locale == "it")
                        "Info ${it.nameIt}"
                    else
                        "${it.nameEn} Info" }
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
                state.error != null -> Text(stringResource(state.error), color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                group != null -> {
                    val imageUrl = if (locale == "it") group.imageUrlIt else group.imageUrlEn
                    val imageDesc = if (locale == "it") group.nameIt else group.nameEn
                    val info = if (locale == "it") group.infoIt else group.infoEn

                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
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
                            AsyncImage(
                                model = imageUrl ?: group.groupImageUrl,
                                contentDescription = imageDesc,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable { showZoom = true },
                                contentScale = ContentScale.Fit
                            )
                        }
                        Spacer(Modifier.height(20.dp))
                        Text(
                            info ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    // Overlay zoomabile
                    if (showZoom) {
                        ZoomOverlayImage(
                            imageUrl = imageUrl ,
                            contentDescription = imageDesc,
                            onClose = { showZoom = false }
                        )
                    }
                }
            }
        }
    }
}
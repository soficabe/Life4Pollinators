package com.example.life4pollinators.ui.screens.insects

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.life4pollinators.data.models.NavBarTab
import com.example.life4pollinators.ui.composables.AppBar
import com.example.life4pollinators.ui.composables.BottomNavBar
import com.example.life4pollinators.ui.composables.InsectCard
import com.example.life4pollinators.ui.navigation.L4PRoute
import java.util.Locale

@Composable
fun InsectsListScreen(
    state: InsectsListState,
    isAuthenticated: Boolean,
    navController: NavHostController
) {
    val locale = Locale.getDefault().language
    val groupId = state.group?.id
    val groupName = state.group?.let { if (locale == "it") it.nameIt else it.nameEn } ?: ""

    Scaffold(
        topBar = { AppBar(navController, personalizedTitle = groupName) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(
                        groupId?.let { L4PRoute.InsectGroupInfo(it) } ?: ""
                    )
                }
            ) {
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
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.error != null -> Text(
                    state.error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(state.insects) { insect ->
                            InsectCard(
                                insect = insect,
                                onClick = {
                                    navController.navigate(
                                        L4PRoute.InsectDetail(insect.id)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
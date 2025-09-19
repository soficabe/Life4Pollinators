package com.example.life4pollinators.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.life4pollinators.data.models.NavBarTab
import com.example.life4pollinators.ui.composables.AppBar
import com.example.life4pollinators.ui.composables.BottomNavBar
import com.example.life4pollinators.ui.composables.SectionCard

@Composable
fun HomeScreen(
    isAuthenticated: Boolean,
    navController: NavHostController
) {

    Scaffold(
        topBar = {
            AppBar(navController)
        },
        bottomBar = {
            BottomNavBar(
                isAuthenticated = isAuthenticated,
                selectedTab = NavBarTab.Home,
                navController = navController
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Text(
                "Learn About:",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SectionCard(
                title = "Plants",
                imageRes = Icons.Outlined.Image, // usa la tua risorsa
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                onClick = { /*TODO*/ }
            )
            Spacer(Modifier.height(8.dp))
            SectionCard(
                title = "Insects",
                imageRes = Icons.Outlined.Image, // usa la tua risorsa
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                onClick = { /*TODO*/ }
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "Test your classification skills:",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SectionCard(
                title = "Plants",
                imageRes = Icons.Outlined.Image,
                backgroundColor = MaterialTheme.colorScheme.surface,
                onClick = { /*TODO*/ }
            )
            Spacer(Modifier.height(8.dp))
            SectionCard(
                title = "Insects",
                imageRes = Icons.Outlined.Image,
                backgroundColor = MaterialTheme.colorScheme.surface,
                onClick = { /*TODO*/ }
            )
        }
    }
}
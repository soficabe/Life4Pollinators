package com.example.life4pollinators.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.life4pollinators.data.models.NavBarTab
import com.example.life4pollinators.ui.composables.AppBar
import com.example.life4pollinators.ui.composables.BottomNavBar
import com.example.life4pollinators.ui.composables.ProfileStatCard
import com.example.life4pollinators.ui.composables.RankingBadge
import com.example.life4pollinators.ui.navigation.L4PRoute

@Composable
fun ProfileScreen(
    state: ProfileState,
    actions: ProfileActions,
    navController: NavHostController
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = { AppBar(navController) },
        bottomBar = { BottomNavBar(selectedTab = NavBarTab.Profile, navController) }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // Avatar migliorato ma semplice
            Surface(
                modifier = Modifier.size(110.dp),
                shape = CircleShape,
                shadowElevation = 6.dp,
                tonalElevation = 2.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.surfaceVariant
                                ),
                                radius = 110f
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "Profile image",
                        modifier = Modifier.size(54.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "@${state.user?.username ?: ""}",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(12.dp))

            // Informazioni utente più compatte
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("First Name: ${state.user?.firstName ?: ""}", style = MaterialTheme.typography.bodyMedium)
                Text("Last Name: ${state.user?.lastName ?: ""}", style = MaterialTheme.typography.bodyMedium)
                Text("Email: ${state.user?.email ?: ""}", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(24.dp))

            // Stat cards migliorate
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) {
                ProfileStatCard(
                    value = "10/39",
                    label = "Sightings",
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.weight(1f)
                )
                ProfileStatCard(
                    value = "8",
                    label = "Contributions",
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(32.dp))

            Text(
                "Ranking",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(Modifier.height(16.dp))

            // Ranking badges migliorati
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp)
            ) {
                RankingBadge(
                    period = "Day",
                    rank = 5,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                RankingBadge(
                    period = "Week",
                    rank = 2,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(32.dp))

            // Edit button migliorato
            FilledTonalButton(
                onClick = { navController.navigate(L4PRoute.EditProfile) },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .padding(horizontal = 40.dp)
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(imageVector = Icons.Outlined.Edit, contentDescription = "Edit Profile")
                Spacer(Modifier.width(8.dp))
                Text(
                    "Edit Profile",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
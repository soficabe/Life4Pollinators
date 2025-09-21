package com.example.life4pollinators.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.life4pollinators.R
import com.example.life4pollinators.data.models.NavBarTab
import com.example.life4pollinators.ui.composables.AppBar
import com.example.life4pollinators.ui.composables.BottomNavBar
import com.example.life4pollinators.ui.composables.ProfileIcon
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

    LaunchedEffect(Unit) {
        actions.refreshProfile()
    }

    Scaffold(
        topBar = { AppBar(navController) },
        bottomBar = { BottomNavBar(selectedTab = NavBarTab.Profile, navController = navController) }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            ProfileIcon(
                imageUrl = state.user?.image,
                isClickable = false,
                showLoader = state.isRefreshing
            )

            Spacer(Modifier.height(16.dp))

            Text(
                "@${state.user?.username ?: ""}",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(12.dp))

            // Informazioni utente più compatte
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("${stringResource(R.string.first_name)}: ${state.user?.firstName ?: ""}", style = MaterialTheme.typography.bodyMedium)
                Text("${stringResource(R.string.last_name)}: ${state.user?.lastName ?: ""}", style = MaterialTheme.typography.bodyMedium)
                Text("${stringResource(R.string.email)}: ${state.user?.email ?: ""}", style = MaterialTheme.typography.bodyMedium)
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
                    label = stringResource(R.string.sightings),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.weight(1f)
                )
                ProfileStatCard(
                    value = "8",
                    label = stringResource(R.string.tests_taken),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(32.dp))

            Text(
                stringResource(R.string.ranking),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
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
                    period = stringResource(R.string.day),
                    rank = 5,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                RankingBadge(
                    period = stringResource(R.string.week),
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
                    stringResource(R.string.title_edit_profile),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
package com.example.life4pollinators.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.life4pollinators.R
import com.example.life4pollinators.data.models.NavBarTab
import com.example.life4pollinators.ui.composables.AppBar
import com.example.life4pollinators.ui.composables.BottomNavBar
import com.example.life4pollinators.ui.composables.SectionCard
import com.example.life4pollinators.ui.composables.SectionCardSize
import com.example.life4pollinators.ui.navigation.L4PRoute

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
                stringResource(R.string.learn_about),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SectionCard(
                title = stringResource(R.string.plants),
                imageRes = painterResource(R.drawable.plants),
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                onClick = { navController.navigate(L4PRoute.PlantsList) },
                cardSize = SectionCardSize.Large
            )
            Spacer(Modifier.height(8.dp))
            SectionCard(
                title = stringResource(R.string.insects),
                imageRes = painterResource(R.drawable.insects),
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                onClick = { navController.navigate(L4PRoute.InsectGroupsList) },
                cardSize = SectionCardSize.Large
            )
            Spacer(Modifier.height(24.dp))
            Text(
                stringResource(R.string.test_your_classification_skills),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SectionCard(
                title = stringResource(R.string.plants),
                imageRes = painterResource(R.drawable.plants),
                backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
                onClick = { /*TODO*/ },
                cardSize = SectionCardSize.Small
            )
            Spacer(Modifier.height(8.dp))
            SectionCard(
                title = stringResource(R.string.insects),
                imageRes = painterResource(R.drawable.insects),
                backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
                onClick = { /*TODO*/ },
                cardSize = SectionCardSize.Small
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}
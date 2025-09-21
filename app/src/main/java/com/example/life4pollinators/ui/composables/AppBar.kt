package com.example.life4pollinators.ui.composables

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.life4pollinators.R
import com.example.life4pollinators.ui.navigation.L4PRoute
import com.example.life4pollinators.ui.theme.AppBarGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    navController: NavHostController
) {
    val backStackEntry by navController.currentBackStackEntryAsState()

    //Definizione del titolo dell'AppBar in base alla rotta attuale
    val title = when {
        backStackEntry?.destination?.hasRoute<L4PRoute.Home>() == true ->
            stringResource(R.string.title_learn)
        backStackEntry?.destination?.hasRoute<L4PRoute.Profile>() == true ->
            stringResource(R.string.title_profile)
        backStackEntry?.destination?.hasRoute<L4PRoute.EditProfile>() == true ->
            stringResource(R.string.title_edit_profile)
        backStackEntry?.destination?.hasRoute<L4PRoute.Settings>() == true ->
            stringResource(R.string.title_settings)
        else -> stringResource(R.string.unknown_screen)
    }

    CenterAlignedTopAppBar(
        title = {
            Text(
                title,
                fontWeight = FontWeight.Medium
            )
        },
        navigationIcon = {
            if(title != stringResource(R.string.title_learn) && navController.previousBackStackEntry != null) {
                IconButton(onClick = {navController.navigateUp()}) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Go Back")
                }
            }
        },
        actions = {
            if(title != "Settings") {
                IconButton(onClick = {navController.navigate(L4PRoute.Settings)}) {
                    Icon(Icons.Outlined.Settings, "Settings")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AppBarGreen
        )
    )
}
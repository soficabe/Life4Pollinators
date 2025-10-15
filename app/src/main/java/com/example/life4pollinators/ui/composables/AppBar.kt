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
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.life4pollinators.R
import com.example.life4pollinators.ui.navigation.L4PRoute
import com.example.life4pollinators.ui.theme.AppBarGreen

/**
 * Barra superiore dell'app (TopAppBar) allineata centralmente.
 * Mostra titolo dinamico in base alla rotta e pulsanti di navigazione/azione.
 *
 * @param navController Controller di navigazione per gestire back e navigazione a settings.
 * @param personalizedTitle Titolo personalizzato opzionale
 * @param showBackButton Se false, nasconde il bottone back anche se c'è un back stack
 * @param showSettingsButton Se false, nasconde il bottone settings
 * @param onBackClick Callback personalizzato per il pulsante back (se null usa navigateUp())
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    navController: NavHostController,
    personalizedTitle: String? = null,
    showBackButton: Boolean = true,
    showSettingsButton: Boolean = true,
    onBackClick: (() -> Unit)? = null
) {
    val backStackEntry by navController.currentBackStackEntryAsState()

    // Usa il titolo personalizzato se fornito, altrimenti quello di default
    val title = personalizedTitle ?: when {
        backStackEntry?.destination?.hasRoute<L4PRoute.Home>() == true ->
            stringResource(R.string.title_learn)
        backStackEntry?.destination?.hasRoute<L4PRoute.Profile>() == true ->
            stringResource(R.string.title_profile)
        backStackEntry?.destination?.hasRoute<L4PRoute.EditProfile>() == true ->
            stringResource(R.string.title_edit_profile)
        backStackEntry?.destination?.hasRoute<L4PRoute.Settings>() == true ->
            stringResource(R.string.title_settings)
        backStackEntry?.destination?.hasRoute<L4PRoute.PlantsList>() == true ->
            stringResource(R.string.title_plants_list)
        backStackEntry?.destination?.hasRoute<L4PRoute.PlantsGeneralInfo>() == true ->
            stringResource(R.string.title_plants_general_info)
        backStackEntry?.destination?.hasRoute<L4PRoute.InsectGroupsList>() == true ->
            stringResource(R.string.title_insect_groups)
        backStackEntry?.destination?.hasRoute<L4PRoute.InsectsGeneralInfo>() == true ->
            stringResource(R.string.title_insects_general_info)
        backStackEntry?.destination?.hasRoute<L4PRoute.QuizQuestion>() == true ->
            stringResource(R.string.title_quiz_question)
        backStackEntry?.destination?.hasRoute<L4PRoute.QuizInsectTypeSelection>() == true ->
            stringResource(R.string.title_quiz_question)
        backStackEntry?.destination?.hasRoute<L4PRoute.QuizInsectsList>() == true ->
            stringResource(R.string.title_quiz_question)
        backStackEntry?.destination?.hasRoute<L4PRoute.QuizTargetSelection>() == true ->
            stringResource(R.string.title_quiz_question)
        backStackEntry?.destination?.hasRoute<L4PRoute.QuizResult>() == true ->
            stringResource(R.string.title_quiz_result)
        backStackEntry?.destination?.hasRoute<L4PRoute.AddSighting>() == true ->
            stringResource(R.string.title_add_sighting)
        backStackEntry?.destination?.hasRoute<L4PRoute.Sightings>() == true ->
            stringResource(R.string.title_sightings)
        else -> stringResource(R.string.unknown_screen)
    }

    CenterAlignedTopAppBar(
        title = {
            Text(
                title,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        },
        navigationIcon = {
            if(showBackButton && title != stringResource(R.string.title_learn) && navController.previousBackStackEntry != null) {
                IconButton(onClick = {
                    if (onBackClick != null) {
                        onBackClick()
                    } else {
                        navController.navigateUp()
                    }
                }) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Go Back")
                }
            }
        },
        actions = {
            if(showSettingsButton && title != stringResource(R.string.title_settings)) {
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
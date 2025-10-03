package com.example.life4pollinators.ui.composables

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.example.life4pollinators.R
import com.example.life4pollinators.data.models.NavBarTab
import com.example.life4pollinators.ui.navigation.L4PRoute

/**
 * Barra di navigazione inferiore con le principali sezioni dell'app.
 *
 * @param isAuthenticated Indica se l'utente è autenticato (serve per schermate protette).
 * @param selectedTab Tab attualmente selezionato.
 * @param navController Controller di navigazione.
 */
@Composable
fun BottomNavBar(
    isAuthenticated: Boolean = true,
    selectedTab: NavBarTab = NavBarTab.None,
    navController: NavHostController
) {
    NavigationBar {
        // Tab "Learn"
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = "Learn") },
            label = { Text(stringResource(R.string.learn)) },
            selected = selectedTab == NavBarTab.Home,
            onClick = { navController.navigate(L4PRoute.Home) }
        )
        // Tab "Sightings"
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Visibility, contentDescription = "Sightings") },
            label = { Text(stringResource(R.string.sightings)) },
            selected = false,
            onClick = {
                if(isAuthenticated)
                    navController.navigate(L4PRoute.Home) // TODO: cambiare con schermata avvistamenti
                else
                    navController.navigate(L4PRoute.SignIn)
            }
        )
        // Tab "Add Sighting"
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.AddCircle, contentDescription = "Add Sighting") },
            label = { Text(stringResource(R.string.add_sighting)) },
            selected = false,
            onClick = {
                if(isAuthenticated)
                    navController.navigate(L4PRoute.AddSighting)
                else
                    navController.navigate(L4PRoute.SignIn)
            }
        )
        // Tab "Profile"
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Person, contentDescription = "User Profile") },
            label = { Text(stringResource(R.string.user_profile)) },
            selected = selectedTab == NavBarTab.Profile,
            onClick = {
                if(isAuthenticated)
                    navController.navigate(L4PRoute.Profile)
                else
                    navController.navigate(L4PRoute.SignIn)
            }
        )
    }
}
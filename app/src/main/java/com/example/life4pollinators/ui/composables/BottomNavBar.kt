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

@Composable
fun BottomNavBar(
    isAuthenticated: Boolean = true,
    selectedTab: NavBarTab = NavBarTab.None,
    navController: NavHostController
) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = "Learn") },
            label = { Text(stringResource(R.string.learn)) },
            selected = selectedTab == NavBarTab.Home,
            onClick = { navController.navigate(L4PRoute.Home) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Visibility, contentDescription = "Sightings") },
            label = { Text(stringResource(R.string.sightings)) },
            selected = false,
            onClick = {
                if(isAuthenticated)
                    navController.navigate(L4PRoute.Home) //poi sarà la vera schermata
                else
                    navController.navigate(L4PRoute.SignIn)
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.AddCircle, contentDescription = "Add Sighting") },
            label = { Text(stringResource(R.string.add_sighting)) },
            selected = false,
            onClick = {
                if(isAuthenticated)
                    navController.navigate(L4PRoute.Home) //poi sarà la vera schermata
                else
                    navController.navigate(L4PRoute.SignIn)
            }
        )
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
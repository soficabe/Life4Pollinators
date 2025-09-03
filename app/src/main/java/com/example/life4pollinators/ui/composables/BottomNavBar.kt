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
import androidx.navigation.NavHostController
import com.example.life4pollinators.data.models.NavBarTab
import com.example.life4pollinators.ui.navigation.L4PRoute

@Composable
fun BottomNavBar(
    selectedTab: NavBarTab,
    navController: NavHostController
) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = "Learn") },
            label = { Text("Learn") },
            selected = selectedTab == NavBarTab.Home,
            onClick = { navController.navigate(L4PRoute.Home) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Visibility, contentDescription = "Sightings") },
            label = { Text("Sightings") },
            selected = false,
            onClick = { /*TODO*/ }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.AddCircle, contentDescription = "Add Sighting") },
            label = { Text("Add Sighting") },
            selected = false,
            onClick = { /*TODO*/ }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Person, contentDescription = "User Profile") },
            label = { Text("User Profile") },
            selected = selectedTab == NavBarTab.Profile,
            onClick = { navController.navigate(L4PRoute.Settings) }
        )
    }
}
package com.example.life4pollinators.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.life4pollinators.ui.screens.editProfile.EdiProfileScreen
import com.example.life4pollinators.ui.screens.home.HomeScreen
import com.example.life4pollinators.ui.screens.profile.ProfileScreen
import com.example.life4pollinators.ui.screens.settings.SettingsScreen
import com.example.life4pollinators.ui.screens.signIn.SignInScreen
import com.example.life4pollinators.ui.screens.signUp.SignUpScreen
import kotlinx.serialization.Serializable

/**
 * Per avere una navigation type-safe identifichiamo ogni schermata
 * con un oggetto
 */
sealed interface L4PRoute {
    @Serializable
    data object SignUp : L4PRoute

    @Serializable
    data object SignIn : L4PRoute

    @Serializable
    data object Home : L4PRoute

    @Serializable
    data object Settings : L4PRoute

    @Serializable
    data object Profile : L4PRoute

    @Serializable
    data object EditProfile : L4PRoute
}

/**
 * Grafo di navigazione: definisce le destinazioni raggiungibili tramite
 * navigazione all’interno dell’app, indicando per ciascuna il composable
 * che ne contiene la UI
 */
@Composable
fun L4PNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
){
    //Composable utilizzato per l'implementazione vera e propria del grafico di navigazione
    NavHost(
        navController = navController,
        startDestination = L4PRoute.Home,
        modifier = modifier
    ){
        composable<L4PRoute.SignUp> {
            SignUpScreen(navController)
        }

        composable<L4PRoute.SignIn> {
            SignInScreen(navController)
        }

        composable<L4PRoute.Home> {
            HomeScreen(navController)
        }

        composable<L4PRoute.Settings> {
            SettingsScreen(navController)
        }

        composable<L4PRoute.Profile> {
            ProfileScreen(navController)
        }

        composable<L4PRoute.EditProfile> {
            EdiProfileScreen(navController)
        }
    }
}

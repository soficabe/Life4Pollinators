package com.example.life4pollinators.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.life4pollinators.ui.screens.editProfile.EditProfileScreen
import com.example.life4pollinators.ui.screens.editProfile.EditProfileViewModel
import com.example.life4pollinators.ui.screens.home.HomeScreen
import com.example.life4pollinators.ui.screens.profile.ProfileScreen
import com.example.life4pollinators.ui.screens.profile.ProfileViewModel
import com.example.life4pollinators.ui.screens.settings.SettingsScreen
import com.example.life4pollinators.ui.screens.settings.SettingsState
import com.example.life4pollinators.ui.screens.settings.SettingsViewModel
import com.example.life4pollinators.ui.screens.signIn.SignInScreen
import com.example.life4pollinators.ui.screens.signIn.SignInViewModel
import com.example.life4pollinators.ui.screens.signUp.SignUpScreen
import com.example.life4pollinators.ui.screens.signUp.SignUpViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

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
    modifier: Modifier = Modifier,
    navController: NavHostController,
    settingsViewModel: SettingsViewModel,
    settingsState: SettingsState
){
    //Composable utilizzato per l'implementazione vera e propria del grafico di navigazione
    NavHost(
        navController = navController,
        startDestination = L4PRoute.Home,
        modifier = modifier
    ){
        composable<L4PRoute.SignUp> {
            val signUpVM = koinViewModel<SignUpViewModel>()
            val signUpState by signUpVM.state.collectAsStateWithLifecycle()
            SignUpScreen(signUpState, signUpVM.actions, navController)
        }

        composable<L4PRoute.SignIn> {
            val signInVM = koinViewModel<SignInViewModel>()
            val signInState by signInVM.state.collectAsStateWithLifecycle()
            SignInScreen(signInState, signInVM.actions, navController)
        }

        composable<L4PRoute.Home> {
            HomeScreen(navController)
        }

        composable<L4PRoute.Settings> {
            SettingsScreen(settingsState, settingsViewModel.actions, navController)
        }

        composable<L4PRoute.Profile> {
            val profileVM = koinViewModel<ProfileViewModel>()
            val profileState by profileVM.state.collectAsStateWithLifecycle()
            ProfileScreen(profileState, profileVM.actions, navController)
        }

        composable<L4PRoute.EditProfile> {
            val editProfileVM = koinViewModel<EditProfileViewModel>()
            val editProfileState by editProfileVM.state.collectAsStateWithLifecycle()
            EditProfileScreen(editProfileState, editProfileVM.actions, navController)
        }
    }
}

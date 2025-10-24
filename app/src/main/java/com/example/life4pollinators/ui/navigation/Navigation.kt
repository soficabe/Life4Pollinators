package com.example.life4pollinators.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.life4pollinators.ui.AuthViewModel
import com.example.life4pollinators.ui.screens.addSighting.AddSightingViewModel
import com.example.life4pollinators.ui.screens.editProfile.EditProfileScreen
import com.example.life4pollinators.ui.screens.editProfile.EditProfileViewModel
import com.example.life4pollinators.ui.screens.home.HomeScreen
import com.example.life4pollinators.ui.screens.insects.InsectDetailScreen
import com.example.life4pollinators.ui.screens.insects.InsectDetailViewModel
import com.example.life4pollinators.ui.screens.insects.InsectGroupInfoScreen
import com.example.life4pollinators.ui.screens.insects.InsectGroupInfoViewModel
import com.example.life4pollinators.ui.screens.insects.InsectGroupsListScreen
import com.example.life4pollinators.ui.screens.insects.InsectGroupsListViewModel
import com.example.life4pollinators.ui.screens.insects.InsectsGeneralInfoScreen
import com.example.life4pollinators.ui.screens.insects.InsectsListScreen
import com.example.life4pollinators.ui.screens.insects.InsectsListViewModel
import com.example.life4pollinators.ui.screens.plants.PlantDetailScreen
import com.example.life4pollinators.ui.screens.plants.PlantDetailViewModel
import com.example.life4pollinators.ui.screens.plants.PlantGeneralInfoScreen
import com.example.life4pollinators.ui.screens.plants.PlantsGeneralInfoViewModel
import com.example.life4pollinators.ui.screens.plants.PlantsListScreen
import com.example.life4pollinators.ui.screens.plants.PlantsListViewModel
import com.example.life4pollinators.ui.screens.profile.ProfileScreen
import com.example.life4pollinators.ui.screens.profile.ProfileViewModel
import com.example.life4pollinators.ui.screens.quiz.QuizInsectTypeSelectionScreen
import com.example.life4pollinators.ui.screens.quiz.QuizInsectsListScreen
import com.example.life4pollinators.ui.screens.quiz.QuizQuestionScreen
import com.example.life4pollinators.ui.screens.quiz.QuizResultScreen
import com.example.life4pollinators.ui.screens.quiz.QuizStartScreen
import com.example.life4pollinators.ui.screens.quiz.QuizTargetSelectionScreen
import com.example.life4pollinators.ui.screens.quiz.QuizViewModel
import com.example.life4pollinators.ui.screens.settings.SettingsScreen
import com.example.life4pollinators.ui.screens.settings.SettingsState
import com.example.life4pollinators.ui.screens.settings.SettingsViewModel
import com.example.life4pollinators.ui.screens.addSighting.AddSightingScreen
import com.example.life4pollinators.ui.screens.leaderboard.LeaderboardScreen
import com.example.life4pollinators.ui.screens.leaderboard.LeaderboardViewModel
import com.example.life4pollinators.ui.screens.sightings.SightingsScreen
import com.example.life4pollinators.ui.screens.sightings.SightingsViewModel
import com.example.life4pollinators.ui.screens.signIn.SignInScreen
import com.example.life4pollinators.ui.screens.signIn.SignInViewModel
import com.example.life4pollinators.ui.screens.signUp.SignUpScreen
import com.example.life4pollinators.ui.screens.signUp.SignUpViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

/**
 * Rappresenta tutte le possibili destinazioni della navigation dell'app.
 * Ogni schermata ha un oggetto dedicato per una navigation type-safe.
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
    data object PlantsList : L4PRoute

    @Serializable
    data object PlantsGeneralInfo : L4PRoute

    @Serializable
    data class PlantDetail(val plantId: String) : L4PRoute

    @Serializable
    data object InsectGroupsList : L4PRoute

    @Serializable
    data object InsectsGeneralInfo: L4PRoute

    @Serializable
    data class InsectsList(val groupId: String) : L4PRoute

    @Serializable
    data class InsectGroupInfo(val groupId: String) : L4PRoute

    @Serializable
    data class InsectDetail(val insectId: String) : L4PRoute

    @Serializable
    data object QuizInsectTypeSelection : L4PRoute

    @Serializable
    data object QuizInsectsList : L4PRoute

    @Serializable
    data object QuizQuestion : L4PRoute

    @Serializable
    data object QuizTargetSelection : L4PRoute

    @Serializable
    data object QuizResult : L4PRoute

    @Serializable
    data object Profile : L4PRoute

    @Serializable
    data object EditProfile : L4PRoute

    @Serializable
    data object AddSighting : L4PRoute

    @Serializable
    data object Sightings : L4PRoute

    @Serializable
    data object Leaderboard : L4PRoute
}

/**
 * Definisce il grafo di navigazione dell'app.
 * Associa a ogni destinazione la composable screen corrispondente.
 *
 * @param modifier Modifier Compose opzionale.
 * @param navController Controller di navigazione principale.
 * @param settingsViewModel ViewModel per la schermata Settings.
 * @param settingsState Stato della schermata Settings.
 */
@Composable
fun L4PNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    settingsViewModel: SettingsViewModel,
    settingsState: SettingsState
){
    // ViewModel per l'autenticazione, usato per gestire l'accesso globale
    val authViewModel = koinViewModel<AuthViewModel>()
    val isAuthenticated by authViewModel.isAuthenticated.collectAsStateWithLifecycle()
    val userId by authViewModel.userId.collectAsStateWithLifecycle()

    // Crea il QuizViewModel qui per condividerlo tra tutte le schermate del quiz
    val quizViewModel = koinViewModel<QuizViewModel>()

    //Composable utilizzato per l'implementazione vera e propria del grafico di navigazione
    // Definisce le schermate raggiungibili tramite navigation
    NavHost(
        navController = navController,
        startDestination = L4PRoute.Home,
        modifier = modifier
    ){
        // Schermata di registrazione
        composable<L4PRoute.SignUp> {
            val signUpVM = koinViewModel<SignUpViewModel>()
            val signUpState by signUpVM.state.collectAsStateWithLifecycle()
            SignUpScreen(signUpState, signUpVM.actions, navController)
        }

        // Schermata di login
        composable<L4PRoute.SignIn> {
            val signInVM = koinViewModel<SignInViewModel>()
            val signInState by signInVM.state.collectAsStateWithLifecycle()
            SignInScreen(signInState, signInVM.actions, navController)
        }

        // Schermata principale Learn (home)
        composable<L4PRoute.Home> {
            HomeScreen(isAuthenticated, navController)
        }

        // Schermata impostazioni
        composable<L4PRoute.Settings> {
            SettingsScreen(settingsState, settingsViewModel.actions, isAuthenticated, navController)
        }

        // Schermata lista delle piante visibili nella parte didattica
        composable<L4PRoute.PlantsList> {
            val plantsListVM = koinViewModel<PlantsListViewModel>()
            val plantsListState by plantsListVM.state.collectAsStateWithLifecycle()
            PlantsListScreen(plantsListState, isAuthenticated, navController)
        }

        // Schermate info generali sulle piante
        composable<L4PRoute.PlantsGeneralInfo> {
            val plantsGeneralInfoVM = koinViewModel<PlantsGeneralInfoViewModel>()
            val plantsGeneralInfoState by plantsGeneralInfoVM.state.collectAsStateWithLifecycle()
            PlantGeneralInfoScreen(plantsGeneralInfoState, isAuthenticated, navController)
        }

        // Schermata di dettaglio di una certa specie di pianta
        composable<L4PRoute.PlantDetail> {
            val plantDetailViewModel = koinViewModel<PlantDetailViewModel>()
            val plantDetailState by plantDetailViewModel.state.collectAsStateWithLifecycle()
            PlantDetailScreen(plantDetailState, isAuthenticated, navController)
        }

        // Schermata lista dei gruppi di insetti consultabili nella parte didattica
        composable<L4PRoute.InsectGroupsList> {
            val insectGroupsListVM = koinViewModel<InsectGroupsListViewModel>()
            val insectGroupsListState by insectGroupsListVM.state.collectAsStateWithLifecycle()
            InsectGroupsListScreen(insectGroupsListState, isAuthenticated, navController)
        }

        // Schermata info generali sugli insetti
        composable<L4PRoute.InsectsGeneralInfo> {
            InsectsGeneralInfoScreen(isAuthenticated, navController)
        }

        // Schermata lista delle specie di insetti di un certo gruppo
        composable<L4PRoute.InsectsList> {
            val insectsListVM = koinViewModel<InsectsListViewModel>()
            val insectsListState by insectsListVM.state.collectAsStateWithLifecycle()
            InsectsListScreen(insectsListState, isAuthenticated, navController)
        }

        // Schermata info di un certo gruppo di insetti
        composable<L4PRoute.InsectGroupInfo> {
            val insectGroupInfoVM = koinViewModel<InsectGroupInfoViewModel>()
            val insectGroupInfoState by insectGroupInfoVM.state.collectAsStateWithLifecycle()
            InsectGroupInfoScreen(insectGroupInfoState, isAuthenticated, navController)
        }

        // Schermata di dettaglio di una certa specie di insetto
        composable<L4PRoute.InsectDetail> {
            val insectDetailVM = koinViewModel<InsectDetailViewModel>()
            val insectDetailState by insectDetailVM.state.collectAsStateWithLifecycle()
            InsectDetailScreen(insectDetailState, isAuthenticated, navController)
        }

        // Schermata Quiz Start
        composable("quizStart/{type}") { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: ""
            LaunchedEffect(type) {
                quizViewModel.actions.setQuizType(type)
            }
            val quizState by quizViewModel.state.collectAsStateWithLifecycle()
            QuizStartScreen(quizState, quizViewModel.actions, isAuthenticated, navController)
        }

        // Schermata di selezione del gruppo di insetto di cui si vuole fare il quiz
        composable<L4PRoute.QuizInsectTypeSelection> {
            val quizState by quizViewModel.state.collectAsStateWithLifecycle()
            QuizInsectTypeSelectionScreen(quizState, quizViewModel.actions, navController)
        }

        // Schermata di selezione della specie di insetto nel quiz
        composable<L4PRoute.QuizInsectsList> {
            val quizState by quizViewModel.state.collectAsStateWithLifecycle()
            QuizInsectsListScreen(quizState, quizViewModel.actions, navController)
        }

        // Schermata Quiz Question
        composable<L4PRoute.QuizQuestion> {
            val quizState by quizViewModel.state.collectAsStateWithLifecycle()
            QuizQuestionScreen(quizState, quizViewModel.actions, navController)
        }

        // Schermata di selezione del target (specie) nel quiz
        composable<L4PRoute.QuizTargetSelection> {
            val quizState by quizViewModel.state.collectAsStateWithLifecycle()
            QuizTargetSelectionScreen(quizState, quizViewModel.actions, navController)
        }

        // Schermata Quiz Result
        composable<L4PRoute.QuizResult> {
            val quizState by quizViewModel.state.collectAsStateWithLifecycle()
            QuizResultScreen(quizState, quizViewModel.actions, isAuthenticated, userId ?: "", navController)
        }

        // Schermata profilo utente
        composable<L4PRoute.Profile> {
            val profileVM = koinViewModel<ProfileViewModel>()
            val profileState by profileVM.state.collectAsStateWithLifecycle()
            ProfileScreen(profileState, profileVM.actions, navController)
        }

        // Schermata modifica profilo
        composable<L4PRoute.EditProfile> {
            val editProfileVM = koinViewModel<EditProfileViewModel>()
            val editProfileState by editProfileVM.state.collectAsStateWithLifecycle()
            EditProfileScreen(editProfileState, editProfileVM.actions, navController)
        }

        // Schermata form di caricamento avvistamento
        composable<L4PRoute.AddSighting> {
            val addSightingVM = koinViewModel<AddSightingViewModel>()
            val addSightingState by addSightingVM.state.collectAsStateWithLifecycle()
            AddSightingScreen(
                state = addSightingState,
                actions = addSightingVM.actions,
                userId = userId ?: "",
                navController = navController
            )
        }

        // Schermata collezione di avvistamenti
        composable<L4PRoute.Sightings> {
            val sightingsVM = koinViewModel<SightingsViewModel>()
            val sightingsState by sightingsVM.state.collectAsStateWithLifecycle()
            SightingsScreen(
                state = sightingsState,
                actions = sightingsVM.actions,
                userId = userId ?: "",
                navController = navController
            )
        }

        // Schermata classifica utenti
        composable<L4PRoute.Leaderboard> {
            val leaderboardVM = koinViewModel<LeaderboardViewModel>()
            val leaderboardState by leaderboardVM.state.collectAsStateWithLifecycle()
            LeaderboardScreen(leaderboardState, leaderboardVM.actions, navController)
        }
    }
}
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
import com.example.life4pollinators.ui.screens.signIn.SignInScreen
import com.example.life4pollinators.ui.screens.signIn.SignInViewModel
import com.example.life4pollinators.ui.screens.signUp.SignUpScreen
import com.example.life4pollinators.ui.screens.signUp.SignUpViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

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
}

@Composable
fun L4PNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    settingsViewModel: SettingsViewModel,
    settingsState: SettingsState
){
    val authViewModel = koinViewModel<AuthViewModel>()
    val isAuthenticated by authViewModel.isAuthenticated.collectAsStateWithLifecycle()

    // Crea il QuizViewModel qui per condividerlo tra tutte le schermate del quiz
    val quizViewModel = koinViewModel<QuizViewModel>()

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
            HomeScreen(isAuthenticated, navController)
        }

        composable<L4PRoute.Settings> {
            SettingsScreen(settingsState, settingsViewModel.actions, isAuthenticated, navController)
        }

        composable<L4PRoute.PlantsList> {
            val plantsListVM = koinViewModel<PlantsListViewModel>()
            val plantsListState by plantsListVM.state.collectAsStateWithLifecycle()
            PlantsListScreen(plantsListState, isAuthenticated, navController)
        }

        composable<L4PRoute.PlantsGeneralInfo> {
            val plantsGeneralInfoVM = koinViewModel<PlantsGeneralInfoViewModel>()
            val plantsGeneralInfoState by plantsGeneralInfoVM.state.collectAsStateWithLifecycle()
            PlantGeneralInfoScreen(plantsGeneralInfoState, isAuthenticated, navController)
        }

        composable<L4PRoute.PlantDetail> {
            val plantDetailViewModel = koinViewModel<PlantDetailViewModel>()
            val plantDetailState by plantDetailViewModel.state.collectAsStateWithLifecycle()
            PlantDetailScreen(plantDetailState, isAuthenticated, navController)
        }

        composable<L4PRoute.InsectGroupsList> {
            val insectGroupsListVM = koinViewModel<InsectGroupsListViewModel>()
            val insectGroupsListState by insectGroupsListVM.state.collectAsStateWithLifecycle()
            InsectGroupsListScreen(insectGroupsListState, isAuthenticated, navController)
        }

        composable<L4PRoute.InsectsGeneralInfo> {
            InsectsGeneralInfoScreen(isAuthenticated, navController)
        }

        composable<L4PRoute.InsectsList> {
            val insectsListVM = koinViewModel<InsectsListViewModel>()
            val insectsListState by insectsListVM.state.collectAsStateWithLifecycle()
            InsectsListScreen(insectsListState, isAuthenticated, navController)
        }

        composable<L4PRoute.InsectGroupInfo> {
            val insectGroupInfoVM = koinViewModel<InsectGroupInfoViewModel>()
            val insectGroupInfoState by insectGroupInfoVM.state.collectAsStateWithLifecycle()
            InsectGroupInfoScreen(insectGroupInfoState, isAuthenticated, navController)
        }

        composable<L4PRoute.InsectDetail> {
            val insectDetailVM = koinViewModel<InsectDetailViewModel>()
            val insectDetailState by insectDetailVM.state.collectAsStateWithLifecycle()
            InsectDetailScreen(insectDetailState, isAuthenticated, navController)
        }

        // Quiz Routes
        composable("quizStart/{type}") { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "plant"
            LaunchedEffect(type) {
                quizViewModel.actions.setQuizType(type)
            }
            val quizState by quizViewModel.state.collectAsStateWithLifecycle()
            QuizStartScreen(quizState, quizViewModel.actions, isAuthenticated, navController)
        }

        composable<L4PRoute.QuizInsectTypeSelection> {
            val quizState by quizViewModel.state.collectAsStateWithLifecycle()
            QuizInsectTypeSelectionScreen(quizState, quizViewModel.actions, isAuthenticated, navController)
        }

        composable<L4PRoute.QuizInsectsList> {
            val quizState by quizViewModel.state.collectAsStateWithLifecycle()
            QuizInsectsListScreen(quizState, quizViewModel.actions, isAuthenticated, navController)
        }

        composable<L4PRoute.QuizQuestion> {
            val quizState by quizViewModel.state.collectAsStateWithLifecycle()
            QuizQuestionScreen(quizState, quizViewModel.actions, isAuthenticated, navController)
        }

        composable<L4PRoute.QuizTargetSelection> {
            val quizState by quizViewModel.state.collectAsStateWithLifecycle()
            QuizTargetSelectionScreen(quizState, quizViewModel.actions, isAuthenticated, navController)
        }

        composable<L4PRoute.QuizResult> {
            val quizState by quizViewModel.state.collectAsStateWithLifecycle()
            QuizResultScreen(quizState, quizViewModel.actions, isAuthenticated, navController)
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
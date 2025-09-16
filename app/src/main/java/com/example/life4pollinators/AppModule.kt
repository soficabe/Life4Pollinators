package com.example.life4pollinators

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.example.life4pollinators.data.repositories.AuthRepository
import com.example.life4pollinators.data.repositories.SettingsRepository
import com.example.life4pollinators.data.repositories.UserRepository
import com.example.life4pollinators.ui.screens.editProfile.EditProfileViewModel
import com.example.life4pollinators.ui.screens.profile.ProfileViewModel
import com.example.life4pollinators.ui.screens.settings.SettingsViewModel
import com.example.life4pollinators.ui.screens.signIn.SignInViewModel
import com.example.life4pollinators.ui.screens.signUp.SignUpViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.composeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val Context.dataStore by preferencesDataStore("preferences")

val appModule = module {
    single { get<Context>().dataStore }

    single {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth) {
                flowType = FlowType.PKCE
                scheme = "app"
                host = "supabase.com"
            }
            install(ComposeAuth) {
                googleNativeLogin(BuildConfig.GOOGLE_CLIENT_ID)
            }
            install(Postgrest)
            install(Storage)
        }
    }

    single { get<SupabaseClient>().auth }
    single { get<SupabaseClient>().composeAuth }
    single { get<SupabaseClient>().postgrest }
    single { get<SupabaseClient>().storage }

    //Repositories
    single { AuthRepository(get(), get()) }
    single { UserRepository(get()) }
    single { SettingsRepository(get()) }

    //ViewModels
    viewModel { SettingsViewModel(get(), get()) }
    viewModel { SignUpViewModel(get()) }
    viewModel { SignInViewModel(get()) }
    viewModel { ProfileViewModel(get(), get()) }
    viewModel { EditProfileViewModel(get(), get()) }
}
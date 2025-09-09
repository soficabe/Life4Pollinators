package com.example.life4pollinators

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.example.life4pollinators.data.repositories.SettingsRepository
import com.example.life4pollinators.ui.screens.settings.SettingsViewModel
import com.example.life4pollinators.ui.screens.signIn.SignInViewModel
import com.example.life4pollinators.ui.screens.signUp.SignUpViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val Context.dataStore by preferencesDataStore("preferences")

val appModule = module {
    single { get<Context>().dataStore }

    //Repositories
    single { SettingsRepository(get()) }

    //ViewModels
    viewModel { SettingsViewModel(get()) }
    viewModel { SignUpViewModel() }
    viewModel { SignInViewModel() }
}
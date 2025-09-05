package com.example.life4pollinators

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.life4pollinators.data.models.Theme
import com.example.life4pollinators.ui.navigation.L4PNavGraph
import com.example.life4pollinators.ui.screens.settings.SettingsViewModel
import com.example.life4pollinators.ui.theme.Life4PollinatorsTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel = koinViewModel<SettingsViewModel>()
            val settingsState by settingsViewModel.state.collectAsStateWithLifecycle()

            Life4PollinatorsTheme (
                darkTheme = when (settingsState.theme) {
                    Theme.Light -> false
                    Theme.Dark -> true
                    Theme.System -> isSystemInDarkTheme()
                }
            ) {
                /**
                 * Il NavController è l'oggetto che gestisce la navigazione.
                 * Viene passato al NavGraph per tenere traccia della destinazione attuale
                 * e per fornire i metodi per muoversi tra le destinazioni definite nel grafo stesso.
                 */
                val navController = rememberNavController()
                L4PNavGraph(
                    navController = navController,
                    settingsViewModel = settingsViewModel,
                    settingsState = settingsState
                )
            }
        }
    }
}

package com.example.life4pollinators

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.life4pollinators.ui.navigation.L4PNavGraph
import com.example.life4pollinators.ui.theme.Life4PollinatorsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Life4PollinatorsTheme {
                /**
                 * Il NavController è l'oggetto che gestisce la navigazione.
                 * Viene passato al NavGraph per tenere traccia della destinazione attuale
                 * e per fornire i metodi per muoversi tra le destinazioni definite nel grafo stesso.
                 */
                val navController = rememberNavController()
                L4PNavGraph(navController)
            }
        }
    }
}

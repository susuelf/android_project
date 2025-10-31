package com.progress.habittracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.progress.habittracker.navigation.NavGraph
import com.progress.habittracker.ui.theme.Progr3SSTheme

/**
 * MainActivity - Az alkalmazás fő belépési pontja
 * 
 * Ez az Activity felelős az alkalmazás indításáért és a Jetpack Compose UI inicializálásáért.
 * Beállítja a Navigation Compose-t és a fő témát.
 */
class MainActivity : ComponentActivity() {
    /**
     * onCreate - Az Activity létrehozásakor hívódik meg
     * 
     * Itt inicializáljuk a Navigation Controller-t és a NavGraph-ot.
     * 
     * @param savedInstanceState Az Activity korábbi állapota, ha van ilyen
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Edge-to-edge megjelenítés engedélyezése (teljes képernyős layout)
        enableEdgeToEdge()
        
        // Compose UI tartalom beállítása
        setContent {
            // Téma alkalmazása
            Progr3SSTheme {
                // NavController létrehozása - ez kezeli a navigációt
                val navController = rememberNavController()
                
                // Scaffold: Material 3 alapvető layout struktúra
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    // NavGraph: Navigációs gráf, ami összeköti a képernyőket
                    NavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

package com.progress.habittracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.progress.habittracker.ui.theme.Progr3SSTheme

/**
 * MainActivity - Az alkalmazás fő belépési pontja
 * 
 * Ez az Activity felelős az alkalmazás indításáért és a Jetpack Compose UI inicializálásáért.
 */
class MainActivity : ComponentActivity() {
    /**
     * onCreate - Az Activity létrehozásakor hívódik meg
     * 
     * @param savedInstanceState Az Activity korábbi állapota, ha van ilyen
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Edge-to-edge megjelenítés engedélyezése (teljes képernyős layout)
        enableEdgeToEdge()
        
        // Compose UI tartalom beállítása
        setContent {
            Progr3SSTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Progr3SS",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

/**
 * Greeting - Egyszerű üdvözlő komponens
 * 
 * @param name A megjelenítendő név az üdvözlésben
 * @param modifier Modifier a komponens testreszabásához
 */
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

/**
 * GreetingPreview - Előnézet a Greeting komponenshez
 * 
 * Ez a preview lehetővé teszi a komponens megtekintését az Android Studio-ban
 * anélkül, hogy az alkalmazást futtatni kellene.
 */
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Progr3SSTheme {
        Greeting("Progr3SS")
    }
}

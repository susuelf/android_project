package com.progress.habittracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.progress.habittracker.ui.theme.Progr3SSTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Az alkalmazás fő Activity-je.
 * 
 * @AndroidEntryPoint annotáció lehetővé teszi a Hilt dependency injection használatát
 * ebben az Activity-ben.
 * 
 * Ez az Activity inicializálja a Jetpack Compose UI-t és kezeli
 * az alkalmazás általános életciklusát.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-to-edge kijelző használata (modern Android design)
        enableEdgeToEdge()
        
        // Jetpack Compose tartalom beállítása
        setContent {
            Progr3SSTheme {
                // Scaffold biztosítja a Material Design 3 alapstruktúrát
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // Üdvözlő szöveg - ez lesz később lecserélve a navigation graph-ra
                        Greeting("Progr3SS")
                    }
                }
            }
        }
    }
}

/**
 * Egyszerű üdvözlő szöveg komponens.
 * 
 * @param name A megjelenítendő név
 * @param modifier Modifier a komponens testreszabásához
 */
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Welcome to $name!",
        modifier = modifier,
        style = MaterialTheme.typography.headlineMedium
    )
}

/**
 * Preview funkció a Greeting komponens előnézetéhez Android Studio-ban.
 * 
 * Lehetővé teszi a komponens megjelenítését az Android Studio Design panelében,
 * anélkül, hogy futtatni kellene az alkalmazást.
 */
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Progr3SSTheme {
        Greeting("Progr3SS")
    }
}

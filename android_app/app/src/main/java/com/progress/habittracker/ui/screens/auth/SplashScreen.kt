package com.progress.habittracker.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.progress.habittracker.navigation.Screen
import com.progress.habittracker.ui.theme.Progr3SSTheme
import com.progress.habittracker.ui.viewmodel.AuthViewModel
import com.progress.habittracker.ui.viewmodel.AuthViewModelFactory
import kotlinx.coroutines.delay

/**
 * SplashScreen - Induló képernyő
 *
 * Ez a képernyő jelenik meg először az alkalmazás indításakor.
 * Célja, hogy betöltse a szükséges adatokat és eldöntse, hova kell navigálni a felhasználót.
 *
 * Főbb funkciók:
 * - Megjeleníti az alkalmazás logóját és nevét (Branding).
 * - Automatikus bejelentkezés ellenőrzése: Megvizsgálja, hogy van-e érvényes token elmentve.
 * - Navigáció:
 *   - Ha van érvényes token -> Home Screen (Főképernyő).
 *   - Ha nincs érvényes token -> Login Screen (Bejelentkezés).
 *
 * @param navController A navigációért felelős vezérlő.
 * @param viewModel Az autentikációs logikát kezelő ViewModel, amely ellenőrzi a bejelentkezési státuszt.
 */
@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(LocalContext.current)
    )
) {
    // LaunchedEffect: Ez a blokk akkor fut le, amikor a Composable először megjelenik a képernyőn.
    // A 'Unit' paraméter miatt csak egyszer fut le.
    LaunchedEffect(Unit) {
        // Kis késleltetés (1.5 másodperc), hogy a felhasználó láthassa a logót és az animációt.
        // Ez ad egy "betöltés" érzetet és nem villan át azonnal a következő képernyőre.
        delay(1500)
        
        // Auto-login ellenőrzés a ViewModel segítségével.
        // A checkAutoLogin metódus megnézi a TokenManager-ben tárolt tokent.
        viewModel.checkAutoLogin { isLoggedIn ->
            if (isLoggedIn) {
                // Ha van érvényes token (be van jelentkezve) -> Navigálás a Home Screen-re
                navController.navigate(Screen.Home.route) {
                    // Töröljük a Splash Screen-t a vissza-gomb történetéből (back stack),
                    // hogy a felhasználó ne tudjon visszalépni a töltőképernyőre.
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            } else {
                // Ha nincs érvényes token (nincs bejelentkezve) -> Navigálás a Login Screen-re
                navController.navigate(Screen.Login.route) {
                    // Itt is töröljük a Splash Screen-t a back stack-ből.
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        }
    }
    
    // UI felépítése: Egy teljes képernyős felület az elsődleges színnel
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.primary
    ) {
        // Oszlop elrendezés a tartalom középre igazításához
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally, // Vízszintesen középre
            verticalArrangement = Arrangement.Center // Függőlegesen középre
        ) {
            // TODO: Később ide kerülhet egy tényleges kép/logó (Image composable)
            // Jelenleg csak a szöveges logó jelenik meg.
            
            // Alkalmazás neve
            Text(
                text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.app_name),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Alcím / Szlogen
            Text(
                text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.app_subtitle),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Töltésjelző (CircularProgressIndicator)
            // Jelzi a felhasználónak, hogy a háttérben folyamatok zajlanak (pl. token ellenőrzés).
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

/**
 * Előnézet a Splash Screen-hez.
 */
@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    Progr3SSTheme {
        SplashScreen(navController = rememberNavController())
    }
}

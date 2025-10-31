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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.progress.habittracker.navigation.Screen
import com.progress.habittracker.ui.theme.Progr3SSTheme
import com.progress.habittracker.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

/**
 * SplashScreen - Splash (induló) képernyő
 * 
 * Ez a képernyő jelenik meg az alkalmazás indításakor.
 * 
 * Funkciók:
 * - Megjeleníti az alkalmazás logóját és nevét
 * - Automatikus login ellenőrzést végez (TokenManager alapján)
 * - Ha van érvényes token -> Home Screen
 * - Ha nincs érvényes token -> Login Screen
 * 
 * @param navController Navigációs controller
 * @param viewModel Auth ViewModel az auto-login ellenőrzéshez
 */
@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: AuthViewModel = AuthViewModel(LocalContext.current)
) {
    // LaunchedEffect: Lefut amikor a composable betöltődik
    LaunchedEffect(Unit) {
        // Kis késleltetés, hogy látható legyen a splash screen (1.5 másodperc)
        delay(1500)
        
        // Auto-login ellenőrzés
        viewModel.checkAutoLogin { isLoggedIn ->
            if (isLoggedIn) {
                // Van érvényes token -> Home Screen
                navController.navigate(Screen.Home.route) {
                    // Töröljük a Splash-t a back stack-ből
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            } else {
                // Nincs érvényes token -> Login Screen
                navController.navigate(Screen.Login.route) {
                    // Töröljük a Splash-t a back stack-ből
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        }
    }
    
    // UI
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.primary
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // TODO: App logo helyett placeholder
            // Image(
            //     painter = painterResource(id = R.drawable.app_logo),
            //     contentDescription = "Progr3SS Logo",
            //     modifier = Modifier.size(120.dp)
            // )
            
            // App név
            Text(
                text = "Progr3SS",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Alcím
            Text(
                text = "Habit Planner & Tracker",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Loading indicator
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

/**
 * Preview a Splash Screen-hez
 */
@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    Progr3SSTheme {
        SplashScreen(navController = rememberNavController())
    }
}

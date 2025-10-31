package com.progress.habittracker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
// Import-ok a PlaceholderScreen-hez
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
// Auth Screen import-ok
import com.progress.habittracker.ui.screens.auth.SplashScreen
import com.progress.habittracker.ui.screens.auth.LoginScreen
import com.progress.habittracker.ui.screens.auth.RegisterScreen
// Home Screen import
import com.progress.habittracker.ui.screens.home.HomeScreen
// Create Schedule Screen import
import com.progress.habittracker.ui.screens.createschedule.CreateScheduleScreen

/**
 * NavGraph - Navigációs gráf definiálása
 * 
 * Ez a Composable függvény hozza létre a teljes navigációs struktúrát.
 * Minden screen-t itt definiálunk és kötünk össze.
 * 
 * @param navController NavHostController a navigációhoz
 * @param modifier Modifier a NavHost-hoz
 * @param startDestination Kezdő képernyő (alapértelmezett: Splash)
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        
        // ==================== AUTH SCREENS ====================
        
        /**
         * Splash Screen
         * - Automatikus login ellenőrzés
         * - Átirányítás Login-ra vagy Home-ra
         */
        composable(route = Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        
        /**
         * Login Screen
         * - Bejelentkezés email + jelszó
         * - Navigáció: Register, Home
         */
        composable(route = Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        
        /**
         * Register Screen
         * - Regisztráció username, email, jelszó
         * - Navigáció: Login, Home
         */
        composable(route = Screen.Register.route) {
            RegisterScreen(navController = navController)
        }
        
        /**
         * Reset Password Screen
         * - Jelszó visszaállítás email alapján
         * - Navigáció: vissza Login-ra
         */
        composable(route = Screen.ResetPassword.route) {
            // TODO: ResetPasswordScreen() composable hívása
            // ResetPasswordScreen(
            //     onNavigateBack = { navController.popBackStack() }
            // )
            PlaceholderScreen(
                screenName = "Reset Password Screen",
                onNavigate = { navController.popBackStack() }
            )
        }
        
        // ==================== MAIN SCREENS ====================
        
        /**
         * Home Screen (Dashboard)
         * - Napi schedule-ok listája
         * - Navigáció: ScheduleDetails, CreateSchedule, Profile
         */
        composable(route = Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        
        /**
         * Create Schedule Screen
         * - Új schedule létrehozása
         * - Navigáció: vissza Home-ra
         */
        composable(route = Screen.CreateSchedule.route) {
            CreateScheduleScreen(
                navController = navController
            )
        }
        
        // ==================== SCHEDULE DETAILS & EDIT ====================
        
        /**
         * Schedule Details Screen
         * - Schedule részletei ID alapján
         * - Navigáció: EditSchedule, AddProgress, vissza
         * 
         * Paraméter: scheduleId (Int)
         */
        composable(
            route = Screen.ScheduleDetails.route,
            arguments = listOf(
                navArgument("scheduleId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val scheduleId = backStackEntry.arguments?.getInt("scheduleId") ?: 0
            
            // TODO: ScheduleDetailsScreen() composable hívása
            // ScheduleDetailsScreen(
            //     scheduleId = scheduleId,
            //     onNavigateToEdit = { id ->
            //         navController.navigate(Screen.EditSchedule.createRoute(id))
            //     },
            //     onNavigateToAddProgress = { id ->
            //         navController.navigate(Screen.AddProgress.createRoute(id))
            //     },
            //     onNavigateBack = { navController.popBackStack() },
            //     onDeleteSuccess = { navController.popBackStack() }
            // )
            PlaceholderScreen(
                screenName = "Schedule Details Screen (ID: $scheduleId)",
                onNavigate = { navController.popBackStack() }
            )
        }
        
        /**
         * Edit Schedule Screen
         * - Schedule szerkesztése ID alapján
         * - Navigáció: vissza ScheduleDetails-re
         * 
         * Paraméter: scheduleId (Int)
         */
        composable(
            route = Screen.EditSchedule.route,
            arguments = listOf(
                navArgument("scheduleId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val scheduleId = backStackEntry.arguments?.getInt("scheduleId") ?: 0
            
            // TODO: EditScheduleScreen() composable hívása
            // EditScheduleScreen(
            //     scheduleId = scheduleId,
            //     onNavigateBack = { navController.popBackStack() },
            //     onSaveSuccess = { navController.popBackStack() }
            // )
            PlaceholderScreen(
                screenName = "Edit Schedule Screen (ID: $scheduleId)",
                onNavigate = { navController.popBackStack() }
            )
        }
        
        // ==================== HABIT & PROGRESS ====================
        
        /**
         * Add Habit Screen
         * - Új habit létrehozása
         * - Navigáció: vissza
         */
        composable(route = Screen.AddHabit.route) {
            // TODO: AddHabitScreen() composable hívása
            // AddHabitScreen(
            //     onNavigateBack = { navController.popBackStack() },
            //     onSaveSuccess = { navController.popBackStack() }
            // )
            PlaceholderScreen(
                screenName = "Add Habit Screen",
                onNavigate = { navController.popBackStack() }
            )
        }
        
        /**
         * Add Progress Screen
         * - Progress hozzáadása schedule-hoz
         * - Navigáció: vissza ScheduleDetails-re
         * 
         * Paraméter: scheduleId (Int)
         */
        composable(
            route = Screen.AddProgress.route,
            arguments = listOf(
                navArgument("scheduleId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val scheduleId = backStackEntry.arguments?.getInt("scheduleId") ?: 0
            
            // TODO: AddProgressScreen() composable hívása
            // AddProgressScreen(
            //     scheduleId = scheduleId,
            //     onNavigateBack = { navController.popBackStack() },
            //     onSaveSuccess = { navController.popBackStack() }
            // )
            PlaceholderScreen(
                screenName = "Add Progress Screen (Schedule ID: $scheduleId)",
                onNavigate = { navController.popBackStack() }
            )
        }
        
        // ==================== PROFILE ====================
        
        /**
         * Profile Screen
         * - Felhasználó profilja és statisztikák
         * - Navigáció: EditProfile, Logout -> Login
         */
        composable(route = Screen.Profile.route) {
            // TODO: ProfileScreen() composable hívása
            // ProfileScreen(
            //     onNavigateToEditProfile = {
            //         navController.navigate(Screen.EditProfile.route)
            //     },
            //     onNavigateBack = { navController.popBackStack() },
            //     onLogout = {
            //         navController.navigate(Screen.Login.route) {
            //             popUpTo(0) { inclusive = true }
            //         }
            //     }
            // )
            PlaceholderScreen(
                screenName = "Profile Screen",
                onNavigate = { navController.popBackStack() }
            )
        }
        
        /**
         * Edit Profile Screen
         * - Profil adatok szerkesztése
         * - Navigáció: vissza Profile-ra
         */
        composable(route = Screen.EditProfile.route) {
            // TODO: EditProfileScreen() composable hívása
            // EditProfileScreen(
            //     onNavigateBack = { navController.popBackStack() },
            //     onSaveSuccess = { navController.popBackStack() }
            // )
            PlaceholderScreen(
                screenName = "Edit Profile Screen",
                onNavigate = { navController.popBackStack() }
            )
        }
    }
}

/**
 * PlaceholderScreen - Ideiglenes placeholder screen
 * 
 * Ezt használjuk amíg az igazi screen composable-ök nincsenek kész.
 * Később töröljük és helyettesítjük az igazi screen-ekkel.
 * 
 * @param screenName A screen neve
 * @param onNavigate Navigációs akció
 */
@Composable
private fun PlaceholderScreen(
    screenName: String,
    onNavigate: () -> Unit
) {
    // TODO: Ezt később törölni kell és helyettesíteni az igazi screen-ekkel
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
        ) {
            androidx.compose.material3.Text(
                text = screenName,
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
            )
            androidx.compose.material3.Button(onClick = onNavigate) {
                androidx.compose.material3.Text("Navigate")
            }
        }
    }
}


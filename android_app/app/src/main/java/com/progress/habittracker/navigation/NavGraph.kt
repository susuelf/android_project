package com.progress.habittracker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
// Auth Screen import-ok
import com.progress.habittracker.ui.screens.auth.SplashScreen
import com.progress.habittracker.ui.screens.auth.LoginScreen
import com.progress.habittracker.ui.screens.auth.RegisterScreen
// Home Screen import
import com.progress.habittracker.ui.screens.home.HomeScreen
// Create Schedule Screen import
import com.progress.habittracker.ui.screens.createschedule.CreateScheduleScreen
// Add Habit Screen import
import com.progress.habittracker.ui.screens.addhabit.AddHabitScreen
// Add Progress Screen import
import com.progress.habittracker.ui.screens.addprogress.AddProgressScreen
// Schedule Details Screen import
import com.progress.habittracker.ui.screens.scheduledetails.ScheduleDetailsScreen
// Edit Schedule Screen import
import com.progress.habittracker.ui.screens.editschedule.EditScheduleScreen
// Profile Screen import
import com.progress.habittracker.ui.screens.profile.ProfileScreen
// Edit Profile Screen import
import com.progress.habittracker.ui.screens.editprofile.EditProfileScreen

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
            
            ScheduleDetailsScreen(
                navController = navController,
                scheduleId = scheduleId
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
            
            EditScheduleScreen(
                navController = navController,
                scheduleId = scheduleId
            )
        }
        
        // ==================== HABIT & PROGRESS ====================
        
        /**
         * Add Habit Screen
         * - Új habit létrehozása
         * - Navigáció: vissza
         */
        composable(route = Screen.AddHabit.route) {
            AddHabitScreen(
                navController = navController
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
            
            AddProgressScreen(
                navController = navController,
                scheduleId = scheduleId
            )
        }
        
        // ==================== PROFILE ====================
        
        /**
         * Profile Screen
         * - Felhasználó profilja és statisztikák
         * - Navigáció: EditProfile, Logout -> Login
         */
        composable(route = Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        
        /**
         * Edit Profile Screen
         * - Profil adatok szerkesztése
         * - Navigáció: vissza Profile-ra
         */
        composable(route = Screen.EditProfile.route) {
            EditProfileScreen(navController = navController)
        }
    }
}



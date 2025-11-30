package com.progress.habittracker.navigation

// Compose és Navigation alapkönyvtárak
import androidx.compose.runtime.Composable // Composable annotáció
import androidx.compose.ui.Modifier // UI módosítók
import androidx.navigation.NavHostController // Navigáció vezérlő
import androidx.navigation.NavType // Navigációs argumentum típusok
import androidx.navigation.compose.NavHost // Navigációs konténer
import androidx.navigation.compose.composable // Útvonal definíció
import androidx.navigation.navArgument // Útvonal argumentum definíció

// Képernyők (Screens) importálása
// Hitelesítés (Auth) képernyők
import com.progress.habittracker.ui.screens.auth.SplashScreen // Indító képernyő
import com.progress.habittracker.ui.screens.auth.LoginScreen // Bejelentkezés
import com.progress.habittracker.ui.screens.auth.RegisterScreen // Regisztráció

// Fő funkciók képernyői
import com.progress.habittracker.ui.screens.home.HomeScreen // Kezdőlap (Dashboard)
import com.progress.habittracker.ui.screens.createschedule.CreateScheduleScreen // Új időbeosztás létrehozása
import com.progress.habittracker.ui.screens.addhabit.AddHabitScreen // Új szokás hozzáadása
import com.progress.habittracker.ui.screens.addprogress.AddProgressScreen // Haladás rögzítése
import com.progress.habittracker.ui.screens.scheduledetails.ScheduleDetailsScreen // Időbeosztás részletei
import com.progress.habittracker.ui.screens.editschedule.EditScheduleScreen // Időbeosztás szerkesztése
import com.progress.habittracker.ui.screens.profile.ProfileScreen // Profil megtekintése
import com.progress.habittracker.ui.screens.editprofile.EditProfileScreen // Profil szerkesztése

/**
 * NavGraph - Az alkalmazás navigációs térképe
 * 
 * Ez a Composable függvény definiálja az összes lehetséges útvonalat (route) az alkalmazásban,
 * és összeköti őket a megfelelő képernyőkkel (Screen Composable-k).
 * 
 * Működés:
 * A NavHost figyeli a navController állapotát, és kicseréli a megjelenített tartalmat
 * az aktuális útvonalnak megfelelően.
 * 
 * @param navController A navigációt vezérlő objektum (ezt adjuk tovább a képernyőknek is).
 * @param modifier A NavHost-ra vonatkozó módosítók (pl. padding).
 * @param startDestination Az alkalmazás indulásakor megjelenő első képernyő útvonala.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Splash.route
) {
    // NavHost: A navigációs gráf konténere
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        
        // ==================== AUTH SCREENS (Hitelesítés) ====================
        
        /**
         * Splash Screen (Indító képernyő)
         * Funkció: Ellenőrzi, hogy van-e érvényes bejelentkezési token.
         * Navigáció: Ha van token -> Home, ha nincs -> Login.
         */
        composable(route = Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        
        /**
         * Login Screen (Bejelentkezés)
         * Funkció: Felhasználó beléptetése email és jelszó megadásával.
         * Navigáció: Sikeres belépés -> Home, Nincs fiók -> Register.
         */
        composable(route = Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        
        /**
         * Register Screen (Regisztráció)
         * Funkció: Új felhasználói fiók létrehozása.
         * Navigáció: Sikeres regisztráció -> Login vagy Home.
         */
        composable(route = Screen.Register.route) {
            RegisterScreen(navController = navController)
        }
        
        // ==================== MAIN SCREENS (Fő funkciók) ====================
        
        /**
         * Home Screen (Kezdőlap / Dashboard)
         * Funkció: Megjeleníti a napi teendőket (Schedule) és az aktuális haladást.
         * Ez az alkalmazás központi képernyője.
         */
        composable(route = Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        
        /**
         * Create Schedule Screen (Új időbeosztás)
         * Funkció: Új napirendi pont létrehozása.
         */
        composable(route = Screen.CreateSchedule.route) {
            CreateScheduleScreen(
                navController = navController
            )
        }
        
        // ==================== SCHEDULE DETAILS & EDIT (Részletek és Szerkesztés) ====================
        
        /**
         * Schedule Details Screen (Időbeosztás részletei)
         * Funkció: Egy konkrét napirendi pont részletes megjelenítése.
         * Paraméter: scheduleId (Int) - A megjelenítendő elem azonosítója.
         * Navigáció: Innen lehet továbbmenni szerkesztésre (EditSchedule) vagy haladás rögzítésére (AddProgress).
         */
        composable(
            route = Screen.ScheduleDetails.route,
            arguments = listOf(
                navArgument("scheduleId") {
                    type = NavType.IntType // Az ID típusa egész szám
                }
            )
        ) { backStackEntry ->
            // Az ID kinyerése az útvonal argumentumokból
            val scheduleId = backStackEntry.arguments?.getInt("scheduleId") ?: 0
            
            ScheduleDetailsScreen(
                navController = navController,
                scheduleId = scheduleId
            )
        }
        
        /**
         * Edit Schedule Screen (Időbeosztás szerkesztése)
         * Funkció: Meglévő napirendi pont módosítása.
         * Paraméter: scheduleId (Int) - A szerkesztendő elem azonosítója.
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
        
        // ==================== HABIT & PROGRESS (Szokások és Haladás) ====================
        
        /**
         * Add Habit Screen (Új szokás hozzáadása)
         * Funkció: Új szokás (Habit) definiálása, amit később be lehet ütemezni.
         */
        composable(route = Screen.AddHabit.route) {
            AddHabitScreen(
                navController = navController
            )
        }
        
        /**
         * Add Progress Screen (Haladás rögzítése)
         * Funkció: Egy adott napirendi ponthoz tartozó teljesítés rögzítése (pl. elolvastam 10 oldalt).
         * Paraméter: scheduleId (Int) - Melyik napirendi ponthoz tartozik a haladás.
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
        
        // ==================== PROFILE (Profil kezelés) ====================
        
        /**
         * Profile Screen (Profil megtekintése)
         * Funkció: Felhasználói adatok, statisztikák és szokások listázása.
         * Innen érhető el a kijelentkezés és a profil szerkesztése.
         */
        composable(route = Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        
        /**
         * Edit Profile Screen (Profil szerkesztése)
         * Funkció: Felhasználói adatok (név, email, kép, leírás) módosítása.
         */
        composable(route = Screen.EditProfile.route) {
            EditProfileScreen(navController = navController)
        }
    }
}



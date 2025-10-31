package com.progress.habittracker.navigation

/**
 * Screen - Navigation útvonalak (routes) definiálása
 * 
 * Ez a sealed class tartalmazza az összes screen útvonalát az alkalmazásban.
 * A Navigation Compose ezeket használja a képernyők közötti navigációhoz.
 * 
 * Sealed class használata: típusbiztos navigáció, compiler ellenőrzés
 */
sealed class Screen(val route: String) {
    
    /**
     * Splash Screen - Induló képernyő
     * - Automatikus login ellenőrzés
     * - Ha van token -> Home
     * - Ha nincs token -> Login
     */
    data object Splash : Screen("splash")
    
    /**
     * Login Screen - Bejelentkezési képernyő
     * - Email és jelszó megadása
     * - "Elfelejtett jelszó" link -> ResetPassword
     * - "Nincs még fiókod?" link -> Register
     * - Sikeres login -> Home
     */
    data object Login : Screen("login")
    
    /**
     * Register Screen - Regisztrációs képernyő
     * - Username, email, password, password confirmation
     * - "Van már fiókod?" link -> Login
     * - Sikeres regisztráció -> Home
     */
    data object Register : Screen("register")
    
    /**
     * Reset Password Screen - Jelszó visszaállítási képernyő
     * - Email cím megadása
     * - Email küldése új jelszóval
     * - "Vissza a bejelentkezéshez" link -> Login
     */
    data object ResetPassword : Screen("reset_password")
    
    /**
     * Home Screen - Főképernyő (Dashboard)
     * - Mai napi schedule-ok listája
     * - Schedule státuszok (Planned, Completed, Skipped)
     * - FAB gomb -> CreateSchedule
     * - Schedule item kattintás -> ScheduleDetails/{id}
     */
    data object Home : Screen("home")
    
    /**
     * Create Schedule Screen - Új schedule létrehozása
     * - Habit kiválasztása vagy új habit létrehozása
     * - Időpont beállítása
     * - Ismétlődés pattern (daily, weekdays, weekends)
     * - Időtartam megadása
     * - Mentés -> vissza Home-ra
     */
    data object CreateSchedule : Screen("create_schedule")
    
    /**
     * Schedule Details Screen - Schedule részletei
     * - Paraméterrel: schedule ID
     * - Habit információk
     * - Progress bar
     * - Jegyzetek
     * - Előzmények (korábbi progress-ek)
     * - Edit gomb -> EditSchedule/{id}
     * - Delete gomb -> törlés után Home
     */
    data object ScheduleDetails : Screen("schedule_details/{scheduleId}") {
        /**
         * Schedule Details útvonal létrehozása ID-val
         * 
         * @param scheduleId A schedule egyedi azonosítója
         * @return Teljes route string
         */
        fun createRoute(scheduleId: Int) = "schedule_details/$scheduleId"
    }
    
    /**
     * Edit Schedule Screen - Schedule szerkesztése
     * - Paraméterrel: schedule ID
     * - Időpontok módosítása (start, end)
     * - Időtartam módosítása
     * - Státusz módosítása (Planned, Completed, Skipped)
     * - Jegyzetek szerkesztése
     * - Mentés -> vissza ScheduleDetails-re
     */
    data object EditSchedule : Screen("edit_schedule/{scheduleId}") {
        /**
         * Edit Schedule útvonal létrehozása ID-val
         * 
         * @param scheduleId A schedule egyedi azonosítója
         * @return Teljes route string
         */
        fun createRoute(scheduleId: Int) = "edit_schedule/$scheduleId"
    }
    
    /**
     * Add Habit Screen - Új szokás hozzáadása
     * - Habit név
     * - Leírás
     * - Cél megadása
     * - Kategória választás (ikonnal)
     * - Mentés után visszatérés
     */
    data object AddHabit : Screen("add_habit")
    
    /**
     * Profile Screen - Profil képernyő
     * - Felhasználó adatai
     * - Szokások listája
     * - Progress összesítő
     * - Edit gomb -> EditProfile
     * - Kijelentkezés gomb -> Login
     */
    data object Profile : Screen("profile")
    
    /**
     * Edit Profile Screen - Profil szerkesztése
     * - Username módosítása
     * - Email megjelenítése (nem módosítható)
     * - Profilkép feltöltése
     * - Mentés -> vissza Profile-ra
     */
    data object EditProfile : Screen("edit_profile")
    
    /**
     * Add Progress Screen - Progress hozzáadása schedule-hoz
     * - Paraméterrel: schedule ID
     * - Dátum kiválasztása
     * - Eltöltött idő megadása
     * - Jegyzetek hozzáadása
     * - Kész/Nem kész jelölés
     * - Mentés -> vissza ScheduleDetails-re
     */
    data object AddProgress : Screen("add_progress/{scheduleId}") {
        /**
         * Add Progress útvonal létrehozása ID-val
         * 
         * @param scheduleId A schedule egyedi azonosítója
         * @return Teljes route string
         */
        fun createRoute(scheduleId: Int) = "add_progress/$scheduleId"
    }
}

/**
 * Segédfüggvény: Schedule ID kivonása a route-ból
 * 
 * @param route A teljes route string
 * @return Schedule ID vagy null ha nincs
 */
fun getScheduleIdFromRoute(route: String?): Int? {
    return route?.split("/")?.lastOrNull()?.toIntOrNull()
}

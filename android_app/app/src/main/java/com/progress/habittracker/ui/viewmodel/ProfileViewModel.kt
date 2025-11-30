package com.progress.habittracker.ui.viewmodel

// Android Lifecycle komponensek
import androidx.lifecycle.ViewModel // Az alap ViewModel osztály
import androidx.lifecycle.ViewModelProvider // Factory interfész
import androidx.lifecycle.viewModelScope // Coroutine scope a ViewModel-hez
// Adatréteg (Data Layer) importok
import com.progress.habittracker.data.local.TokenManager // Token kezelés
import com.progress.habittracker.data.model.HabitResponseDto // Habit adatmodell
import com.progress.habittracker.data.model.ProfileResponseDto // Profil adatmodell
import com.progress.habittracker.data.repository.ProfileRepository // Profil repository
import com.progress.habittracker.data.repository.ScheduleRepository // Schedule repository (statisztikákhoz)
import com.progress.habittracker.util.Resource // Wrapper osztály az eredmények kezelésére (Success, Error, Loading)
// Kotlin Coroutines és Flow
import kotlinx.coroutines.flow.MutableStateFlow // Módosítható állapotfolyam
import kotlinx.coroutines.flow.StateFlow // Csak olvasható állapotfolyam
import kotlinx.coroutines.flow.asStateFlow // Konverzió StateFlow-ra
import kotlinx.coroutines.flow.combine // Flow-k összefésülése
import kotlinx.coroutines.flow.update // Állapot frissítése
import kotlinx.coroutines.launch // Coroutine indítása
// Dátumkezelés
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

/**
 * Profile ViewModel
 * 
 * Ez az osztály felelős a Profil képernyő (ProfileScreen) üzleti logikájáért és állapotkezeléséért.
 * 
 * Főbb feladatai:
 * 1. A felhasználói profil adatainak lekérése a Repository-ból.
 * 2. A felhasználó szokásainak (Habits) lekérése.
 * 3. Statisztikák számítása a szokásokhoz (pl. heti teljesítés).
 * 4. A UI állapot (ProfileUiState) karbantartása és frissítése.
 * 5. Kijelentkezés kezelése.
 * 
 * @param profileRepository Adatforrás a profil adatokhoz.
 * @param scheduleRepository Adatforrás a beosztásokhoz (statisztikákhoz kell).
 * @param tokenManager A hitelesítési token törléséhez kijelentkezéskor.
 */
class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val scheduleRepository: ScheduleRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    // A UI állapotát tároló MutableStateFlow. Csak a ViewModel módosíthatja.
    private val _uiState = MutableStateFlow(ProfileUiState())
    // A UI számára publikus, csak olvasható StateFlow.
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    // Inicializáláskor azonnal betöltjük a profilt
    init {
        loadProfile()
    }

    /**
     * UI State data class
     * 
     * Ez az osztály írja le a képernyő teljes állapotát egy adott pillanatban.
     * A Compose UI ez alapján rajzolja ki magát.
     * 
     * @param isLoading Töltődik-e a profil.
     * @param profile A betöltött profil adatok.
     * @param habits A felhasználó szokásainak listája.
     * @param habitStats Statisztikák (Habit ID -> Teljesítési arány).
     * @param isLoadingHabits Töltődnek-e a szokások.
     * @param error Hibaüzenet, ha valami nem sikerült.
     */
    data class ProfileUiState(
        val isLoading: Boolean = false,
        val profile: ProfileResponseDto? = null,
        val habits: List<HabitResponseDto> = emptyList(),
        val habitStats: Map<Int, Float> = emptyMap(), // Habit ID -> Consistency % (0.0 - 1.0)
        val isLoadingHabits: Boolean = false,
        val error: String? = null
    )

    /**
     * Profil betöltése
     * 
     * Elindít egy coroutine-t, és lekéri a profil adatokat a repository-ból.
     * Kezeli a Loading, Success és Error állapotokat.
     */
    fun loadProfile() {
        viewModelScope.launch {
            // A repository Flow-t ad vissza, amire feliratkozunk (collect)
            profileRepository.getMyProfile().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Betöltés kezdete: isLoading = true
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        // Sikeres betöltés: adatok mentése, isLoading = false
                        resource.data?.let { profile ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    profile = profile,
                                    error = null
                                )
                            }
                            // Ha megvan a profil (és az ID), betöltjük a szokásokat és statisztikákat is
                            loadUserHabitsAndStats(profile.id)
                        }
                    }
                    is Resource.Error -> {
                        // Hiba történt: hibaüzenet mentése, isLoading = false
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = resource.message ?: "Ismeretlen hiba"
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Felhasználó habit-jeinek és statisztikáinak betöltése
     * 
     * Ez a függvény párhuzamosan kéri le a szokásokat és a heti beosztást,
     * majd összefésüli (combine) az eredményeket a statisztikák kiszámításához.
     */
    private fun loadUserHabitsAndStats(userId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingHabits = true) }

            // 1. Mai dátum lekérése
            val today = LocalDate.now()
            val dateFormatter = DateTimeFormatter.ISO_DATE
            val todayString = today.format(dateFormatter)

            // 2. Flow-k definiálása
            val habitsFlow = profileRepository.getUserHabits(userId)
            val schedulesFlow = scheduleRepository.getSchedulesByDay(todayString)
            
            // 3. Combine operátor használata: Habits + Today's Schedules
            combine(habitsFlow, schedulesFlow) { habitsResource, schedulesResource ->
                var currentHabits = _uiState.value.habits
                var currentStats = _uiState.value.habitStats
                var isLoading = true
                
                // Hibakezelés (egyszerűsítve: ha bármelyik hiba, akkor hiba)
                if (habitsResource is Resource.Error || schedulesResource is Resource.Error) {
                    isLoading = false
                    // Itt lehetne error message-t beállítani
                }

                // Ha mindkettő sikeres, számoljuk a statisztikát
                if (habitsResource is Resource.Success && schedulesResource is Resource.Success) {
                    val habits = habitsResource.data ?: emptyList()
                    val schedules = schedulesResource.data ?: emptyList()
                    
                    currentHabits = habits
                    
                    // Statisztika számítása: Mai átlagos progress habit-enként
                    currentStats = habits.associate { habit ->
                        // Keressük a habit-hez tartozó mai schedule-öket
                        // Fontos: schedule.habit.id-t használunk, mert a habitId mező nem biztosított
                        val habitSchedules = schedules.filter { it.habit.id == habit.id }
                        
                        val progress = if (habitSchedules.isNotEmpty()) {
                            // Összeadjuk a százalékokat
                            val totalProgress = habitSchedules.sumOf { schedule ->
                                com.progress.habittracker.util.ScheduleStateCalculator.calculate(schedule).progressPercentage.toDouble()
                            }
                            // Átlagolunk
                            val avgPercent = totalProgress / habitSchedules.size
                            // 0.0 - 1.0 tartományba konvertáljuk a UI számára
                            (avgPercent / 100.0).toFloat()
                        } else {
                            0f // Ha nincs mára ütemezve, 0%
                        }
                        habit.id to progress
                    }
                    isLoading = false
                } else if (habitsResource is Resource.Loading || schedulesResource is Resource.Loading) {
                    isLoading = true
                }
                
                Triple(currentHabits, currentStats, isLoading)
            }.collect { (habits, stats, isLoading) ->
                _uiState.update { 
                    it.copy(
                        habits = habits,
                        habitStats = stats,
                        isLoadingHabits = isLoading
                    ) 
                }
            }
        }
    }

    /**
     * Kijelentkezés
     * 
     * Törli a tárolt hitelesítési tokent és minden egyéb mentett adatot,
     * így a következő indításkor a felhasználónak újra be kell jelentkeznie.
     */
    fun logout() {
        viewModelScope.launch {
            tokenManager.clearAll()
        }
    }

    /**
     * Hibaüzenet törlése
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * ProfileViewModel Factory
 */
class ProfileViewModelFactory(
    private val profileRepository: ProfileRepository,
    private val scheduleRepository: ScheduleRepository,
    private val tokenManager: TokenManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(profileRepository, scheduleRepository, tokenManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

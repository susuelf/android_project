package com.progress.habittracker.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.progress.habittracker.data.local.TokenManager
import com.progress.habittracker.data.repository.ProgressRepository
import com.progress.habittracker.data.repository.ScheduleRepository
import com.progress.habittracker.navigation.Screen
import com.progress.habittracker.ui.theme.DarkBackground
import com.progress.habittracker.ui.theme.DarkSurface
import com.progress.habittracker.ui.theme.Progr3SSTheme
import com.progress.habittracker.ui.theme.SuccessCyan
import com.progress.habittracker.ui.theme.TextPrimary
import com.progress.habittracker.ui.theme.TextSecondary
import com.progress.habittracker.ui.theme.TextTertiary
import com.progress.habittracker.ui.viewmodel.HomeViewModel
import com.progress.habittracker.ui.viewmodel.HomeViewModelFactory
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

/**
 * Home Screen - Főképernyő
 *
 * Az alkalmazás központi képernyője, ahol a felhasználó látja a napi teendőit (schedule).
 *
 * Főbb funkciók:
 * - Dátum navigáció: Előző/Következő nap, Ugrás a mai napra, Naptár választó.
 * - "Today's plan" fejléc: Az aktuálisan kiválasztott nap megjelenítése.
 * - Időbeosztások listázása: Napszakok szerint csoportosítva (Reggel, Délután, Este).
 * - Státusz kezelés: Checkbox segítségével gyorsan állítható a státusz (Tervezett -> Befejezett).
 * - Új időbeosztás létrehozása: Floating Action Button (FAB) segítségével.
 * - Alsó navigációs sáv (Bottom Navigation): Navigáció a főbb képernyők között (Home, Profil).
 *
 * @param navController A navigációért felelős vezérlő.
 */
@Suppress("NewApi") // Java Time API használata miatt (desugaring támogatott)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController
) {
    val context = LocalContext.current

    // ViewModel és Repository-k inicializálása
    val tokenManager = remember { TokenManager(context) }
    val scheduleRepository = remember { ScheduleRepository(tokenManager) }
    val progressRepository = remember { ProgressRepository(tokenManager) }
    
    // ViewModel létrehozása a Factory segítségével
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(scheduleRepository, progressRepository)
    )

    // UI állapot figyelése a ViewModel-ből
    val uiState by viewModel.uiState.collectAsState()

    // Dátum formázók inicializálása
    val dayOfWeekFormatter = remember { DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH) }
    val topBarDateFormatter = remember { DateTimeFormatter.ofPattern("yyyy. MM. dd.") }

    // Snackbar állapot a hibaüzenetek megjelenítéséhez
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Dátumválasztó dialógus állapota
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Kiválasztott alsó navigációs elem (0 = Home)
    var selectedNavItem by remember { mutableStateOf(0) }
    
    // Adatok frissítése, amikor a képernyő újra előtérbe kerül (pl. visszatérés szerkesztésből)
    val navBackStackEntry = navController.currentBackStackEntry
    LaunchedEffect(navBackStackEntry) {
        viewModel.refreshSchedules()
    }

    // Hibaüzenetek kezelése
    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMessage ->
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    // Dátumválasztó dialógus megjelenítése
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.selectedDate
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                            viewModel.selectDate(selectedDate)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Scaffold: Az alapvető képernyőszerkezet
    Scaffold(
        topBar = {
            // Felső sáv: Dátum navigáció
            TopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Dátum (pl. 2023. 10. 27.)
                        Text(
                            text = uiState.selectedDate.format(topBarDateFormatter),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        // Hét napja (pl. Friday)
                        Text(
                            text = uiState.selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() },
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    // Előző nap gomb
                    IconButton(onClick = { viewModel.goToPreviousDay() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous day",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    // "MA" gomb
                    TextButton(onClick = { viewModel.goToToday() }) {
                        Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.today_caps), color = TextPrimary)
                    }

                    // Naptár gomb
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select date",
                            tint = TextPrimary
                        )
                    }

                    // Következő nap gomb
                    IconButton(onClick = { viewModel.goToNextDay() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next day",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            // Alsó navigációs sáv
            NavigationBar(
                containerColor = DarkSurface,
                contentColor = TextPrimary
            ) {
                // Home menüpont
                NavigationBarItem(
                    icon = { 
                        Icon(
                            if (selectedNavItem == 0) Icons.Filled.Home else Icons.Outlined.Home, 
                            contentDescription = "Home"
                        ) 
                    },
                    label = { Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.home_title)) },
                    selected = selectedNavItem == 0,
                    onClick = { 
                        selectedNavItem = 0
                        // Már a Home screen-en vagyunk
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SuccessCyan,
                        selectedTextColor = SuccessCyan,
                        indicatorColor = DarkSurface,
                        unselectedIconColor = TextTertiary,
                        unselectedTextColor = TextTertiary
                    )
                )
                
                // Profil menüpont
                NavigationBarItem(
                    icon = { 
                        Icon(
                            if (selectedNavItem == 1) Icons.Filled.Person else Icons.Outlined.Person, 
                            contentDescription = "Profile"
                        ) 
                    },
                    label = { Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.profile_title)) },
                    selected = selectedNavItem == 1,
                    onClick = { 
                        selectedNavItem = 1
                        navController.navigate(Screen.Profile.route)
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SuccessCyan,
                        selectedTextColor = SuccessCyan,
                        indicatorColor = DarkSurface,
                        unselectedIconColor = TextTertiary,
                        unselectedTextColor = TextTertiary
                    )
                )
            }
        },
        floatingActionButton = {
            // Lebegő akciógomb (FAB) - Új időbeosztás létrehozása
            FloatingActionButton(
                onClick = { navController.navigate(Screen.CreateSchedule.route) },
                containerColor = SuccessCyan,
                contentColor = DarkBackground
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Schedule"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // "Today's plan" Fejléc
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.todays_plan),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 24.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${uiState.selectedDate.format(dayOfWeekFormatter)}, ${uiState.selectedDate.format(dateFormatter)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    fontSize = 16.sp
                )
            }
            
            // Tartalom megjelenítése állapot szerint
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when {
                    // Töltés állapot
                    uiState.isLoading && uiState.schedules.isEmpty() -> {
                        LoadingState()
                    }

                    // Üres állapot - nincs megjeleníthető elem
                    !uiState.isLoading && uiState.schedules.isEmpty() -> {
                        EmptyState(
                            onCreateSchedule = { navController.navigate(Screen.CreateSchedule.route) }
                        )
                    }

                    // Lista megjelenítése napszakok szerint csoportosítva
                    else -> {
                        ScheduleListGroupedByTimeOfDay(
                            schedules = uiState.schedules,
                            isRefreshing = uiState.isRefreshing,
                            onRefresh = { viewModel.refreshSchedules() },
                            onScheduleClick = { scheduleId ->
                                navController.navigate(Screen.ScheduleDetails.createRoute(scheduleId))
                            },
                            onStatusToggle = { scheduleId, currentStatus ->
                                viewModel.toggleScheduleStatus(scheduleId, currentStatus)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Töltés állapotot megjelenítő komponens.
 */
@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.loading_schedules),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Üres állapotot megjelenítő komponens.
 * Akkor jelenik meg, ha az adott napra nincs időbeosztás.
 *
 * @param onCreateSchedule Callback függvény az új időbeosztás létrehozásához.
 */
@Composable
private fun EmptyState(
    onCreateSchedule: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.no_schedules_for_day),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.add_new_schedule_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onCreateSchedule,
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.new_schedule))
            }
        }
    }
}

/**
 * Időbeosztások listázása napszakok szerint csoportosítva.
 *
 * @param schedules A megjelenítendő időbeosztások listája.
 * @param isRefreshing Jelzi, ha éppen frissítés történik.
 * @param onRefresh Callback a lista frissítéséhez.
 * @param onScheduleClick Callback egy elemre kattintáskor.
 * @param onStatusToggle Callback a státusz módosításakor.
 */
@Composable
private fun ScheduleListGroupedByTimeOfDay(
    schedules: List<com.progress.habittracker.data.model.ScheduleResponseDto>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onScheduleClick: (Int) -> Unit,
    onStatusToggle: (Int, com.progress.habittracker.data.model.ScheduleStatus) -> Unit
) {
    // Csoportosítás napszak szerint és rendezés (Reggel -> Délután -> Este)
    val groupedSchedules = remember(schedules) {
        schedules.groupBy { schedule ->
            getTimeOfDay(schedule.startTime)
        }.toSortedMap(compareBy { 
            when(it) {
                TimeOfDay.Morning -> 0
                TimeOfDay.Afternoon -> 1
                TimeOfDay.Night -> 2
            }
        })
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Csoportok iterálása
        groupedSchedules.forEach { (timeOfDay, scheduleGroup) ->
            // Csoport fejléc (pl. "Morning 🌅")
            item(key = "header_$timeOfDay") {
                TimeOfDayHeader(timeOfDay)
            }
            
            // Csoport elemei
            items(
                items = scheduleGroup,
                key = { it.id }
            ) { schedule ->
                ScheduleItemCard(
                    schedule = schedule,
                    onScheduleClick = onScheduleClick,
                    onStatusToggle = onStatusToggle
                )
            }
        }

        // Extra térköz az alján a FAB és a Bottom Navigation miatt
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

/**
 * Napszakok enumerációja.
 */
private enum class TimeOfDay {
    Morning,    // 00:00 - 11:59
    Afternoon,  // 12:00 - 17:59
    Night       // 18:00 - 23:59
}

/**
 * Segédfüggvény a napszak meghatározásához a kezdési idő alapján.
 */
private fun getTimeOfDay(startTimeString: String): TimeOfDay {
    return try {
        // Idő string parse-olása
        val timeStr = if (startTimeString.contains('T')) {
            startTimeString.substringAfter('T').substringBefore('Z')
        } else {
            startTimeString
        }
        
        val time = LocalTime.parse(timeStr, DateTimeFormatter.ISO_LOCAL_TIME)
        val hour = time.hour
        
        when {
            hour < 12 -> TimeOfDay.Morning
            hour < 18 -> TimeOfDay.Afternoon
            else -> TimeOfDay.Night
        }
    } catch (e: Exception) {
        TimeOfDay.Morning // Alapértelmezett érték hiba esetén
    }
}

/**
 * Napszak fejléc megjelenítése ikonnal és szöveggel.
 */
@Composable
private fun TimeOfDayHeader(timeOfDay: TimeOfDay) {
    val (emoji, label) = when (timeOfDay) {
        TimeOfDay.Morning -> "🌅" to androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.morning)
        TimeOfDay.Afternoon -> "🍂" to androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.afternoon)
        TimeOfDay.Night -> "🌙" to androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.night)
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.titleMedium,
            fontSize = 20.sp,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            fontSize = 18.sp
        )
    }
}

/**
 * Előnézet (Preview) a Home képernyőhöz.
 */
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    Progr3SSTheme {
        HomeScreen(navController = rememberNavController())
    }
}

/**
 * Előnézet (Preview) az üres állapothoz.
 */
@Preview(showBackground = true)
@Composable
fun HomeScreenEmptyPreview() {
    Progr3SSTheme {
        EmptyState(onCreateSchedule = {})
    }
}

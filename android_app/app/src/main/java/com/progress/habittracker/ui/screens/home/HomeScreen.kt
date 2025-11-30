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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.*

private const val START_PAGE = Int.MAX_VALUE / 2

/**
 * Home Screen - Főképernyő (Design frissítve - "Today's plan" + Bottom Navigation)
 * 
 * Az alkalmazás központi képernyője, ahol a felhasználó látja a napi schedule-jait.
 * 
 * Funkciók:
 * - Top Bar: Dátum navigáció (előző/következő nap, MA gomb)
 * - "Today's plan" header dátummal (Monday, Jul 10)
 * - Schedule-ok időszakok szerint csoportosítva (Morning 🌅, Afternoon 🍂, Night 🌙)
 * - Schedule-ok rendezve időrendi sorrendben
 * - Státusz jelzés és gyors státusz váltás (checkbox)
 * - Floating Action Button (cyan) -> Create Schedule
 * - Bottom Navigation Bar (Home, AI Assistant, Profile)
 * - Dark theme teljes képernyőn
 * - Üres állapot kezelése (nincs schedule)
 * - Hiba állapot kezelése
 * 
 * @param navController Navigációs controller
 */
@Suppress("NewApi") // Java Time API is available via desugaring
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController
) {
    val context = LocalContext.current

    // ViewModel inicializálás Factory-val
    val tokenManager = remember { TokenManager(context) }
    val scheduleRepository = remember { ScheduleRepository(tokenManager) }
    val progressRepository = remember { ProgressRepository(tokenManager) }
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(scheduleRepository, progressRepository)
    )

    // UI State collect
    val uiState by viewModel.uiState.collectAsState()

    // Pager Logic
    val anchorDate = remember { LocalDate.now() }
    val scope = rememberCoroutineScope()

    fun getDateForPage(page: Int): LocalDate {
        val daysDiff = page - START_PAGE
        return anchorDate.plusDays(daysDiff.toLong())
    }

    fun getPageForDate(date: LocalDate): Int {
        val daysDiff = ChronoUnit.DAYS.between(anchorDate, date).toInt()
        return START_PAGE + daysDiff
    }

    val pagerState = rememberPagerState(
        initialPage = START_PAGE,
        pageCount = { Int.MAX_VALUE }
    )

    LaunchedEffect(pagerState.currentPage) {
        val date = getDateForPage(pagerState.currentPage)
        if (date != uiState.selectedDate) {
            viewModel.loadSchedules(date)
        }
    }

    LaunchedEffect(uiState.selectedDate) {
        val targetPage = getPageForDate(uiState.selectedDate)
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    // Dátum formázók
    val dayOfWeekFormatter = remember { DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH) }
    val topBarDateFormatter = remember { DateTimeFormatter.ofPattern("yyyy. MM. dd.") }

    // Snackbar host state
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Selected navigation item
    var selectedNavItem by remember { mutableStateOf(0) } // 0 = Home
    
    // Frissítés amikor visszajövünk Edit Schedule-ből vagy más képernyőről
    val navBackStackEntry = navController.currentBackStackEntry
    LaunchedEffect(navBackStackEntry) {
        viewModel.refreshSchedules()
    }

    // Error handling - Snackbar megjelenítése
    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMessage ->
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            // Top App Bar - Dátum navigáció
            TopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Dátum
                        Text(
                            text = uiState.selectedDate.format(topBarDateFormatter),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        // Hét napja
                        Text(
                            text = uiState.selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("hu"))
                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.forLanguageTag("hu")) else it.toString() },
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    // Előző nap
                    IconButton(onClick = { 
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous day",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    // Ma gomb
                    TextButton(onClick = { 
                        scope.launch { pagerState.animateScrollToPage(getPageForDate(LocalDate.now())) }
                    }) {
                        Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.today_caps), color = TextPrimary)
                    }

                    // Következő nap
                    IconButton(onClick = { 
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }) {
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
            // Bottom Navigation Bar
            NavigationBar(
                containerColor = DarkSurface,
                contentColor = TextPrimary
            ) {
                // Home
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
                
                // Profile
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
            // FAB - Cyan színnel
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
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { page ->
            val pageDate = getDateForPage(page)
            
            if (pageDate == uiState.selectedDate) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // "Today's plan" Header
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
                    
                    // Tartalom
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        when {
                            // Loading állapot
                            uiState.isLoading && uiState.schedules.isEmpty() -> {
                                LoadingState()
                            }

                            // Üres állapot - nincs schedule
                            !uiState.isLoading && uiState.schedules.isEmpty() -> {
                                EmptyState(
                                    onCreateSchedule = { navController.navigate(Screen.CreateSchedule.route) }
                                )
                            }

                            // Schedules megjelenítése időszakok szerint csoportosítva
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
            } else {
                // Loading placeholder for off-screen pages
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

/**
 * Loading State
 * 
 * Betöltés közbeni állapot megjelenítése
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
 * Empty State
 * 
 * Üres állapot megjelenítése - nincs schedule
 * 
 * @param onCreateSchedule Callback az új schedule létrehozáshoz
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
 * Schedule List csoportosítva napszak szerint
 * 
 * Schedules megjelenítése Morning, Afternoon, Night csoportokban
 * 
 * @param schedules Schedule-ok listája
 * @param isRefreshing Refresh folyamatban flag
 * @param onRefresh Refresh callback
 * @param onScheduleClick Schedule kattintás callback
 * @param onStatusToggle Státusz váltás callback
 */
@Composable
private fun ScheduleListGroupedByTimeOfDay(
    schedules: List<com.progress.habittracker.data.model.ScheduleResponseDto>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onScheduleClick: (Int) -> Unit,
    onStatusToggle: (Int, com.progress.habittracker.data.model.ScheduleStatus) -> Unit
) {
    // Csoportosítás napszak szerint
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
        // Csoportok megjelenítése
        groupedSchedules.forEach { (timeOfDay, scheduleGroup) ->
            // Csoport header
            item(key = "header_$timeOfDay") {
                TimeOfDayHeader(timeOfDay)
            }
            
            // Csoport schedule-ai
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

        // Extra padding a FAB miatt (és bottom nav bar miatt is)
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

/**
 * Napszak enum
 */
private enum class TimeOfDay {
    Morning,    // 00:00 - 11:59
    Afternoon,  // 12:00 - 17:59
    Night       // 18:00 - 23:59
}

/**
 * Meghatározza a napszakot a startTime alapján
 */
private fun getTimeOfDay(startTimeString: String): TimeOfDay {
    return try {
        // Parse time
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
        TimeOfDay.Morning // Default fallback
    }
}

/**
 * Napszak header megjelenítése emoji-val
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
 * Schedule List - régi verzió (backup)
 * 
 * Schedule-ok listája pull-to-refresh támogatással
 */
@Composable
private fun ScheduleList(
    schedules: List<com.progress.habittracker.data.model.ScheduleResponseDto>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onScheduleClick: (Int) -> Unit,
    onStatusToggle: (Int, com.progress.habittracker.data.model.ScheduleStatus) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Schedule-ok megjelenítése
        items(
            items = schedules,
            key = { it.id }
        ) { schedule ->
            ScheduleItemCard(
                schedule = schedule,
                onScheduleClick = onScheduleClick,
                onStatusToggle = onStatusToggle
            )
        }

        // Extra padding a FAB miatt
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

/**
 * Preview - Schedules-szel
 */
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    Progr3SSTheme {
        HomeScreen(navController = rememberNavController())
    }
}

/**
 * Preview - Üres állapot
 */
@Preview(showBackground = true)
@Composable
fun HomeScreenEmptyPreview() {
    Progr3SSTheme {
        EmptyState(onCreateSchedule = {})
    }
}

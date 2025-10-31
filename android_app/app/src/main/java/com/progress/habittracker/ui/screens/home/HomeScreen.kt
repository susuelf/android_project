package com.progress.habittracker.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.progress.habittracker.data.local.TokenManager
import com.progress.habittracker.data.repository.ScheduleRepository
import com.progress.habittracker.navigation.Screen
import com.progress.habittracker.ui.theme.Progr3SSTheme
import com.progress.habittracker.ui.viewmodel.HomeViewModel
import com.progress.habittracker.ui.viewmodel.HomeViewModelFactory
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

/**
 * Home Screen - Főképernyő
 * 
 * Az alkalmazás központi képernyője, ahol a felhasználó látja a napi schedule-jait.
 * 
 * Funkciók:
 * - Napi schedule-ok megjelenítése listában
 * - Schedule-ok rendezve időrendi sorrendben
 * - Státusz jelzés és gyors státusz váltás (checkbox)
 * - Dátum navigáció (előző/következő nap, ma gomb)
 * - Pull-to-refresh támogatás
 * - Floating Action Button -> Create Schedule
 * - Bottom Navigation Bar (később)
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
    val repository = remember { ScheduleRepository(tokenManager) }
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(repository)
    )

    // UI State collect
    val uiState by viewModel.uiState.collectAsState()

    // Dátum formázók
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy. MM. dd.") }

    // Snackbar host state
    val snackbarHostState = remember { SnackbarHostState() }

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
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                            text = uiState.selectedDate.format(dateFormatter),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        // Hét napja
                        Text(
                            text = uiState.selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("hu"))
                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.forLanguageTag("hu")) else it.toString() },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    // Előző nap
                    IconButton(onClick = { viewModel.goToPreviousDay() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Előző nap"
                        )
                    }
                },
                actions = {
                    // Ma gomb
                    TextButton(onClick = { viewModel.goToToday() }) {
                        Text("MA")
                    }

                    // Következő nap
                    IconButton(onClick = { viewModel.goToNextDay() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Következő nap"
                        )
                    }

                    // Profile icon (navigáció Profile-ra)
                    IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profil"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // FAB - Create Schedule
            FloatingActionButton(
                onClick = { navController.navigate(Screen.CreateSchedule.route) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Új Schedule"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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

                // Schedules megjelenítése
                else -> {
                    ScheduleList(
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
                text = "Schedule-ok betöltése...",
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
                text = "Nincs scheduled feladat erre a napra",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Adj hozzá egy új scheduled-ot a + gombbal!",
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
                Text("Új Schedule")
            }
        }
    }
}

/**
 * Schedule List
 * 
 * Schedule-ok listája pull-to-refresh támogatással
 * 
 * @param schedules Schedule-ok listája
 * @param isRefreshing Refresh folyamatban flag
 * @param onRefresh Refresh callback
 * @param onScheduleClick Schedule kattintás callback
 * @param onStatusToggle Státusz váltás callback
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

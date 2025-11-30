package com.progress.habittracker.ui.screens.createschedule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.progress.habittracker.data.local.TokenManager
import com.progress.habittracker.data.model.HabitResponseDto
import com.progress.habittracker.data.model.RepeatPattern
import com.progress.habittracker.data.repository.HabitRepository
import com.progress.habittracker.data.repository.ScheduleRepository
import com.progress.habittracker.ui.viewmodel.CreateScheduleViewModel
import com.progress.habittracker.ui.viewmodel.CreateScheduleViewModelFactory
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Create Schedule Screen - Új időbeosztás létrehozása
 *
 * Ez a képernyő teszi lehetővé a felhasználók számára, hogy új időbeosztást (schedule) hozzanak létre
 * egy meglévő szokáshoz (habit).
 *
 * Főbb funkciók:
 * - Szokás kiválasztása a meglévő listából.
 * - Kezdési időpont és tervezett időtartam beállítása.
 * - Ismétlődési mintázat beállítása (pl. naponta, hétköznap, hétvégén).
 * - Opcionális megjegyzések hozzáadása.
 * - Új szokás létrehozásának kezdeményezése, ha a listában nem szerepel a kívánt szokás.
 *
 * @param navController A navigációért felelős vezérlő.
 */
@Suppress("NewApi") // Java Time API használata miatt (desugaring támogatott)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScheduleScreen(
    navController: NavController
) {
    // Kontextus és függőségek inicializálása
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val scheduleRepository = remember { ScheduleRepository(tokenManager) }
    val habitRepository = remember { HabitRepository(tokenManager) }

    // ViewModel létrehozása a Factory segítségével
    val viewModel: CreateScheduleViewModel = viewModel(
        factory = CreateScheduleViewModelFactory(scheduleRepository, habitRepository)
    )

    // UI állapot figyelése a ViewModel-ből
    val uiState by viewModel.uiState.collectAsState()
    
    // Snackbar állapot a hibaüzenetek megjelenítéséhez
    val snackbarHostState = remember { SnackbarHostState() }

    // Habit lista újratöltése, amikor visszanavigálunk az AddHabit képernyőről.
    // Ez biztosítja, hogy ha a felhasználó létrehozott egy új szokást, az megjelenjen a listában.
    val navBackStackEntry = navController.currentBackStackEntry
    LaunchedEffect(navBackStackEntry) {
        viewModel.loadHabits()
    }

    // Ha sikeres a létrehozás, navigáljunk vissza az előző képernyőre
    LaunchedEffect(uiState.createSuccess) {
        if (uiState.createSuccess) {
            navController.popBackStack()
        }
    }

    // Hibaüzenet megjelenítése Snackbar-ban
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError() // Hiba törlése megjelenítés után
        }
    }

    // Scaffold: Az alapvető képernyőszerkezet (TopBar, BottomBar, Content)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.create_schedule_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.back)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomBar(
                onCreateClick = { viewModel.createSchedule() },
                isCreating = uiState.isCreating,
                enabled = uiState.selectedHabit != null // Csak akkor engedélyezett, ha van kiválasztott szokás
            )
        }
    ) { paddingValues ->
        // A tartalom megjelenítése
        CreateScheduleContent(
            uiState = uiState,
            viewModel = viewModel,
            navController = navController,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

/**
 * A képernyő tartalmát megjelenítő Composable.
 * LazyColumn-t használ a görgethetőség érdekében.
 */
@Composable
private fun CreateScheduleContent(
    uiState: CreateScheduleViewModel.CreateScheduleUiState,
    viewModel: CreateScheduleViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Szokás kiválasztása szekció
        item {
            HabitSelectionSection(
                habits = uiState.habits,
                selectedHabit = uiState.selectedHabit,
                isLoading = uiState.isLoadingHabits,
                onHabitSelected = { viewModel.selectHabit(it) },
                onAddNewHabit = { navController.navigate(com.progress.habittracker.navigation.Screen.AddHabit.route) }
            )
        }

        // Csak akkor jelenítjük meg a többi szekciót, ha már van kiválasztott szokás
        if (uiState.selectedHabit != null) {
            // 2. Idő és időtartam beállítása szekció
            item {
                TimeSection(
                    startTime = uiState.startTime,
                    duration = uiState.durationMinutes,
                    onStartTimeChange = { viewModel.setStartTime(it) },
                    onDurationChange = { viewModel.setDuration(it) }
                )
            }

            // 3. Ismétlődési mintázat beállítása szekció
            item {
                RepeatPatternSection(
                    repeatPattern = uiState.repeatPattern,
                    onPatternChange = { viewModel.setRepeatPattern(it) }
                )
            }

            // 4. Megjegyzések szekció
            item {
                NotesSection(
                    notes = uiState.notes,
                    onNotesChange = { viewModel.setNotes(it) }
                )
            }
        }
    }
}

/**
 * Szokás kiválasztására szolgáló szekció.
 * Tartalmaz egy legördülő menüt a meglévő szokásokkal és egy gombot új szokás létrehozásához.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HabitSelectionSection(
    habits: List<HabitResponseDto>,
    selectedHabit: HabitResponseDto?,
    isLoading: Boolean,
    onHabitSelected: (HabitResponseDto) -> Unit,
    onAddNewHabit: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) } // Legördülő menü állapota

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Fejléc és "Új hozzáadása" gomb
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.habit_label),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                TextButton(onClick = onAddNewHabit) {
                    Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.add_new_habit_button))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                // Legördülő menü (Dropdown)
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedHabit?.name ?: androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.select_habit_placeholder),
                        onValueChange = {},
                        readOnly = true, // A felhasználó nem írhat bele közvetlenül
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        habits.forEach { habit ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = habit.name,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = habit.category.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    onHabitSelected(habit)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Idő és időtartam beállítására szolgáló szekció.
 * Tartalmaz egy időválasztót (TimePicker) és egy szövegmezőt az időtartamhoz.
 */
@Composable
private fun TimeSection(
    startTime: LocalTime,
    duration: Int?,
    onStartTimeChange: (LocalTime) -> Unit,
    onDurationChange: (Int?) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.time_and_duration_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Kezdési idő kiválasztása (TimePickerDialog)
            OutlinedButton(
                onClick = {
                    android.app.TimePickerDialog(
                        context,
                        { _, hour, minute ->
                            onStartTimeChange(LocalTime.of(hour, minute))
                        },
                        startTime.hour,
                        startTime.minute,
                        true // 24 órás formátum
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.start_time_prefix) + startTime.format(DateTimeFormatter.ofPattern("HH:mm")))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Időtartam megadása percben
            var durationText by remember { mutableStateOf(duration?.toString() ?: "30") }
            OutlinedTextField(
                value = durationText,
                onValueChange = {
                    durationText = it
                    it.toIntOrNull()?.let { minutes -> onDurationChange(minutes) }
                },
                label = { Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.duration_min_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Ismétlődési mintázat beállítására szolgáló szekció.
 * FilterChip-eket használ a választáshoz.
 */
@Composable
private fun RepeatPatternSection(
    repeatPattern: RepeatPattern,
    onPatternChange: (RepeatPattern) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.repeat_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Első sor: Egyszeri, Naponta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = repeatPattern == RepeatPattern.None,
                    onClick = { onPatternChange(RepeatPattern.None) },
                    label = { Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.repeat_once)) },
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = repeatPattern == RepeatPattern.Daily,
                    onClick = { onPatternChange(RepeatPattern.Daily) },
                    label = { Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.repeat_daily)) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Második sor: Hétköznap, Hétvégén
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = repeatPattern == RepeatPattern.Weekdays,
                    onClick = { onPatternChange(RepeatPattern.Weekdays) },
                    label = { Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.repeat_weekdays)) },
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = repeatPattern == RepeatPattern.Weekends,
                    onClick = { onPatternChange(RepeatPattern.Weekends) },
                    label = { Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.repeat_weekends)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Megjegyzések hozzáadására szolgáló szekció.
 */
@Composable
private fun NotesSection(
    notes: String,
    onNotesChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.notes_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                placeholder = { Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.notes_placeholder)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )
        }
    }
}

/**
 * Alsó sáv a létrehozás gombbal.
 */
@Composable
private fun BottomBar(
    onCreateClick: () -> Unit,
    isCreating: Boolean,
    enabled: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onCreateClick,
                enabled = enabled && !isCreating,
                modifier = Modifier.weight(1f)
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.create_schedule_button))
                }
            }
        }
    }
}

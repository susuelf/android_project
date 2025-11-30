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
 * Create Schedule Screen
 * 
 * Új schedule létrehozása:
 * - Habit kiválasztás
 * - Dátum és időpont beállítás
 * - Ismétlődés pattern
 * - Duration
 * - Notes
 */
@Suppress("NewApi") // Java Time API is available via desugaring
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScheduleScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val scheduleRepository = remember { ScheduleRepository(tokenManager) }
    val habitRepository = remember { HabitRepository(tokenManager) }

    val viewModel: CreateScheduleViewModel = viewModel(
        factory = CreateScheduleViewModelFactory(scheduleRepository, habitRepository)
    )

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Habit lista újratöltése amikor visszanavigálunk az AddHabit-ból
    // NavBackStackEntry-t figyeljük, hogy tudjuk mikor jövünk vissza
    val navBackStackEntry = navController.currentBackStackEntry
    LaunchedEffect(navBackStackEntry) {
        // Amikor visszajövünk erre a screen-re, frissítsük a habit listát
        viewModel.loadHabits()
    }

    // Ha sikeres a létrehozás, navigáljunk vissza
    LaunchedEffect(uiState.createSuccess) {
        if (uiState.createSuccess) {
            navController.popBackStack()
        }
    }

    // Hibaüzenet megjelenítése
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

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
                enabled = uiState.selectedHabit != null
            )
        }
    ) { paddingValues ->
        CreateScheduleContent(
            uiState = uiState,
            viewModel = viewModel,
            navController = navController,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

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
        // Habit Selection Section
        item {
            HabitSelectionSection(
                habits = uiState.habits,
                selectedHabit = uiState.selectedHabit,
                isLoading = uiState.isLoadingHabits,
                onHabitSelected = { viewModel.selectHabit(it) },
                onAddNewHabit = { navController.navigate(com.progress.habittracker.navigation.Screen.AddHabit.route) }
            )
        }

        // Time Section
        if (uiState.selectedHabit != null) {
            item {
                TimeSection(
                    startTime = uiState.startTime,
                    duration = uiState.durationMinutes,
                    onStartTimeChange = { viewModel.setStartTime(it) },
                    onDurationChange = { viewModel.setDuration(it) }
                )
            }

            // Repeat Pattern Section
            item {
                RepeatPatternSection(
                    repeatPattern = uiState.repeatPattern,
                    onPatternChange = { viewModel.setRepeatPattern(it) }
                )
            }

            // Notes Section
            item {
                NotesSection(
                    notes = uiState.notes,
                    onNotesChange = { viewModel.setNotes(it) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HabitSelectionSection(
    habits: List<HabitResponseDto>,
    selectedHabit: HabitResponseDto?,
    isLoading: Boolean,
    onHabitSelected: (HabitResponseDto) -> Unit,
    onAddNewHabit: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
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
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedHabit?.name ?: androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.select_habit_placeholder),
                        onValueChange = {},
                        readOnly = true,
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

            // Start Time Picker
            OutlinedButton(
                onClick = {
                    android.app.TimePickerDialog(
                        context,
                        { _, hour, minute ->
                            onStartTimeChange(LocalTime.of(hour, minute))
                        },
                        startTime.hour,
                        startTime.minute,
                        true // 24-hour format
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

            // Duration Input
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

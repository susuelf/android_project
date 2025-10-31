package com.progress.habittracker.ui.screens.createschedule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
                title = { Text("Új Schedule") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Vissza"
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
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun CreateScheduleContent(
    uiState: CreateScheduleViewModel.CreateScheduleUiState,
    viewModel: CreateScheduleViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Habit Selection Section
        item {
            HabitSelectionSection(
                habits = uiState.habits,
                selectedHabit = uiState.selectedHabit,
                isLoading = uiState.isLoadingHabits,
                onHabitSelected = { viewModel.selectHabit(it) }
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
    onHabitSelected: (HabitResponseDto) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Habit",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

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
                        value = selectedHabit?.name ?: "Válassz habit-et",
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
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Időpont és időtartam",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Start Time Display
            Text(
                text = "Kezdés: ${startTime.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Duration Input
            var durationText by remember { mutableStateOf(duration?.toString() ?: "30") }
            OutlinedTextField(
                value = durationText,
                onValueChange = {
                    durationText = it
                    it.toIntOrNull()?.let { minutes -> onDurationChange(minutes) }
                },
                label = { Text("Időtartam (perc)") },
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
                text = "Ismétlődés",
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
                    label = { Text("Egyszeri") },
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = repeatPattern == RepeatPattern.Daily,
                    onClick = { onPatternChange(RepeatPattern.Daily) },
                    label = { Text("Napi") },
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
                    label = { Text("Hétköznap") },
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = repeatPattern == RepeatPattern.Weekends,
                    onClick = { onPatternChange(RepeatPattern.Weekends) },
                    label = { Text("Hétvége") },
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
                text = "Jegyzetek",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                placeholder = { Text("Opcionális jegyzetek...") },
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
                    Text("Schedule létrehozása")
                }
            }
        }
    }
}

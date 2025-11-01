package com.progress.habittracker.ui.screens.editschedule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
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
import com.progress.habittracker.data.model.ScheduleStatus
import com.progress.habittracker.data.repository.ScheduleRepository
import com.progress.habittracker.ui.viewmodel.EditScheduleViewModel
import com.progress.habittracker.ui.viewmodel.EditScheduleViewModelFactory
import java.time.format.DateTimeFormatter

/**
 * Edit Schedule Screen
 * 
 * Schedule szerkesztése
 * 
 * Funkciók:
 * - Dátum módosítása
 * - Kezdési/befejezési időpont módosítása
 * - Időtartam módosítása
 * - Státusz váltás (Planned/Completed/Skipped)
 * - Jegyzetek szerkesztése
 * - Mentés
 * 
 * @param navController Navigációs kontroller
 * @param scheduleId Schedule azonosító
 */
@Suppress("NewApi") // Java Time API is available via desugaring
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScheduleScreen(
    navController: NavController,
    scheduleId: Int
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val scheduleRepository = remember { ScheduleRepository(tokenManager) }
    
    val viewModel: EditScheduleViewModel = viewModel(
        factory = EditScheduleViewModelFactory(scheduleId, scheduleRepository)
    )
    
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Error handling
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }
    
    // Success navigation
    LaunchedEffect(uiState.updateSuccess) {
        if (uiState.updateSuccess) {
            navController.popBackStack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Schedule Szerkesztése") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Vissza")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Habit info (read-only)
                uiState.schedule?.let { schedule ->
                    HabitInfoCard(
                        habitName = schedule.habit.name,
                        categoryName = schedule.habit.category.name
                    )
                }
                
                // Dátum kártya
                DateCard(
                    date = uiState.date,
                    onDateChange = { viewModel.setDate(it) }
                )
                
                // Időpont kártya
                TimeCard(
                    startTime = uiState.startTime,
                    endTime = uiState.endTime,
                    onStartTimeChange = { viewModel.setStartTime(it) },
                    onEndTimeChange = { viewModel.setEndTime(it) }
                )
                
                // Időtartam
                DurationCard(
                    duration = uiState.durationMinutes,
                    onDurationChange = { viewModel.setDuration(it) }
                )
                
                // Státusz
                StatusCard(
                    status = uiState.status,
                    onStatusChange = { viewModel.setStatus(it) }
                )
                
                // Jegyzetek
                NotesCard(
                    notes = uiState.notes,
                    onNotesChange = { viewModel.setNotes(it) }
                )
                
                // Mentés gomb
                Button(
                    onClick = { viewModel.updateSchedule() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isUpdating
                ) {
                    if (uiState.isUpdating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Mentés")
                    }
                }
            }
        }
    }
}

/**
 * Habit info kártya (read-only)
 */
@Composable
private fun HabitInfoCard(
    habitName: String,
    categoryName: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = habitName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = categoryName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * Dátum kártya
 */
@Composable
private fun DateCard(
    date: java.time.LocalDate,
    onDateChange: (java.time.LocalDate) -> Unit
) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Dátum",
                style = MaterialTheme.typography.titleMedium
            )
            
            OutlinedButton(
                onClick = {
                    android.app.DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            onDateChange(java.time.LocalDate.of(year, month + 1, dayOfMonth))
                        },
                        date.year,
                        date.monthValue - 1,
                        date.dayOfMonth
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(date.format(DateTimeFormatter.ofPattern("yyyy. MMMM dd.")))
            }
        }
    }
}

/**
 * Időpont kártya
 */
@Composable
private fun TimeCard(
    startTime: java.time.LocalTime,
    endTime: java.time.LocalTime?,
    onStartTimeChange: (java.time.LocalTime) -> Unit,
    onEndTimeChange: (java.time.LocalTime?) -> Unit
) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Időpont",
                style = MaterialTheme.typography.titleMedium
            )
            
            // Kezdési időpont
            OutlinedButton(
                onClick = {
                    android.app.TimePickerDialog(
                        context,
                        { _, hour, minute ->
                            onStartTimeChange(java.time.LocalTime.of(hour, minute))
                        },
                        startTime.hour,
                        startTime.minute,
                        true
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Kezdés: ${startTime.format(DateTimeFormatter.ofPattern("HH:mm"))}")
            }
            
            // Befejezési időpont (opcionális)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Befejezés:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                if (endTime != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                android.app.TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        onEndTimeChange(java.time.LocalTime.of(hour, minute))
                                    },
                                    endTime.hour,
                                    endTime.minute,
                                    true
                                ).show()
                            }
                        ) {
                            Text(endTime.format(DateTimeFormatter.ofPattern("HH:mm")))
                        }
                        
                        TextButton(onClick = { onEndTimeChange(null) }) {
                            Text("Törlés")
                        }
                    }
                } else {
                    TextButton(
                        onClick = {
                            android.app.TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    onEndTimeChange(java.time.LocalTime.of(hour, minute))
                                },
                                startTime.hour,
                                startTime.minute,
                                true
                            ).show()
                        }
                    ) {
                        Text("+ Hozzáadás")
                    }
                }
            }
        }
    }
}

/**
 * Időtartam kártya
 */
@Composable
private fun DurationCard(
    duration: Int,
    onDurationChange: (Int) -> Unit
) {
    var durationText by remember(duration) { mutableStateOf(duration.toString()) }
    val isError = durationText.toIntOrNull() == null || durationText.toIntOrNull()!! <= 0
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Időtartam (perc)",
                style = MaterialTheme.typography.titleMedium
            )
            
            OutlinedTextField(
                value = durationText,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                        durationText = newValue
                        newValue.toIntOrNull()?.let { onDurationChange(it) }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Perc") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = isError,
                supportingText = if (isError) {
                    { Text("Érvényes számot adj meg (nagyobb mint 0)") }
                } else null
            )
        }
    }
}

/**
 * Státusz kártya
 */
@Composable
private fun StatusCard(
    status: ScheduleStatus,
    onStatusChange: (ScheduleStatus) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Státusz",
                style = MaterialTheme.typography.titleMedium
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = status == ScheduleStatus.Planned,
                    onClick = { onStatusChange(ScheduleStatus.Planned) },
                    label = { Text("Tervezett") }
                )
                
                FilterChip(
                    selected = status == ScheduleStatus.Completed,
                    onClick = { onStatusChange(ScheduleStatus.Completed) },
                    label = { Text("Kész") }
                )
                
                FilterChip(
                    selected = status == ScheduleStatus.Skipped,
                    onClick = { onStatusChange(ScheduleStatus.Skipped) },
                    label = { Text("Kihagyva") }
                )
            }
        }
    }
}

/**
 * Jegyzetek kártya
 */
@Composable
private fun NotesCard(
    notes: String,
    onNotesChange: (String) -> Unit
) {
    val maxLength = 500
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Jegyzetek",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = "${notes.length} / $maxLength",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (notes.length > maxLength) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            
            OutlinedTextField(
                value = notes,
                onValueChange = { newValue ->
                    if (newValue.length <= maxLength) {
                        onNotesChange(newValue)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                label = { Text("Jegyzetek (opcionális)") },
                maxLines = 5,
                supportingText = {
                    Text("Max ${maxLength} karakter")
                }
            )
        }
    }
}

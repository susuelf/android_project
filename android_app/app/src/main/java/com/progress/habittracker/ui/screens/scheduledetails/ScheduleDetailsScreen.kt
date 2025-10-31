package com.progress.habittracker.ui.screens.scheduledetails

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.progress.habittracker.data.model.ScheduleResponseDto
import com.progress.habittracker.data.model.ScheduleStatus
import com.progress.habittracker.data.repository.ScheduleRepository
import com.progress.habittracker.ui.viewmodel.ScheduleDetailsViewModel
import com.progress.habittracker.ui.viewmodel.ScheduleDetailsViewModelFactory
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Schedule Details Screen
 * 
 * Funkciók:
 * - Schedule részletes adatainak megjelenítése
 * - Habit információk
 * - Progress history lista
 * - Notes megjelenítése
 * - Progress bar
 * - Edit/Delete gombok
 * - Státusz váltás
 */
@Suppress("NewApi") // Java Time API is available via desugaring
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleDetailsScreen(
    navController: NavController,
    scheduleId: Int
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val scheduleRepository = remember { ScheduleRepository(tokenManager) }

    val viewModel: ScheduleDetailsViewModel = viewModel(
        factory = ScheduleDetailsViewModelFactory(scheduleRepository, scheduleId)
    )

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Ha sikeresen töröltük, navigáljunk vissza
    LaunchedEffect(uiState.deleteSuccess) {
        if (uiState.deleteSuccess) {
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
            viewModel.clearError()
        }
    }

    // Delete confirmation dialog state
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Schedule Részletek") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Vissza"
                        )
                    }
                },
                actions = {
                    // Edit gomb
                    IconButton(
                        onClick = {
                            // TODO: Navigáció Edit Schedule Screen-re
                            // navController.navigate(Screen.EditSchedule.createRoute(scheduleId))
                        },
                        enabled = !uiState.isDeleting
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Szerkesztés"
                        )
                    }

                    // Delete gomb
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        enabled = !uiState.isDeleting
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Törlés",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                LoadingState(modifier = Modifier.padding(paddingValues))
            }

            uiState.schedule != null -> {
                ScheduleDetailsContent(
                    schedule = uiState.schedule!!,
                    viewModel = viewModel,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            else -> {
                ErrorState(
                    message = "Nem sikerült betölteni a schedule-t",
                    onRetry = { viewModel.loadScheduleDetails() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Schedule törlése") },
            text = { Text("Biztosan törölni szeretnéd ezt a schedule-t?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteSchedule()
                    }
                ) {
                    Text("Törlés", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Mégse")
                }
            }
        )
    }
}

@Composable
private fun ScheduleDetailsContent(
    schedule: ScheduleResponseDto,
    viewModel: ScheduleDetailsViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Habit Information Section
        item {
            HabitInfoCard(schedule = schedule)
        }

        // Schedule Information Section
        item {
            ScheduleInfoCard(schedule = schedule)
        }

        // Progress Bar Section
        item {
            ProgressBarCard(
                progressPercentage = viewModel.calculateProgressPercentage(),
                completedCount = viewModel.getCompletedProgressCount(),
                totalCount = viewModel.getTotalProgressCount(),
                goal = schedule.habit.goal
            )
        }

        // Status Change Section
        item {
            val isUpdating by viewModel.uiState.collectAsState()
            StatusChangeCard(
                currentStatus = schedule.status,
                onStatusChange = { newStatus ->
                    viewModel.updateScheduleStatus(newStatus)
                },
                isUpdating = isUpdating.isUpdating
            )
        }

        // Notes Section
        if (!schedule.notes.isNullOrBlank()) {
            item {
                NotesCard(notes = schedule.notes)
            }
        }

        // Progress History Section
        if (!schedule.progress.isNullOrEmpty()) {
            item {
                Text(
                    text = "Progress History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            items(schedule.progress.sortedByDescending { it.date }) { progress ->
                ProgressItemCard(progress = progress)
            }
        }
    }
}

@Composable
private fun HabitInfoCard(schedule: ScheduleResponseDto) {
    val habit = schedule.habit

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Category
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Kategória:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = habit.category.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.height(4.dp))

            // Goal
            if (habit.goal != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Cél:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${habit.goal} alkalom",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Description
            if (!habit.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = habit.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun ScheduleInfoCard(schedule: ScheduleResponseDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Schedule Információk",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Date
            InfoRow(label = "Dátum", value = formatDate(schedule.date))

            // Time
            val timeRange = "${formatTime(schedule.startTime)} - ${formatTime(schedule.endTime)}"
            InfoRow(label = "Időpont", value = timeRange)

            // Duration
            InfoRow(label = "Időtartam", value = "${schedule.durationMinutes} perc")

            // Custom Schedule
            if (schedule.isCustom == true) {
                InfoRow(label = "Típus", value = "Egyedi schedule")
            }

            // Participants
            if (!schedule.participants.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Résztvevők:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                schedule.participants.forEach { participant ->
                    Text(
                        text = "• ${participant.username}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ProgressBarCard(
    progressPercentage: Float,
    completedCount: Int,
    totalCount: Int,
    goal: String?
) {
    val goalInt = goal?.toIntOrNull()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
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
                    text = "Haladás",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${progressPercentage.toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progressPercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$completedCount / ${goalInt ?: totalCount} befejezve",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun StatusChangeCard(
    currentStatus: ScheduleStatus,
    onStatusChange: (ScheduleStatus) -> Unit,
    isUpdating: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Státusz váltás",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Planned
                FilterChip(
                    selected = currentStatus == ScheduleStatus.Planned,
                    onClick = { onStatusChange(ScheduleStatus.Planned) },
                    label = { Text("Tervezett") },
                    enabled = !isUpdating,
                    modifier = Modifier.weight(1f)
                )

                // Completed
                FilterChip(
                    selected = currentStatus == ScheduleStatus.Completed,
                    onClick = { onStatusChange(ScheduleStatus.Completed) },
                    label = { Text("Kész") },
                    enabled = !isUpdating,
                    modifier = Modifier.weight(1f)
                )

                // Skipped
                FilterChip(
                    selected = currentStatus == ScheduleStatus.Skipped,
                    onClick = { onStatusChange(ScheduleStatus.Skipped) },
                    label = { Text("Kihagyva") },
                    enabled = !isUpdating,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NotesCard(notes: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Jegyzetek",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = notes,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Újrapróbálás")
            }
        }
    }
}

// ===== HELPER FUNCTIONS =====

private fun formatDate(dateString: String?): String {
    if (dateString == null) return "N/A"
    return try {
        val date = LocalDate.parse(dateString)
        val formatter = DateTimeFormatter.ofPattern("yyyy. MMM. dd.")
        date.format(formatter)
    } catch (_: Exception) {
        dateString
    }
}

private fun formatTime(timeString: String?): String {
    if (timeString == null) return "N/A"
    return try {
        val time = LocalTime.parse(timeString)
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        time.format(formatter)
    } catch (_: Exception) {
        timeString
    }
}

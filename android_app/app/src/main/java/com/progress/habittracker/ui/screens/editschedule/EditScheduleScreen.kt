package com.progress.habittracker.ui.screens.editschedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.progress.habittracker.data.local.TokenManager
import com.progress.habittracker.data.model.ScheduleStatus
import com.progress.habittracker.data.repository.ScheduleRepository
import com.progress.habittracker.ui.theme.DarkBackground
import com.progress.habittracker.ui.theme.DarkSurface
import com.progress.habittracker.ui.theme.PrimaryPurple
import com.progress.habittracker.ui.theme.SuccessCyan
import com.progress.habittracker.ui.theme.TextPrimary
import com.progress.habittracker.ui.theme.TextSecondary
import com.progress.habittracker.ui.theme.TextTertiary
import com.progress.habittracker.ui.viewmodel.EditScheduleViewModel
import com.progress.habittracker.ui.viewmodel.EditScheduleViewModelFactory
import java.time.format.DateTimeFormatter

/**
 * Edit Schedule Screen - Dark Theme
 * 
 * Schedule szerkesztése dark theme-el
 * 
 * Funkciók (spec szerint):
 * - Start Time és End Time módosítása
 * - Duration (időtartam) módosítása
 * - Status beállítása: Planned, Completed, Skipped
 * - Participants/Partners hozzáadása és eltávolítása
 * - Notes szerkesztése
 * - Mentés (PATCH /schedule/{id})
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
                title = { Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.edit_schedule_title), color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.back),
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground
    ) { paddingValues ->
        
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(DarkBackground),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryPurple)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(DarkBackground)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Habit info (read-only)
                uiState.schedule?.let { schedule ->
                    HabitInfoCard(
                        habitName = schedule.habit.name,
                        categoryName = schedule.habit.category.name
                    )
                }
                
                // Időpont kártya (Start & End Time)
                TimeCard(
                    startTime = uiState.startTime,
                    endTime = uiState.endTime,
                    onStartTimeChange = { viewModel.setStartTime(it) }
                )
                
                // Időtartam (Duration)
                DurationCard(
                    duration = uiState.durationMinutes,
                    onDurationChange = { viewModel.setDuration(it) }
                )
                
                // Státusz (Status: Planned/Completed/Skipped)
                StatusCard(
                    status = uiState.status,
                    onStatusChange = { viewModel.setStatus(it) }
                )
                
                // Jegyzetek (Notes)
                NotesCard(
                    notes = uiState.notes,
                    onNotesChange = { viewModel.setNotes(it) }
                )
                
                // Résztvevők/Partnerek (Participants/Partners)
                uiState.schedule?.let { schedule ->
                    ParticipantsCard(
                        participants = schedule.participants ?: emptyList(),
                        onRemoveParticipant = { participantId ->
                            viewModel.removeParticipant(participantId)
                        }
                    )
                }
                
                // Mentés gomb
                Button(
                    onClick = { viewModel.updateSchedule() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isUpdating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryPurple,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isUpdating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.save_changes), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                
                // Extra padding
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

/**
 * Habit info kártya (read-only) - Dark Theme
 */
@Composable
private fun HabitInfoCard(
    habitName: String,
    categoryName: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = habitName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 20.sp
            )
            Text(
                text = categoryName,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                fontSize = 14.sp
            )
        }
    }
}

/**
 * Időpont kártya - Dark Theme
 * Start Time szerkeszthető, End Time automatikusan számolódik (duration alapján)
 */
@Composable
private fun TimeCard(
    startTime: java.time.LocalTime,
    endTime: java.time.LocalTime?,
    onStartTimeChange: (java.time.LocalTime) -> Unit
) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.schedule_time),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 18.sp
            )
            
            // Start Time (szerkeszthető)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.start_time),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    fontSize = 14.sp
                )
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
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextPrimary,
                        containerColor = DarkBackground
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = TextTertiary.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = SuccessCyan
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = startTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // End Time (read-only, automatikusan számolódik)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.end_time),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.auto_calculated),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary,
                        fontSize = 11.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
                
                // Read-only End Time megjelenítés
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            DarkBackground.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = TextTertiary
                        )
                        Text(
                            text = endTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "--:--",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Időtartam kártya - Dark Theme
 * Duration in minutes (max 480 perc = 8 óra)
 */
@Composable
private fun DurationCard(
    duration: Int,
    onDurationChange: (Int) -> Unit
) {
    val maxDuration = 480 // Max 8 óra (480 perc)
    var durationText by remember(duration) { mutableStateOf(duration.toString()) }
    val durationValue = durationText.toIntOrNull()
    val isError = durationValue == null || durationValue <= 0 || durationValue > maxDuration
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.duration),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 18.sp
            )
            
            OutlinedTextField(
                value = durationText,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                        durationText = newValue
                        newValue.toIntOrNull()?.let { value ->
                            if (value in 1..maxDuration) {
                                onDurationChange(value)
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.minutes), color = TextTertiary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = isError,
                supportingText = {
                    when {
                        durationValue == null || durationValue <= 0 -> 
                            Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.duration_error_invalid), color = MaterialTheme.colorScheme.error)
                        durationValue > maxDuration -> 
                            Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.duration_error_max, maxDuration, maxDuration/60), color = MaterialTheme.colorScheme.error)
                        else -> 
                            Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.duration_hint, maxDuration, maxDuration/60), color = TextTertiary, fontSize = 11.sp)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkBackground,
                    unfocusedContainerColor = DarkBackground,
                    focusedBorderColor = PrimaryPurple,
                    unfocusedBorderColor = TextTertiary.copy(alpha = 0.3f),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = PrimaryPurple,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    errorTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

/**
 * Státusz kártya - Dark Theme
 * Status: Planned, Completed, Skipped (spec szerint)
 */
@Composable
private fun StatusCard(
    status: ScheduleStatus,
    onStatusChange: (ScheduleStatus) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.status),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 18.sp
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = status == ScheduleStatus.Planned,
                    onClick = { onStatusChange(ScheduleStatus.Planned) },
                    label = { 
                        Text(
                            androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.status_planned),
                            fontSize = 14.sp,
                            fontWeight = if (status == ScheduleStatus.Planned) FontWeight.SemiBold else FontWeight.Normal
                        ) 
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = DarkBackground,
                        selectedContainerColor = PrimaryPurple.copy(alpha = 0.2f),
                        labelColor = TextSecondary,
                        selectedLabelColor = PrimaryPurple
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = status == ScheduleStatus.Planned,
                        borderColor = TextTertiary.copy(alpha = 0.3f),
                        selectedBorderColor = PrimaryPurple,
                        borderWidth = 1.5.dp,
                        selectedBorderWidth = 1.5.dp
                    )
                )
                
                FilterChip(
                    selected = status == ScheduleStatus.Completed,
                    onClick = { onStatusChange(ScheduleStatus.Completed) },
                    label = { 
                        Text(
                            androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.status_completed),
                            fontSize = 14.sp,
                            fontWeight = if (status == ScheduleStatus.Completed) FontWeight.SemiBold else FontWeight.Normal
                        ) 
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = DarkBackground,
                        selectedContainerColor = SuccessCyan.copy(alpha = 0.2f),
                        labelColor = TextSecondary,
                        selectedLabelColor = SuccessCyan
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = status == ScheduleStatus.Completed,
                        borderColor = TextTertiary.copy(alpha = 0.3f),
                        selectedBorderColor = SuccessCyan,
                        borderWidth = 1.5.dp,
                        selectedBorderWidth = 1.5.dp
                    )
                )
                
                FilterChip(
                    selected = status == ScheduleStatus.Skipped,
                    onClick = { onStatusChange(ScheduleStatus.Skipped) },
                    label = { 
                        Text(
                            androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.status_skipped),
                            fontSize = 14.sp,
                            fontWeight = if (status == ScheduleStatus.Skipped) FontWeight.SemiBold else FontWeight.Normal
                        ) 
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = DarkBackground,
                        selectedContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                        labelColor = TextSecondary,
                        selectedLabelColor = MaterialTheme.colorScheme.error
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = status == ScheduleStatus.Skipped,
                        borderColor = TextTertiary.copy(alpha = 0.3f),
                        selectedBorderColor = MaterialTheme.colorScheme.error,
                        borderWidth = 1.5.dp,
                        selectedBorderWidth = 1.5.dp
                    )
                )
            }
        }
    }
}

/**
 * Jegyzetek kártya - Dark Theme
 * Notes editing (spec szerint)
 */
@Composable
private fun NotesCard(
    notes: String,
    onNotesChange: (String) -> Unit
) {
    val maxLength = 500
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.notes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 18.sp
                )
                
                Text(
                    text = "${notes.length} / $maxLength",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (notes.length > maxLength) {
                        MaterialTheme.colorScheme.error
                    } else {
                        TextTertiary
                    },
                    fontSize = 12.sp
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
                    .height(140.dp),
                placeholder = { Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.notes_placeholder), color = TextTertiary) },
                maxLines = 6,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkBackground,
                    unfocusedContainerColor = DarkBackground,
                    focusedBorderColor = PrimaryPurple,
                    unfocusedBorderColor = TextTertiary.copy(alpha = 0.3f),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = PrimaryPurple
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

/**
 * Résztvevők/Partnerek kártya - Dark Theme
 * Participants/Partners management (spec szerint)
 */
@Composable
private fun ParticipantsCard(
    participants: List<com.progress.habittracker.data.model.ParticipantResponseDto>,
    onRemoveParticipant: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.partners),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 18.sp
            )
            
            if (participants.isEmpty()) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.no_partners),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary,
                    fontSize = 14.sp
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    participants.forEach { participant ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    DarkBackground,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Avatar
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            PrimaryPurple.copy(alpha = 0.3f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = participant.username.take(1).uppercase(),
                                        color = PrimaryPurple,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                                
                                Column {
                                    Text(
                                        text = participant.username,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                    if (participant.email.isNotEmpty()) {
                                        Text(
                                            text = participant.email,
                                            color = TextTertiary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                            
                            IconButton(
                                onClick = { onRemoveParticipant(participant.id) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.remove_partner),
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // Note: Add partner functionality jelenleg nincs implementálva a backend-ben
            Text(
                text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.partners_note),
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary,
                fontSize = 11.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

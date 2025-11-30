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
 * Edit Schedule Screen - Időbeosztás szerkesztése
 *
 * Ez a képernyő teszi lehetővé egy meglévő időbeosztás (schedule) részleteinek módosítását.
 *
 * Főbb funkciók:
 * - Kezdési időpont (Start Time) módosítása.
 * - Tervezett időtartam (Duration) módosítása.
 * - Státusz beállítása (Tervezett, Befejezett, Kihagyott).
 * - Megjegyzések (Notes) szerkesztése.
 * - Résztvevők/Partnerek kezelése (eltávolítás).
 * - Változtatások mentése a szerverre.
 *
 * @param navController A navigációért felelős vezérlő.
 * @param scheduleId A szerkesztendő időbeosztás azonosítója.
 */
@Suppress("NewApi") // Java Time API használata miatt (desugaring támogatott)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScheduleScreen(
    navController: NavController,
    scheduleId: Int
) {
    // Kontextus és függőségek inicializálása
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val scheduleRepository = remember { ScheduleRepository(tokenManager) }
    
    // ViewModel létrehozása a Factory segítségével, átadva a scheduleId-t
    val viewModel: EditScheduleViewModel = viewModel(
        factory = EditScheduleViewModelFactory(scheduleId, scheduleRepository)
    )
    
    // UI állapot figyelése a ViewModel-ből
    val uiState by viewModel.uiState.collectAsState()
    
    // Snackbar állapot a hibaüzenetek megjelenítéséhez
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Hibaüzenetek kezelése
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }
    
    // Sikeres frissítés esetén visszanavigálás
    LaunchedEffect(uiState.updateSuccess) {
        if (uiState.updateSuccess) {
            navController.popBackStack()
        }
    }
    
    // Scaffold: Az alapvető képernyőszerkezet
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
        
        // Töltésjelző megjelenítése, ha az adatok még töltődnek
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
            // Tartalom megjelenítése
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(DarkBackground)
                    .verticalScroll(rememberScrollState()) // Görgethető tartalom
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. Szokás információk (csak olvasható)
                uiState.schedule?.let { schedule ->
                    HabitInfoCard(
                        habitName = schedule.habit.name,
                        categoryName = schedule.habit.category.name
                    )
                }
                
                // 2. Időpont kártya (Kezdés és Befejezés)
                TimeCard(
                    startTime = uiState.startTime,
                    endTime = uiState.endTime,
                    onStartTimeChange = { viewModel.setStartTime(it) }
                )
                
                // 3. Időtartam kártya
                DurationCard(
                    duration = uiState.durationMinutes,
                    onDurationChange = { viewModel.setDuration(it) }
                )
                
                // 4. Státusz kártya (Tervezett/Befejezett/Kihagyott)
                StatusCard(
                    status = uiState.status,
                    onStatusChange = { viewModel.setStatus(it) }
                )
                
                // 5. Jegyzetek kártya
                NotesCard(
                    notes = uiState.notes,
                    onNotesChange = { viewModel.setNotes(it) }
                )
                
                // 6. Résztvevők/Partnerek kártya
                uiState.schedule?.let { schedule ->
                    ParticipantsCard(
                        participants = schedule.participants ?: emptyList(),
                        onRemoveParticipant = { participantId ->
                            viewModel.removeParticipant(participantId)
                        }
                    )
                }
                
                // 7. Mentés gomb
                Button(
                    onClick = { viewModel.updateSchedule() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isUpdating, // Letiltva mentés közben
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
                
                // Extra térköz az alján
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

/**
 * Szokás információit megjelenítő kártya (csak olvasható).
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
 * Időpontok beállítására szolgáló kártya.
 * A kezdési idő szerkeszthető, a befejezési idő automatikusan számolódik az időtartam alapján.
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
            
            // Kezdési idő (szerkeszthető TimePickerDialog-gal)
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
                            true // 24 órás formátum
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
            
            // Befejezési idő (csak olvasható, automatikusan számolódik)
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
 * Időtartam beállítására szolgáló kártya.
 * Percben adható meg, maximum 480 perc (8 óra).
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
                    // Csak számokat engedünk beírni
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
                    // Hibaüzenetek vagy segédszöveg megjelenítése
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
 * Státusz beállítására szolgáló kártya.
 * Lehetséges értékek: Tervezett (Planned), Befejezett (Completed), Kihagyott (Skipped).
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
            
            // FilterChip-ek a státusz kiválasztásához
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Planned Chip
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
                
                // Completed Chip
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
                
                // Skipped Chip
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
 * Megjegyzések szerkesztésére szolgáló kártya.
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
 * Résztvevők/Partnerek kezelésére szolgáló kártya.
 * Listázza a résztvevőket és lehetőséget ad az eltávolításukra.
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
                                // Avatar (Kezdőbetű)
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
                            
                            // Törlés gomb
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
            
            // Megjegyzés: Új partner hozzáadása jelenleg nem támogatott ezen a felületen
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

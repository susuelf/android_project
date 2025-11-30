package com.progress.habittracker.ui.screens.scheduledetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.progress.habittracker.data.local.TokenManager
import com.progress.habittracker.data.model.ScheduleResponseDto
import com.progress.habittracker.data.model.ScheduleStatus
import com.progress.habittracker.data.repository.ScheduleRepository
import com.progress.habittracker.ui.theme.DarkBackground
import com.progress.habittracker.ui.theme.DarkSurface
import com.progress.habittracker.ui.theme.HabitBlue
import com.progress.habittracker.ui.theme.PrimaryPurple
import com.progress.habittracker.ui.theme.SuccessCyan
import com.progress.habittracker.ui.theme.TextPrimary
import com.progress.habittracker.ui.theme.TextSecondary
import com.progress.habittracker.ui.theme.TextTertiary
import com.progress.habittracker.ui.viewmodel.ScheduleDetailsViewModel
import com.progress.habittracker.ui.viewmodel.ScheduleDetailsViewModelFactory
import com.progress.habittracker.navigation.Screen
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Schedule Details Screen - Időbeosztás részletei
 *
 * Ez a képernyő jeleníti meg egy adott időbeosztás (schedule) részletes adatait.
 *
 * Főbb komponensek:
 * 1. Habit Info Card: Szokás neve, időtartam, státusz, résztvevők.
 * 2. Progress Bar Card: Teljesítési arány vizualizációja.
 * 3. Notes Card: Jegyzetek megjelenítése és szerkesztése.
 * 4. Recent Activity Card: Aznapi egyéb időbeosztások listája.
 *
 * Funkciók:
 * - Szerkesztés és Törlés menü (Top Bar).
 * - Új előrehaladás (Progress) hozzáadása (FAB).
 * - Jegyzetek szerkesztése dialógusablakban.
 * - Törlés megerősítése dialógusablakban.
 *
 * @param navController A navigációért felelős vezérlő.
 * @param scheduleId A megjelenítendő időbeosztás azonosítója.
 */
@Suppress("NewApi") // Java Time API használata miatt (desugaring támogatott)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleDetailsScreen(
    navController: NavController,
    scheduleId: Int
) {
    // Kontextus és függőségek inicializálása
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val scheduleRepository = remember { ScheduleRepository(tokenManager) }

    // ViewModel létrehozása a Factory segítségével
    val viewModel: ScheduleDetailsViewModel = viewModel(
        factory = ScheduleDetailsViewModelFactory(scheduleRepository, scheduleId)
    )

    // UI állapot figyelése a ViewModel-ből
    val uiState by viewModel.uiState.collectAsState()
    
    // Snackbar állapot a hibaüzenetek megjelenítéséhez
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Adatok frissítése, amikor visszatérünk erre a képernyőre (pl. AddProgress-ből)
    val navBackStackEntry = navController.currentBackStackEntry
    LaunchedEffect(navBackStackEntry) {
        viewModel.refreshSchedule()
    }

    // Sikeres törlés esetén visszanavigálás
    LaunchedEffect(uiState.deleteSuccess) {
        if (uiState.deleteSuccess) {
            navController.popBackStack()
        }
    }

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

    // Dialógusok és menü állapotai
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditNotesDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    // Scaffold: Az alapvető képernyőszerkezet
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.schedule_details_title), color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.back),
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    // Menü gomb (három pont)
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            enabled = !uiState.isDeleting
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.menu),
                                tint = TextPrimary
                            )
                        }
                        
                        // Legördülő menü: Szerkesztés és Törlés
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.edit_schedule)) },
                                onClick = {
                                    showMenu = false
                                    navController.navigate(Screen.EditSchedule.createRoute(scheduleId))
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.delete_schedule)) },
                                onClick = {
                                    showMenu = false
                                    showDeleteDialog = true
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete, 
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        floatingActionButton = {
            // FAB: Új előrehaladás hozzáadása
            ExtendedFloatingActionButton(
                onClick = {
                    navController.navigate(Screen.AddProgress.createRoute(scheduleId))
                },
                containerColor = SuccessCyan,
                contentColor = DarkBackground,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.add_progress)
                    )
                },
                text = {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.add_progress),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground
    ) { paddingValues ->
        // Tartalom megjelenítése állapot szerint
        when {
            uiState.isLoading -> {
                LoadingState(modifier = Modifier.padding(paddingValues))
            }

            uiState.schedule != null -> {
                ScheduleDetailsContent(
                    schedule = uiState.schedule!!,
                    daySchedules = uiState.daySchedules,
                    viewModel = viewModel,
                    onEditNotesClick = { showEditNotesDialog = true },
                    modifier = Modifier.padding(paddingValues)
                )
            }

            else -> {
                ErrorState(
                    message = "Could not load schedule",
                    onRetry = { viewModel.loadScheduleDetails() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }

    // Törlés megerősítése dialógus
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Schedule") },
            text = { Text("Are you sure you want to delete this schedule?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteSchedule()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Jegyzetek szerkesztése dialógus
    if (showEditNotesDialog) {
        val schedule = uiState.schedule
        if (schedule != null) {
            EditNotesDialog(
                currentNotes = schedule.notes ?: "",
                onDismiss = { showEditNotesDialog = false },
                onSave = { newNotes ->
                    viewModel.updateNotes(newNotes)
                    showEditNotesDialog = false
                },
                isLoading = uiState.isUpdating
            )
        }
    }
}

/**
 * A képernyő tartalmát megjelenítő komponens.
 * Listázza a különböző információs kártyákat.
 */
@Composable
private fun ScheduleDetailsContent(
    schedule: ScheduleResponseDto,
    daySchedules: List<ScheduleResponseDto>,
    viewModel: ScheduleDetailsViewModel,
    onEditNotesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Szokás információk kártya
        item {
            HabitInfoCard(schedule = schedule)
        }

        // 2. Előrehaladás (Progress Bar) kártya
        item {
            ProgressBarCard(schedule = schedule, viewModel = viewModel)
        }

        // 3. Jegyzetek kártya
        item {
            NotesCard(
                notes = schedule.notes,
                onEditNotes = onEditNotesClick
            )
        }

        // 4. Napi aktivitás kártya
        item {
            RecentActivityCard(
                schedule = schedule,
                daySchedules = daySchedules
            )
        }
        
        // Extra térköz a FAB miatt
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

/**
 * 1. Habit Information Card
 *
 * Megjeleníti a szokás alapvető adatait:
 * - Ikon és név.
 * - Időtartam (Kezdés - Befejezés).
 * - Tervezett hossz és státusz.
 * - Résztvevők.
 * - Ismétlődés típusa.
 */
@Composable
private fun HabitInfoCard(schedule: ScheduleResponseDto) {
    val habit = schedule.habit
    
    // UI állapot számítása a központosított kalkulátorral
    val uiState = com.progress.habittracker.util.ScheduleStateCalculator.calculate(schedule)
    val displayStatus = if (uiState.isChecked) ScheduleStatus.Completed else schedule.status

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Habit ikon (színes kör + betű/emoji)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(HabitBlue.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = habit.name.firstOrNull()?.uppercase() ?: "H",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = HabitBlue,
                    fontSize = 28.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Habit név
            Text(
                text = habit.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 22.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Időtartam (pl. 1:30 PM - 3:30 PM)
            Text(
                text = "${formatTimeAMPM(schedule.startTime)} – ${formatTimeAMPM(schedule.endTime)}",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Időtartam és Státusz sor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Duration",
                        color = TextTertiary,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${schedule.durationMinutes ?: 0}m",
                        color = TextPrimary,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
                
                // Státusz jelvény (Badge)
                Surface(
                    color = when (displayStatus) {
                        ScheduleStatus.Completed -> SuccessCyan.copy(alpha = 0.2f)
                        ScheduleStatus.Planned -> PrimaryPurple.copy(alpha = 0.2f)
                        ScheduleStatus.Skipped -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = when (displayStatus) {
                            ScheduleStatus.Completed -> "Completed"
                            ScheduleStatus.Planned -> "Planned"
                            ScheduleStatus.Skipped -> "Skipped"
                        },
                        color = when (displayStatus) {
                            ScheduleStatus.Completed -> SuccessCyan
                            ScheduleStatus.Planned -> PrimaryPurple
                            ScheduleStatus.Skipped -> MaterialTheme.colorScheme.error
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Résztvevők megjelenítése (ha vannak)
            if (!schedule.participants.isNullOrEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "With partner(s)",
                        color = TextTertiary,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Partner avatar (kis kör)
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(HabitBlue.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = schedule.participants.first().username.firstOrNull()?.uppercase() ?: "?",
                                color = HabitBlue,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Text(
                            text = schedule.participants.first().username,
                            color = TextPrimary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Ismétlődés típusa
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Repeat",
                    color = TextTertiary,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp
                )
                
                Text(
                    text = "recurring", // Jelenleg statikus, később dinamikus lehet
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }
}

/**
 * 2. Progress Bar Card
 *
 * Megjeleníti a teljesítési arányt százalékban és vizuálisan (progress bar).
 * Összehasonlítja az eltöltött időt a tervezett időtartammal.
 */
@Composable
private fun ProgressBarCard(
    schedule: ScheduleResponseDto,
    viewModel: ScheduleDetailsViewModel
) {
    // Összes eltöltött idő kiszámítása a progressekből
    val totalLoggedTime = viewModel.getTotalLoggedTime()
    val scheduleDuration = schedule.durationMinutes ?: 0

    // Százalék számítás a ViewModel-ből
    val uiState = viewModel.getScheduleUiState()
    val percentage = uiState.progressPercentage

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Címsor: "Progress" + százalék
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 18.sp
                )
                
                Text(
                    text = "${percentage.toInt()}%",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = SuccessCyan,
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress indicator (csík)
            LinearProgressIndicator(
                progress = { percentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = SuccessCyan,
                trackColor = DarkBackground,
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Eltöltött idő / Tervezett idő szöveg
            Text(
                text = "$totalLoggedTime / $scheduleDuration minutes",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary,
                fontSize = 12.sp
            )
        }
    }
}

/**
 * 3. Notes Card
 *
 * Megjeleníti a jegyzeteket és lehetőséget ad a szerkesztésükre.
 */
@Composable
private fun NotesCard(
    notes: String?,
    onEditNotes: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Notes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 18.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Jegyzetek szövege vagy placeholder
            Text(
                text = notes ?: "No notes added yet",
                style = MaterialTheme.typography.bodyMedium,
                color = if (notes != null) TextSecondary else TextTertiary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Szerkesztés gomb
            TextButton(
                onClick = onEditNotes,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = SuccessCyan
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Edit Notes",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }
}

/**
 * 4. Recent Activity Card (Daily Schedule)
 *
 * Megjeleníti az adott napra vonatkozó egyéb időbeosztásokat.
 */
@Composable
private fun RecentActivityCard(
    schedule: ScheduleResponseDto,
    daySchedules: List<ScheduleResponseDto>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.daily_schedule_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 18.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Más schedule-ök listázása
            if (daySchedules.isNotEmpty()) {
                daySchedules.forEach { daySchedule ->
                    ScheduleActivityItem(schedule = daySchedule)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            } else {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.no_other_schedules),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary,
                    fontSize = 14.sp
                )
            }
        }
    }
}

/**
 * Schedule Activity Item - Egyetlen schedule megjelenítése a listában.
 */
@Composable
private fun ScheduleActivityItem(
    schedule: ScheduleResponseDto
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Státusz ikon
            Icon(
                imageVector = when (schedule.status) {
                    ScheduleStatus.Completed -> Icons.Default.Edit // Placeholder ikon
                    ScheduleStatus.Planned -> Icons.Default.MoreVert // Placeholder ikon
                    ScheduleStatus.Skipped -> Icons.Default.Delete // Placeholder ikon
                },
                contentDescription = null,
                tint = when (schedule.status) {
                    ScheduleStatus.Completed -> SuccessCyan
                    ScheduleStatus.Planned -> PrimaryPurple
                    ScheduleStatus.Skipped -> MaterialTheme.colorScheme.error
                },
                modifier = Modifier.size(20.dp)
            )
            
            // Habit név + időpont
            Column {
                Text(
                    text = schedule.habit.name,
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Text(
                    text = "${formatTimeAMPM(schedule.startTime)} - ${schedule.status.name}",
                    color = TextTertiary,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp
                )
            }
        }
        
        // Időtartam
        Text(
            text = "${schedule.durationMinutes ?: 0}m",
            color = when (schedule.status) {
                ScheduleStatus.Completed -> SuccessCyan
                ScheduleStatus.Planned -> TextSecondary
                ScheduleStatus.Skipped -> MaterialTheme.colorScheme.error
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

/**
 * Töltés állapotot megjelenítő komponens.
 */
@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Hiba állapotot megjelenítő komponens.
 */
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

// ===== SEGÉDFÜGGVÉNYEK =====

/**
 * 24-órás formátumot AM/PM formátumra alakít.
 * Pl. "14:30" -> "2:30 PM"
 */
private fun formatTimeAMPM(time: String?): String {
    if (time.isNullOrEmpty()) return ""
    
    return try {
        // ISO formátum kezelése
        val timeString = if (time.contains("T")) {
            time.substringAfter("T").substringBefore(":")
                .let { hour ->
                    val minute = time.substringAfter("T").substringAfter(":").substringBefore(":")
                    "$hour:$minute"
                }
        } else {
            time
        }
        
        val parts = timeString.split(":")
        val hour = parts[0].toInt()
        val minute = parts.getOrNull(1) ?: "00"
        
        val amPm = if (hour >= 12) "PM" else "AM"
        val displayHour = when (hour) {
            0 -> 12
            in 1..12 -> hour
            else -> hour - 12
        }
        
        "$displayHour:$minute $amPm"
    } catch (e: Exception) {
        time // Hiba esetén visszaadjuk az eredeti stringet
    }
}

/**
 * Dátum formázás időponttal.
 * Pl. "2025-10-31" -> "Oct 31, 2:30 PM"
 */
private fun formatDateWithTime(date: String?, createdAt: String?): String {
    val dateStr = formatDate(date)
    val timeStr = if (createdAt != null) {
        formatTimeAMPM(createdAt)
    } else {
        ""
    }
    
    return if (timeStr.isNotEmpty()) {
        "$dateStr, $timeStr"
    } else {
        dateStr
    }
}

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

// ===== EDIT NOTES DIALOG =====

/**
 * Edit Notes Dialog - Jegyzetek szerkesztése
 *
 * Egy felugró ablak, ahol a felhasználó módosíthatja a jegyzeteket.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditNotesDialog(
    currentNotes: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    isLoading: Boolean
) {
    var notes by remember { mutableStateOf(currentNotes) }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        containerColor = DarkSurface,
        title = {
            Text(
                text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.edit_notes_title),
                color = TextPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                placeholder = {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.edit_notes_placeholder),
                        color = TextTertiary
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkBackground,
                    unfocusedContainerColor = DarkBackground,
                    focusedBorderColor = PrimaryPurple,
                    unfocusedBorderColor = TextTertiary.copy(alpha = 0.3f),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = PrimaryPurple
                ),
                enabled = !isLoading,
                maxLines = 8,
                shape = RoundedCornerShape(12.dp)
            )
        },
        confirmButton = {
            Button(
                onClick = { onSave(notes) },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryPurple,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.save))
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.cancel),
                    color = TextSecondary
                )
            }
        }
    )
}

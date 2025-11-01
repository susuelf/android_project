package com.progress.habittracker.ui.screens.addprogress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.progress.habittracker.data.local.TokenManager
import com.progress.habittracker.data.repository.ProgressRepository
import com.progress.habittracker.data.repository.ScheduleRepository
import com.progress.habittracker.ui.theme.DarkBackground
import com.progress.habittracker.ui.theme.DarkSurface
import com.progress.habittracker.ui.theme.PrimaryPurple
import com.progress.habittracker.ui.theme.SuccessCyan
import com.progress.habittracker.ui.theme.TextPrimary
import com.progress.habittracker.ui.theme.TextSecondary
import com.progress.habittracker.ui.theme.TextTertiary
import com.progress.habittracker.ui.viewmodel.AddProgressViewModel
import com.progress.habittracker.ui.viewmodel.AddProgressViewModelFactory
import java.time.format.DateTimeFormatter

/**
 * Add Progress Screen - Dark Theme
 * 
 * Update your progress for a schedule
 * 
 * Funkciók (spec szerint):
 * - Log time spent on activity (kötelező)
 * - Add optional notes
 * - Counts towards the completion of a schedule based on time
 * - Progress = completed work (isCompleted mindig true)
 * 
 * Backend: POST /progress
 * Required: scheduleId, date, logged_time
 * Optional: notes
 * Fixed: is_completed = true
 * 
 * @param navController Navigációs kontroller
 * @param scheduleId Schedule azonosító
 */
@Suppress("NewApi") // Java Time API is available via desugaring
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProgressScreen(
    navController: NavController,
    scheduleId: Int
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val scheduleRepository = remember { ScheduleRepository(tokenManager) }
    val progressRepository = remember { ProgressRepository(tokenManager) }
    
    val viewModel: AddProgressViewModel = viewModel(
        factory = AddProgressViewModelFactory(scheduleId, scheduleRepository, progressRepository)
    )
    
    val uiState by viewModel.uiState.collectAsState()
    
    // Snackbar host
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
    LaunchedEffect(uiState.createSuccess) {
        if (uiState.createSuccess) {
            navController.popBackStack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Progress", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(DarkBackground)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Info header
            Text(
                text = "Update your progress",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 22.sp
            )
            
            Text(
                text = "Log the time you spent on this activity. Progress represents actual completed work.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Logged Time (kötelező) - max a hátralévő idő
            val maxDuration = uiState.maxAllowedTime
            LoggedTimeCard(
                loggedTime = uiState.loggedTime,
                maxDuration = maxDuration,
                onLoggedTimeChange = { viewModel.setLoggedTime(it) }
            )
            
            // Notes (opcionális)
            NotesCard(
                notes = uiState.notes,
                onNotesChange = { viewModel.setNotes(it) }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Save button
            Button(
                onClick = { viewModel.createProgress() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isCreating,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SuccessCyan,
                    contentColor = DarkBackground
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                if (uiState.isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = DarkBackground,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Save Progress",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            // Extra padding
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

/**
 * Logged Time Card - Dark Theme
 * Time spent in minutes (required, max = schedule duration)
 */
@Composable
private fun LoggedTimeCard(
    loggedTime: String,
    maxDuration: Int,
    onLoggedTimeChange: (String) -> Unit
) {
    val timeValue = loggedTime.toIntOrNull()
    val isError = loggedTime.isEmpty() || timeValue == null || timeValue <= 0 || timeValue > maxDuration
    
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
                text = "Time Spent *",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 18.sp
            )
            
            OutlinedTextField(
                value = loggedTime,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                        onLoggedTimeChange(newValue)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Minutes (required)", color = TextTertiary) },
                placeholder = { Text("e.g., 30", color = TextTertiary.copy(alpha = 0.5f)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = isError,
                supportingText = {
                    when {
                        loggedTime.isEmpty() ->
                            Text("Time spent is required", color = MaterialTheme.colorScheme.error)
                        timeValue != null && timeValue <= 0 -> 
                            Text("Enter a positive number", color = MaterialTheme.colorScheme.error)
                        timeValue != null && timeValue > maxDuration -> 
                            Text("Cannot exceed remaining time ($maxDuration min)", color = MaterialTheme.colorScheme.error)
                        else -> 
                            Text("Max $maxDuration min (remaining time)", color = TextTertiary, fontSize = 11.sp)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkBackground,
                    unfocusedContainerColor = DarkBackground,
                    focusedBorderColor = SuccessCyan,
                    unfocusedBorderColor = TextTertiary.copy(alpha = 0.3f),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = SuccessCyan,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    errorTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

/**
 * Notes Card - Dark Theme
 * Optional notes for the progress update
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
                    text = "Notes",
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
                placeholder = { 
                    Text(
                        "Add notes about your progress... (optional)",
                        color = TextTertiary.copy(alpha = 0.5f)
                    ) 
                },
                maxLines = 6,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkBackground,
                    unfocusedContainerColor = DarkBackground,
                    focusedBorderColor = SuccessCyan,
                    unfocusedBorderColor = TextTertiary.copy(alpha = 0.3f),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = SuccessCyan
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}


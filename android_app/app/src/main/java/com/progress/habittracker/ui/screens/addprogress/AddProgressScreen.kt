package com.progress.habittracker.ui.screens.addprogress

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.progress.habittracker.data.local.TokenManager
import com.progress.habittracker.data.repository.ProgressRepository
import com.progress.habittracker.ui.viewmodel.AddProgressViewModel
import com.progress.habittracker.ui.viewmodel.AddProgressViewModelFactory
import java.time.format.DateTimeFormatter

/**
 * Add Progress Screen
 * 
 * Progress (haladás) hozzáadása egy schedule-hoz
 * 
 * Funkciók:
 * - Dátum választás
 * - Eltöltött idő megadása (opcionális)
 * - Jegyzetek hozzáadása (opcionális)
 * - Completed checkbox
 * - Progress mentése
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
    val progressRepository = remember { ProgressRepository(tokenManager) }
    
    val viewModel: AddProgressViewModel = viewModel(
        factory = AddProgressViewModelFactory(scheduleId, progressRepository)
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
                title = { Text("Progress Hozzáadása") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Vissza")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dátum kártya
            DateCard(
                date = uiState.date,
                onDateChange = { viewModel.setDate(it) }
            )
            
            // Eltöltött idő
            LoggedTimeCard(
                loggedTime = uiState.loggedTime,
                onLoggedTimeChange = { viewModel.setLoggedTime(it) }
            )
            
            // Jegyzetek
            NotesCard(
                notes = uiState.notes,
                onNotesChange = { viewModel.setNotes(it) }
            )
            
            // Completed checkbox
            CompletedCard(
                isCompleted = uiState.isCompleted,
                onToggle = { viewModel.toggleCompleted() }
            )
            
            // Mentés gomb
            Button(
                onClick = { viewModel.createProgress() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isCreating
            ) {
                if (uiState.isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Progress Mentése")
                }
            }
        }
    }
}

/**
 * Dátum választó kártya
 */
@Composable
private fun DateCard(
    date: java.time.LocalDate,
    onDateChange: (java.time.LocalDate) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
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
            
            // Date Picker Button
            OutlinedButton(
                onClick = {
                    // Android DatePickerDialog
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
            
            Text(
                text = "Kattints a dátum megváltoztatásához",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Eltöltött idő kártya
 */
@Composable
private fun LoggedTimeCard(
    loggedTime: String,
    onLoggedTimeChange: (String) -> Unit
) {
    // Validáció: csak számok és nem negatív
    val isError = loggedTime.isNotEmpty() && 
                  (loggedTime.toIntOrNull() == null || loggedTime.toIntOrNull()!! < 0)
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Eltöltött idő (perc)",
                style = MaterialTheme.typography.titleMedium
            )
            
            OutlinedTextField(
                value = loggedTime,
                onValueChange = { newValue ->
                    // Csak számokat engedélyezünk
                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                        onLoggedTimeChange(newValue)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Perc (opcionális)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = isError,
                supportingText = if (isError) {
                    { Text("Érvényes számot adj meg (0 vagy nagyobb)") }
                } else null
            )
            
            Text(
                text = "Ha üres, csak a befejezettséget rögzítjük",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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

/**
 * Befejezett checkbox kártya
 */
@Composable
private fun CompletedCard(
    isCompleted: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Befejezett",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = if (isCompleted) {
                        "Ez a progress befejezettként lesz rögzítve"
                    } else {
                        "Ez a progress folyamatban lévőként lesz rögzítve"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Switch(
                checked = isCompleted,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

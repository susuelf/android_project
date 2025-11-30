package com.progress.habittracker.ui.screens.addprogress

// Android és Compose alapkönyvtárak
import androidx.compose.foundation.background // Háttérszín
import androidx.compose.foundation.layout.* // Layout elemek
import androidx.compose.foundation.rememberScrollState // Görgetés állapota
import androidx.compose.foundation.shape.RoundedCornerShape // Lekerekített sarkok
import androidx.compose.foundation.text.KeyboardOptions // Billentyűzet beállítások
import androidx.compose.foundation.verticalScroll // Függőleges görgetés
import androidx.compose.material.icons.Icons // Ikonok
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Vissza nyíl
import androidx.compose.material3.* // Material Design 3
import androidx.compose.runtime.* // Állapotkezelés
import androidx.compose.ui.Alignment // Igazítás
import androidx.compose.ui.Modifier // UI módosítók
import androidx.compose.ui.graphics.Color // Színek
import androidx.compose.ui.text.font.FontWeight // Betűvastagság
import androidx.compose.ui.text.input.KeyboardType // Billentyűzet típusa (pl. szám)
import androidx.compose.ui.unit.dp // Mértékegység
import androidx.compose.ui.unit.sp // Betűméret
import androidx.lifecycle.viewmodel.compose.viewModel // ViewModel integráció
import androidx.navigation.NavController // Navigáció
// Saját osztályok
import com.progress.habittracker.data.local.TokenManager // Token kezelés
import com.progress.habittracker.data.repository.ProgressRepository // Progress adatréteg
import com.progress.habittracker.data.repository.ScheduleRepository // Schedule adatréteg
import com.progress.habittracker.ui.theme.DarkBackground // Téma színek
import com.progress.habittracker.ui.theme.DarkSurface
import com.progress.habittracker.ui.theme.PrimaryPurple
import com.progress.habittracker.ui.theme.SuccessCyan
import com.progress.habittracker.ui.theme.TextPrimary
import com.progress.habittracker.ui.theme.TextSecondary
import com.progress.habittracker.ui.theme.TextTertiary
import com.progress.habittracker.ui.viewmodel.AddProgressViewModel // ViewModel
import com.progress.habittracker.ui.viewmodel.AddProgressViewModelFactory // ViewModel Factory
import java.time.format.DateTimeFormatter // Dátum formázás

/**
 * Add Progress Screen - Haladás rögzítése
 * 
 * Ez a képernyő lehetővé teszi a felhasználó számára, hogy rögzítse, mennyi időt töltött
 * egy adott tevékenységgel (Schedule).
 * 
 * Funkciók:
 * 1. Eltöltött idő (Logged Time) megadása percben (Kötelező).
 * 2. Megjegyzés (Notes) hozzáadása (Opcionális).
 * 3. Validáció: Nem lehet többet rögzíteni, mint a tervezett időtartam.
 * 4. Mentés: Elküldi az adatokat a szervernek (POST /progress).
 * 
 * @param navController Navigációs vezérlő.
 * @param scheduleId Annak a beosztásnak (Schedule) az azonosítója, amihez a haladást rögzítjük.
 */
@Suppress("NewApi") // Java Time API használata (desugaring támogatással)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProgressScreen(
    navController: NavController,
    scheduleId: Int
) {
    // Függőségek inicializálása
    val context = androidx.compose.ui.platform.LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val scheduleRepository = remember { ScheduleRepository(tokenManager) }
    val progressRepository = remember { ProgressRepository(tokenManager) }
    
    // ViewModel létrehozása a Factory segítségével, átadva a scheduleId-t
    val viewModel: AddProgressViewModel = viewModel(
        factory = AddProgressViewModelFactory(scheduleId, scheduleRepository, progressRepository)
    )
    
    // UI állapot figyelése
    val uiState by viewModel.uiState.collectAsState()
    
    // Snackbar állapot a visszajelzésekhez
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
    
    // Sikeres mentés esetén visszalépés
    LaunchedEffect(uiState.createSuccess) {
        if (uiState.createSuccess) {
            navController.popBackStack()
        }
    }
    
    // Scaffold: UI keret
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.add_progress_title), color = TextPrimary) },
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
        // Tartalom
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(DarkBackground)
                .verticalScroll(rememberScrollState()) // Görgethető tartalom
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Fejléc szövegek
            Text(
                text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.update_progress_header),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 22.sp
            )
            
            Text(
                text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.update_progress_description),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 1. Kártya: Eltöltött idő megadása
            // A maxDuration korlátozza a beírható értéket
            val maxDuration = uiState.maxAllowedTime
            LoggedTimeCard(
                loggedTime = uiState.loggedTime,
                maxDuration = maxDuration,
                onLoggedTimeChange = { viewModel.setLoggedTime(it) }
            )
            
            // 2. Kártya: Megjegyzések (Opcionális)
            NotesCard(
                notes = uiState.notes,
                onNotesChange = { viewModel.setNotes(it) }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Mentés gomb
            Button(
                onClick = { viewModel.createProgress() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isCreating, // Letiltva mentés közben
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
                        androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.save_progress_button),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            // Extra térköz az alján
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

/**
 * Logged Time Card - Eltöltött idő beviteli kártya
 * 
 * @param loggedTime Jelenleg beírt érték (szövegként).
 * @param maxDuration Maximálisan beírható idő (percben).
 * @param onLoggedTimeChange Callback változáskor.
 */
@Composable
private fun LoggedTimeCard(
    loggedTime: String,
    maxDuration: Int,
    onLoggedTimeChange: (String) -> Unit
) {
    // Validáció: Szám-e, üres-e, pozitív-e, nem haladja-e meg a maximumot
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
                text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.time_spent_label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 18.sp
            )
            
            OutlinedTextField(
                value = loggedTime,
                onValueChange = { newValue ->
                    // Csak számokat engedünk beírni
                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                        onLoggedTimeChange(newValue)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.minutes_required_label), color = TextTertiary) },
                placeholder = { Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.minutes_placeholder), color = TextTertiary.copy(alpha = 0.5f)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), // Számbillentyűzet
                singleLine = true,
                isError = isError,
                supportingText = {
                    // Hibaüzenetek vagy segédszöveg megjelenítése
                    when {
                        loggedTime.isEmpty() ->
                            Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.time_spent_required_error), color = MaterialTheme.colorScheme.error)
                        timeValue != null && timeValue <= 0 -> 
                            Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.positive_number_error), color = MaterialTheme.colorScheme.error)
                        timeValue != null && timeValue > maxDuration -> 
                            Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.max_time_exceeded_error, maxDuration), color = MaterialTheme.colorScheme.error)
                        else -> 
                            Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.max_time_hint, maxDuration), color = TextTertiary, fontSize = 11.sp)
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
 * Notes Card - Megjegyzés kártya
 * 
 * Opcionális szöveges mező a haladáshoz fűzött megjegyzéseknek.
 * 
 * @param notes A jelenlegi megjegyzés szövege.
 * @param onNotesChange Callback változáskor.
 */
@Composable
private fun NotesCard(
    notes: String,
    onNotesChange: (String) -> Unit
) {
    val maxLength = 500 // Maximális karakterszám
    
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
            // Fejléc és karakterszámláló
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.notes_title),
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
            
            // Szövegmező
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
                        androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.notes_optional_placeholder),
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


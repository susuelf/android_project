package com.progress.habittracker.ui.screens.addhabit

// Android és Compose alapkönyvtárak
import androidx.compose.foundation.background // Háttérszín beállítása
import androidx.compose.foundation.border // Keret rajzolása
import androidx.compose.foundation.clickable // Kattinthatóság
import androidx.compose.foundation.layout.* // Layout elemek (Column, Row, Spacer, stb.)
import androidx.compose.foundation.lazy.LazyColumn // Görgethető lista
import androidx.compose.foundation.lazy.grid.GridCells // Rács elrendezés cellái
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid // Görgethető rács
import androidx.compose.foundation.lazy.grid.items // Rács elemek kezelése
import androidx.compose.foundation.shape.RoundedCornerShape // Lekerekített sarkok
import androidx.compose.material.icons.Icons // Ikonok
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Vissza nyíl
import androidx.compose.material.icons.filled.Category // Kategória ikon
import androidx.compose.material3.* // Material Design 3 komponensek
import androidx.compose.runtime.* // Állapotkezelés
import androidx.compose.ui.Alignment // Igazítás
import androidx.compose.ui.Modifier // UI módosítók
import androidx.compose.ui.graphics.Color // Színkezelés
import androidx.compose.ui.text.font.FontWeight // Betűvastagság
import androidx.compose.ui.unit.dp // Mértékegység
import androidx.compose.ui.unit.sp // Betűméret
import androidx.lifecycle.viewmodel.compose.viewModel // ViewModel integráció
import androidx.navigation.NavController // Navigáció
// Saját osztályok
import com.progress.habittracker.data.local.TokenManager // Token kezelés
import com.progress.habittracker.data.model.HabitCategoryResponseDto // Kategória adatmodell
import com.progress.habittracker.data.repository.HabitRepository // Adatréteg
import com.progress.habittracker.ui.theme.DarkBackground // Téma színek
import com.progress.habittracker.ui.theme.DarkSurface
import com.progress.habittracker.ui.theme.PrimaryPurple
import com.progress.habittracker.ui.theme.TextPrimary
import com.progress.habittracker.ui.theme.TextSecondary
import com.progress.habittracker.ui.theme.TextTertiary
import com.progress.habittracker.ui.viewmodel.AddHabitViewModel // ViewModel
import com.progress.habittracker.ui.viewmodel.AddHabitViewModelFactory // ViewModel Factory

/**
 * AddHabitScreen - Új szokás (Habit) létrehozása
 *
 * Ez a képernyő űrlapot biztosít a felhasználónak egy új szokás definiálásához.
 *
 * Funkciók:
 * 1. Szokás nevének megadása (Kötelező).
 * 2. Leírás vagy motiváció megadása (Opcionális).
 * 3. Cél (Goal) meghatározása (pl. "10 pages", "30 mins") (Kötelező).
 * 4. Kategória kiválasztása egy listából (Kötelező).
 * 5. Mentés gomb: Validáció után elküldi az adatokat a szervernek.
 * 6. Mégse gomb: Visszalépés mentés nélkül.
 *
 * @param navController Navigációs vezérlő a visszalépéshez.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(
    navController: NavController
) {
    // Függőségek inicializálása
    val tokenManager = remember { TokenManager(navController.context) }
    val habitRepository = remember { HabitRepository(tokenManager) }
    val viewModelFactory = remember { AddHabitViewModelFactory(habitRepository) }
    
    // ViewModel létrehozása a Factory segítségével
    val viewModel: AddHabitViewModel = viewModel(factory = viewModelFactory)

    // UI állapot figyelése (StateFlow -> State)
    val uiState by viewModel.uiState.collectAsState()

    // Snackbar állapot a hibaüzenetek megjelenítéséhez
    val snackbarHostState = remember { SnackbarHostState() }

    // Sikeres létrehozás figyelése: Ha true, visszanavigálunk az előző képernyőre
    LaunchedEffect(uiState.createSuccess) {
        if (uiState.createSuccess) {
            navController.popBackStack()
        }
    }

    // Hibaüzenetek figyelése és megjelenítése Snackbar-on
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError() // Hiba törlése megjelenítés után
        }
    }

    // Scaffold: Az oldal alapszerkezete (TopBar, Snackbar, Content)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.add_new_habit_title), color = TextPrimary) },
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
        // Tartalom megjelenítése
        if (uiState.isLoadingCategories) {
            // Töltés állapot: Spinner megjelenítése
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryPurple)
            }
        } else {
            // Űrlap megjelenítése
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Görgethető tartalom (LazyColumn)
                LazyColumn(
                    modifier = Modifier
                        .weight(1f) // Kitölti a rendelkezésre álló helyet a gombok felett
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    contentPadding = PaddingValues(vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // 1. Mező: Habit név
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.habit_name_label),
                                color = TextPrimary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            OutlinedTextField(
                                value = uiState.name,
                                onValueChange = { viewModel.setName(it) },
                                placeholder = { 
                                    Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.habit_name_placeholder), color = TextTertiary) 
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = DarkSurface,
                                    unfocusedContainerColor = DarkSurface,
                                    focusedBorderColor = PrimaryPurple,
                                    unfocusedBorderColor = DarkSurface,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    cursorColor = PrimaryPurple
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // 2. Mező: Leírás / Motiváció
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.description_label),
                                color = TextPrimary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            Text(
                                text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.description_hint),
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            OutlinedTextField(
                                value = uiState.description,
                                onValueChange = { viewModel.setDescription(it) },
                                placeholder = { 
                                    Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.description_placeholder), color = TextTertiary) 
                                },
                                minLines = 3,
                                maxLines = 5,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = DarkSurface,
                                    unfocusedContainerColor = DarkSurface,
                                    focusedBorderColor = PrimaryPurple,
                                    unfocusedBorderColor = DarkSurface,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    cursorColor = PrimaryPurple
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // 3. Mező: Cél (Goal)
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.set_goal_label),
                                color = TextPrimary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            OutlinedTextField(
                                value = uiState.goal,
                                onValueChange = { viewModel.setGoal(it) },
                                placeholder = { 
                                    Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.goal_placeholder), color = TextTertiary) 
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = DarkSurface,
                                    unfocusedContainerColor = DarkSurface,
                                    focusedBorderColor = PrimaryPurple,
                                    unfocusedBorderColor = DarkSurface,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    cursorColor = PrimaryPurple
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // 4. Mező: Kategória választás
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.select_category_label),
                                color = TextPrimary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            Text(
                                text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.select_category_hint),
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                    }

                    // Kategória választó rács (Grid)
                    item {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3), // 3 oszlopos elrendezés
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.height(300.dp) // Fix magasság a görgetéshez
                        ) {
                            items(uiState.categories) { category ->
                                CategoryItem(
                                    category = category,
                                    isSelected = category == uiState.selectedCategory,
                                    onSelect = { viewModel.selectCategory(category) }
                                )
                            }
                        }
                    }

                    // Extra térköz az alján
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                
                // Alsó gombsor (Mégse és Létrehozás)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Mégse gomb (Cancel)
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = TextPrimary
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.dp,
                            brush = androidx.compose.ui.graphics.SolidColor(TextTertiary.copy(alpha = 0.3f))
                        ),
                        shape = RoundedCornerShape(27.dp)
                    ) {
                        Text(
                            androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.cancel),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Létrehozás gomb (Create)
                    Button(
                        onClick = { viewModel.createHabit() },
                        enabled = !uiState.isCreating, // Letiltva, ha éppen mentés van folyamatban
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryPurple,
                            contentColor = TextPrimary,
                            disabledContainerColor = PrimaryPurple.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(27.dp)
                    ) {
                        if (uiState.isCreating) {
                            // Töltés jelző a gombon belül
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = TextPrimary
                            )
                        } else {
                            Text(
                                text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.create),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * CategoryItem - Egy kategória megjelenítése a rácsban
 *
 * @param category A megjelenítendő kategória adatai.
 * @param isSelected Igaz, ha ez a kategória van kiválasztva.
 * @param onSelect Callback függvény, ami a kategóriára kattintáskor hívódik meg.
 */
@Composable
fun CategoryItem(
    category: HabitCategoryResponseDto,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f) // Négyzet alakú kártya
            .clickable(onClick = onSelect)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) PrimaryPurple else TextTertiary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                PrimaryPurple.copy(alpha = 0.1f) // Halvány lila háttér, ha kiválasztva
            else
                DarkSurface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Kategória ikon megjelenítése
            val iconUrl = category.iconUrl
            if (iconUrl != null && iconUrl.length == 1) {
                // Ha az ikon egy karakter (pl. emoji)
                Text(
                    text = iconUrl,
                    fontSize = 32.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            } else {
                // Ha nincs ikon, vagy nem karakter, alapértelmezett ikon
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = null,
                    tint = if (isSelected) PrimaryPurple else TextSecondary,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(bottom = 8.dp)
                )
            }

            // Kategória neve
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) PrimaryPurple else TextPrimary,
                maxLines = 2,
                fontSize = 12.sp
            )
        }
    }
}

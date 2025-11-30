package com.progress.habittracker.ui.screens.profile

// Android és Compose alapkönyvtárak importálása a UI építéséhez
import androidx.compose.foundation.layout.* // Layout elemek (Column, Row, Spacer, stb.)
import androidx.compose.foundation.lazy.LazyColumn // Görgethető lista
import androidx.compose.foundation.lazy.items // Lista elemek kezelése
import androidx.compose.foundation.shape.CircleShape // Kör alakzat (pl. profilképhez)
import androidx.compose.foundation.shape.RoundedCornerShape // Lekerekített sarkok
import androidx.compose.material.icons.Icons // Ikonok gyűjteménye
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Vissza nyíl ikon (tükrözhető)
import androidx.compose.material.icons.automirrored.filled.Logout // Kijelentkezés ikon
import androidx.compose.material.icons.filled.Add // Hozzáadás ikon
import androidx.compose.material.icons.filled.Edit // Szerkesztés ikon
import androidx.compose.material.icons.filled.Home // Kezdőlap ikon
import androidx.compose.material.icons.filled.Person // Profil ikon
import androidx.compose.material.icons.outlined.Home // Kezdőlap ikon (körvonalas)
import androidx.compose.material.icons.outlined.Person // Profil ikon (körvonalas)
import androidx.compose.material3.* // Material Design 3 komponensek
import androidx.compose.runtime.* // Compose állapotkezelés (remember, mutableStateOf, stb.)
import androidx.compose.ui.Alignment // Igazítások
import androidx.compose.ui.Modifier // UI módosítók (padding, size, stb.)
import androidx.compose.ui.draw.clip // Vágás (pl. kép kerekítése)
import androidx.compose.ui.layout.ContentScale // Kép méretezése
import androidx.compose.ui.platform.LocalContext // Android Context elérése
import androidx.compose.ui.text.font.FontWeight // Betűvastagság
import androidx.compose.ui.unit.dp // Mértékegység (density-independent pixel)
import androidx.lifecycle.viewmodel.compose.viewModel // ViewModel integráció Compose-ba
import androidx.navigation.NavController // Navigáció kezelése
import coil.compose.AsyncImage // Kép betöltése URL-ből (Coil könyvtár)
import coil.request.ImageRequest // Kép kérés konfigurálása
// Saját osztályok importálása
import com.progress.habittracker.data.local.TokenManager // Token kezelés (helyi adattárolás)
import com.progress.habittracker.data.model.HabitResponseDto // Adatmodell a szokásokhoz
import com.progress.habittracker.data.repository.ProfileRepository // Adatréteg a profilhoz
import com.progress.habittracker.navigation.Screen // Navigációs útvonalak definíciója
import com.progress.habittracker.ui.theme.DarkSurface // Téma színek
import com.progress.habittracker.ui.theme.SuccessCyan
import com.progress.habittracker.ui.theme.TextTertiary
import com.progress.habittracker.ui.viewmodel.ProfileViewModel // Nézetmodell a profil képernyőhöz
import com.progress.habittracker.ui.viewmodel.ProfileViewModelFactory // Factory a ViewModel létrehozásához

/**
 * Profile Screen
 *
 * Ez a képernyő felelős a felhasználói profil megjelenítéséért és kezeléséért.
 *
 * Főbb funkciók:
 * 1. Felhasználói adatok (név, email, profilkép) megjelenítése.
 * 2. A felhasználó szokásainak (habits) listázása.
 * 3. Lehetőség új szokás hozzáadására.
 * 4. Profil szerkesztésének kezdeményezése.
 * 5. Kijelentkezés a fiókból.
 * 6. Alsó navigációs sáv (Bottom Navigation) megjelenítése a főbb képernyők közötti váltáshoz.
 *
 * Kapcsolatok:
 * - ProfileViewModel: Az üzleti logika és az állapot kezelése.
 * - ProfileRepository: Adatok lekérése a szerverről.
 * - TokenManager: Hitelesítési token kezelése.
 * - NavController: Navigáció más képernyőkre (pl. EditProfile, AddHabit, Login).
 *
 * @param navController A navigációt vezérlő objektum, amely lehetővé teszi a képernyők közötti váltást.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController
) {
    // Context lekérése a függőségek (pl. TokenManager) inicializálásához
    val context = LocalContext.current
    
    // Függőségek manuális injektálása (DI keretrendszer hiányában)
    // TokenManager: A bejelentkezési token tárolása és olvasása
    val tokenManager = remember { TokenManager(context) }
    // Repository-k: Az adatréteg elérése
    val profileRepository = remember { ProfileRepository(tokenManager) }
    val scheduleRepository = remember { com.progress.habittracker.data.repository.ScheduleRepository(tokenManager) }

    // ViewModel inicializálása a Factory segítségével
    // A ViewModel felelős a UI állapotának tárolásáért és az üzleti logika végrehajtásáért
    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(profileRepository, scheduleRepository, tokenManager)
    )

    // A UI állapot figyelése (StateFlow -> State)
    // Ha a viewModel.uiState változik, a UI automatikusan újrarajzolódik
    val uiState by viewModel.uiState.collectAsState()

    // Helyi állapot a kijelentkezési megerősítő ablak megjelenítéséhez
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Kijelentkezés megerősítő ablak (Dialog)
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.logout_confirmation_title)) },
            text = { Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.logout_confirmation_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Kijelentkezés végrehajtása a ViewModel-en keresztül
                        viewModel.logout()
                        showLogoutDialog = false
                        // Navigálás a bejelentkezési képernyőre, és a back stack törlése
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                ) {
                    Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.logout))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.cancel))
                }
            }
        )
    }

    // Életciklus esemény: Amikor a képernyő fókuszba kerül (pl. visszatéréskor), frissítjük az adatokat
    val navBackStackEntry = navController.currentBackStackEntry
    LaunchedEffect(navBackStackEntry) {
        // Ha visszatérünk, frissítjük a profilt és a habit-eket
        viewModel.loadProfile()
    }

    // Scaffold: Az alapvető UI struktúra (TopBar, BottomBar, Content)
    Scaffold(
        // Felső sáv (TopAppBar)
        topBar = {
            TopAppBar(
                title = { Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.profile_title)) },
                actions = {
                    // Kijelentkezés gomb a jobb felső sarokban
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.logout)
                        )
                    }
                }
            )
        },
        // Alsó navigációs sáv (Bottom Navigation)
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                // Home gomb
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Outlined.Home,
                            contentDescription = "Home"
                        )
                    },
                    label = { Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.home_title)) },
                    selected = false, // Nem ez az aktív képernyő
                    onClick = {
                        // Navigálás a Home képernyőre
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SuccessCyan,
                        selectedTextColor = SuccessCyan,
                        indicatorColor = DarkSurface,
                        unselectedIconColor = TextTertiary,
                        unselectedTextColor = TextTertiary
                    )
                )

                // Profile gomb (Aktív)
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "Profile"
                        )
                    },
                    label = { Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.profile_title)) },
                    selected = true, // Ez az aktív képernyő
                    onClick = {
                        // Már a Profile screen-en vagyunk, nem történik semmi
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SuccessCyan,
                        selectedTextColor = SuccessCyan,
                        indicatorColor = DarkSurface,
                        unselectedIconColor = TextTertiary,
                        unselectedTextColor = TextTertiary
                    )
                )
            }
        }
    ) { paddingValues ->
        // A tartalom konténere, figyelembe véve a Scaffold padding-jét
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // UI állapotok kezelése (Betöltés, Hiba, Tartalom)
            when {
                uiState.isLoading -> {
                    // Betöltés jelző (Spinner)
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null -> {
                    // Hibaüzenet és Újrapróbálkozás gomb
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.error ?: androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.unknown_error),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadProfile() }) {
                            Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.retry))
                        }
                    }
                }
                uiState.profile != null -> {
                    // Sikeres betöltés esetén a profil tartalom megjelenítése
                    ProfileContent(
                        profile = uiState.profile!!,
                        habits = uiState.habits,
                        habitStats = uiState.habitStats,
                        isLoadingHabits = uiState.isLoadingHabits,
                        onEditProfile = { navController.navigate(Screen.EditProfile.route) },
                        onAddHabit = { navController.navigate(Screen.AddHabit.route) }
                    )
                }
            }
        }
    }
}

/**
 * Profil tartalom megjelenítése
 *
 * Ez a komponens felelős a profil adatok és a szokások listájának elrendezéséért.
 * LazyColumn-t használ a görgethető tartalomhoz.
 *
 * @param profile A felhasználó profil adatai
 * @param habits A felhasználó szokásainak listája
 * @param habitStats Statisztikák a szokásokhoz (pl. teljesítési arány)
 * @param isLoadingHabits Töltődik-e éppen a szokások listája
 * @param onEditProfile Callback a profil szerkesztéséhez
 * @param onAddHabit Callback új szokás hozzáadásához
 */
@Composable
private fun ProfileContent(
    profile: com.progress.habittracker.data.model.ProfileResponseDto,
    habits: List<HabitResponseDto>,
    habitStats: Map<Int, Float>,
    isLoadingHabits: Boolean,
    onEditProfile: () -> Unit,
    onAddHabit: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Szekció: Profil fejléc (Kép, Név, Email, Leírás, Szerkesztés gomb)
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profil kép megjelenítése
                    Surface(
                        modifier = Modifier.size(140.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        if (!profile.profileImageUrl.isNullOrBlank()) {
                            // Kép betöltése URL-ből aszinkron módon
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(profile.profileImageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.profile_picture_content_desc),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Alapértelmezett ikon, ha nincs profilkép
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Felhasználónév
                    Text(
                        text = profile.username,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Email cím
                    Text(
                        text = profile.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Bemutatkozás (ha van)
                    if (!profile.description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = profile.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Profil szerkesztése gomb
                    Button(
                        onClick = onEditProfile,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.edit_profile))
                    }
                }
            }
        }

        // 2. Szekció: Szokások (Habits) listája
        item {
            Text(
                text = "${androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.my_habits)} (${habits.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (isLoadingHabits) {
            // Betöltés jelző a szokásokhoz
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (habits.isEmpty()) {
            // Üres állapot, ha nincsenek szokások
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.no_habits_yet),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Szokások listázása
            items(habits) { habit ->
                HabitItem(
                    habit = habit,
                    consistency = habitStats[habit.id] ?: 0f
                )
            }
        }

        // 3. Szekció: Új szokás hozzáadása gomb
        item {
            Button(
                onClick = onAddHabit,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.add_new_habit_title))
            }
        }
    }
}

/**
 * Egy szokás (Habit) kártya megjelenítése a listában
 */
@Composable
private fun HabitItem(
    habit: HabitResponseDto,
    consistency: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Cím és Kategória
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // Kategória címke (Chip)
                AssistChip(
                    onClick = { },
                    label = { Text(habit.category.name) },
                    modifier = Modifier.height(24.dp)
                )
            }

            // Leírás
            if (!habit.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = habit.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Cél (Goal)
            if (!habit.goal.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.habit_goal_prefix)}${habit.goal}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            // Napi haladás (Progress) - Itt jeleníthető meg a statisztika
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Daily Progress",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(consistency * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { consistency },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }
    }
}

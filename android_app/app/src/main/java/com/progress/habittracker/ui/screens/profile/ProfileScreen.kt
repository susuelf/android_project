package com.progress.habittracker.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.progress.habittracker.data.local.TokenManager
import com.progress.habittracker.data.model.HabitResponseDto
import com.progress.habittracker.data.repository.ProfileRepository
import com.progress.habittracker.navigation.Screen
import com.progress.habittracker.ui.theme.DarkSurface
import com.progress.habittracker.ui.theme.SuccessCyan
import com.progress.habittracker.ui.theme.TextTertiary
import com.progress.habittracker.ui.viewmodel.ProfileViewModel
import com.progress.habittracker.ui.viewmodel.ProfileViewModelFactory

/**
 * Profile Screen
 *
 * Felhasználói profil megjelenítése
 *
 * Funkciók:
 * - Profil adatok megjelenítése
 * - Habit-ek listázása
 * - Profil szerkesztése
 * - Kijelentkezés
 *
 * @param navController Navigációs kontroller
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val profileRepository = remember { ProfileRepository(tokenManager) }
    val scheduleRepository = remember { com.progress.habittracker.data.repository.ScheduleRepository(tokenManager) }

    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(profileRepository, scheduleRepository, tokenManager)
    )

    val uiState by viewModel.uiState.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }

    // Kijelentkezés dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.logout_confirmation_title)) },
            text = { Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.logout_confirmation_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.logout()
                        showLogoutDialog = false
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

    // Frissítés amikor visszajövünk Add Habit-ből vagy más képernyőről
    val navBackStackEntry = navController.currentBackStackEntry
    LaunchedEffect(navBackStackEntry) {
        // Ha visszatérünk, frissítjük a profilt és a habit-eket
        viewModel.loadProfile()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.profile_title)) },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.logout)
                        )
                    }
                }
            )
        },
        bottomBar = {
            // Bottom Navigation Bar
            NavigationBar(
                containerColor = DarkSurface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                // Home
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Outlined.Home,
                            contentDescription = "Home"
                        )
                    },
                    label = { Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.home_title)) },
                    selected = false,
                    onClick = {
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

                // Profile
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "Profile"
                        )
                    },
                    label = { Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.profile_title)) },
                    selected = true,
                    onClick = {
                        // Már a Profile screen-en vagyunk
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null -> {
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
        // Profil kép és alapadatok
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
                    // Profil kép
                    Surface(
                        modifier = Modifier.size(140.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        if (!profile.profileImageUrl.isNullOrBlank()) {
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

                    // Username
                    Text(
                        text = profile.username,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Email
                    Text(
                        text = profile.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Description
                    if (!profile.description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = profile.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Szerkesztés gomb
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

        // Habit-ek szekció
        item {
            Text(
                text = "${androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.my_habits)} (${habits.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (isLoadingHabits) {
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
            items(habits) { habit ->
                HabitItem(
                    habit = habit,
                    consistency = habitStats[habit.id] ?: 0f
                )
            }
        }

        // Create New Habit Button
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
 * Habit item megjelenítése
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
                
                // Kategória chip
                AssistChip(
                    onClick = { },
                    label = { Text(habit.category.name) },
                    modifier = Modifier.height(24.dp)
                )
            }

            if (!habit.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = habit.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!habit.goal.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.habit_goal_prefix)}${habit.goal}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            // Consistency Progress Bar
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

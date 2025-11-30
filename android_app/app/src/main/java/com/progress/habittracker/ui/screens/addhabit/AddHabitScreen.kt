package com.progress.habittracker.ui.screens.addhabit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.progress.habittracker.data.local.TokenManager
import com.progress.habittracker.data.model.HabitCategoryResponseDto
import com.progress.habittracker.data.repository.HabitRepository
import com.progress.habittracker.ui.theme.DarkBackground
import com.progress.habittracker.ui.theme.DarkSurface
import com.progress.habittracker.ui.theme.PrimaryPurple
import com.progress.habittracker.ui.theme.TextPrimary
import com.progress.habittracker.ui.theme.TextSecondary
import com.progress.habittracker.ui.theme.TextTertiary
import com.progress.habittracker.ui.viewmodel.AddHabitViewModel
import com.progress.habittracker.ui.viewmodel.AddHabitViewModelFactory

/**
 * AddHabitScreen - Új habit létrehozása (Design frissítve - Dark Theme)
 *
 * Funkciók:
 * - Habit név megadása (kötelező)
 * - Leírás/motiváció (opcionális) - short explanation
 * - Goal megadása (kötelező)
 * - Kategória választás ikonnal (kötelező)
 * - Dark theme styling
 * - Cancel és Create gombok
 * - Habit mentése -> POST /habit
 *
 * @param navController Navigációs controller
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(
    navController: NavController
) {
    // ViewModel inicializálás Factory-val
    val tokenManager = remember { TokenManager(navController.context) }
    val habitRepository = remember { HabitRepository(tokenManager) }
    val viewModelFactory = remember { AddHabitViewModelFactory(habitRepository) }
    val viewModel: AddHabitViewModel = viewModel(factory = viewModelFactory)

    // UI state
    val uiState by viewModel.uiState.collectAsState()

    // Snackbar state
    val snackbarHostState = remember { SnackbarHostState() }

    // Automatikus navigáció vissza sikeres létrehozás után
    LaunchedEffect(uiState.createSuccess) {
        if (uiState.createSuccess) {
            navController.popBackStack()
        }
    }

    // Error handling Snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

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
        if (uiState.isLoadingCategories) {
            // Loading state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryPurple)
            }
        } else {
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Scrollable content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    contentPadding = PaddingValues(vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Habit név
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

                    // Leírás/Motiváció
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

                    // Goal
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

                    // Kategória választás
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

                    // Kategória grid
                    item {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.height(300.dp)
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

                    // Extra spacing
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                
                // Bottom buttons (Cancel és Create)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel gomb
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
                    
                    // Create gomb
                    Button(
                        onClick = { viewModel.createHabit() },
                        enabled = !uiState.isCreating,
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
 * CategoryItem - Kategória választó elem (Dark Theme)
 *
 * @param category Kategória adatai
 * @param isSelected Ki van-e választva
 * @param onSelect Kiválasztás callback
 */
@Composable
fun CategoryItem(
    category: HabitCategoryResponseDto,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onSelect)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) PrimaryPurple else TextTertiary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                PrimaryPurple.copy(alpha = 0.1f)
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
            // Kategória ikon (emoji vagy első betű)
            val iconUrl = category.iconUrl
            if (iconUrl != null && iconUrl.length == 1) {
                // Ha emoji/single character
                Text(
                    text = iconUrl,
                    fontSize = 32.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            } else {
                // Első betű vagy default ikon
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = null,
                    tint = if (isSelected) PrimaryPurple else TextSecondary,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(bottom = 8.dp)
                )
            }

            // Kategória név
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

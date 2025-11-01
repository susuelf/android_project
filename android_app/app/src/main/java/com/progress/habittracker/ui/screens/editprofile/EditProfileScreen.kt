package com.progress.habittracker.ui.screens.editprofile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
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
import com.progress.habittracker.data.repository.ProfileRepository
import com.progress.habittracker.ui.viewmodel.EditProfileViewModel
import com.progress.habittracker.ui.viewmodel.EditProfileViewModelFactory
import java.io.File
import java.io.FileOutputStream

/**
 * Edit Profile Screen
 * 
 * Profil adatok szerkesztése
 * 
 * Funkciók:
 * - Username módosítása
 * - Description módosítása
 * - Profilkép feltöltése/csere
 * - Mentés
 * 
 * @param navController Navigációs kontroller
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val profileRepository = remember { ProfileRepository(tokenManager) }

    val viewModel: EditProfileViewModel = viewModel(
        factory = EditProfileViewModelFactory(profileRepository)
    )

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Automatikus visszanavigálás sikeres mentés után
    LaunchedEffect(uiState.updateSuccess) {
        if (uiState.updateSuccess) {
            navController.popBackStack()
        }
    }

    // Hibaüzenet megjelenítése Snackbar-ban
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // URI-ból File objektum készítése
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = File(context.cacheDir, "profile_image_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(file)
                
                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                
                viewModel.selectImage(file)
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil Szerkesztése") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Vissza"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                LoadingState(modifier = Modifier.padding(paddingValues))
            }

            uiState.profile != null -> {
                EditProfileContent(
                    uiState = uiState,
                    onUsernameChange = viewModel::setUsername,
                    onDescriptionChange = viewModel::setDescription,
                    onImagePickerClick = {
                        imagePickerLauncher.launch("image/*")
                    },
                    onSaveClick = viewModel::saveProfile,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            else -> {
                ErrorState(
                    message = "Nem sikerült betölteni a profilt",
                    onRetry = { viewModel.loadProfile() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun EditProfileContent(
    uiState: EditProfileViewModel.EditProfileUiState,
    onUsernameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onImagePickerClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profilkép szerkesztés
        ProfileImageSection(
            currentImageUrl = uiState.profile?.profileImageUrl,
            selectedImageFile = uiState.selectedImageFile,
            onImagePickerClick = onImagePickerClick,
            isUploading = uiState.isUploadingImage
        )

        // Email (read-only)
        EmailCard(email = uiState.profile?.email ?: "")

        // Username szerkesztés
        UsernameCard(
            username = uiState.username,
            onUsernameChange = onUsernameChange
        )

        // Description szerkesztés
        DescriptionCard(
            description = uiState.description,
            onDescriptionChange = onDescriptionChange
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Mentés gomb
        Button(
            onClick = onSaveClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !uiState.isUpdating && !uiState.isUploadingImage
        ) {
            if (uiState.isUpdating || uiState.isUploadingImage) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Változtatások Mentése")
            }
        }
    }
}

@Composable
private fun ProfileImageSection(
    currentImageUrl: String?,
    selectedImageFile: File?,
    onImagePickerClick: () -> Unit,
    isUploading: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            // Profilkép megjelenítése
            Surface(
                modifier = Modifier.size(140.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
            ) {
                if (selectedImageFile != null) {
                    // Lokálisan kiválasztott kép
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(selectedImageFile)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Kiválasztott profilkép",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else if (!currentImageUrl.isNullOrBlank()) {
                    // Meglévő profilkép
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(currentImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profilkép",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Placeholder (felhasználó neve kezdőbetűje)
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Nincs kép",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Loading overlay
            if (isUploading) {
                Surface(
                    modifier = Modifier.size(140.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }

        // Kép választás gomb
        OutlinedButton(
            onClick = onImagePickerClick,
            enabled = !isUploading
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Kép Választása")
        }
    }
}

@Composable
private fun EmailCard(email: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Email",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = email,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Az email nem módosítható",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun UsernameCard(
    username: String,
    onUsernameChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Felhasználónév",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = username,
                onValueChange = onUsernameChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Add meg a felhasználóneved") },
                singleLine = true
            )
        }
    }
}

@Composable
private fun DescriptionCard(
    description: String,
    onDescriptionChange: (String) -> Unit
) {
    val maxLength = 500
    val isOverLimit = description.length > maxLength

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Bemutatkozás",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = { Text("Mesélj magadról...") },
                maxLines = 5,
                isError = isOverLimit,
                supportingText = {
                    Text(
                        text = "${description.length} / $maxLength",
                        color = if (isOverLimit) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            )
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

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

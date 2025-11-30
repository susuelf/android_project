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
 * Edit Profile Screen - Profil szerkesztése
 *
 * Ez a képernyő teszi lehetővé a felhasználó számára a profiladatainak módosítását.
 *
 * Főbb funkciók:
 * - Felhasználónév (Username) módosítása.
 * - Bemutatkozás (Description/Bio) módosítása.
 * - Profilkép feltöltése vagy cseréje a galériából.
 * - Email cím megjelenítése (csak olvasható).
 * - Változtatások mentése a szerverre.
 *
 * @param navController A navigációért felelős vezérlő.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController
) {
    // Kontextus és függőségek inicializálása
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val profileRepository = remember { ProfileRepository(tokenManager) }

    // ViewModel létrehozása a Factory segítségével
    val viewModel: EditProfileViewModel = viewModel(
        factory = EditProfileViewModelFactory(profileRepository)
    )

    // UI állapot figyelése a ViewModel-ből
    val uiState by viewModel.uiState.collectAsState()
    
    // Snackbar állapot a hibaüzenetek megjelenítéséhez
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

    // Képválasztó (Image Picker) indítója
    // Ez kezeli a galériából való képkiválasztás eredményét
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Ha a felhasználó kiválasztott egy képet (URI nem null)
            try {
                // URI-ból File objektum készítése:
                // 1. Megnyitjuk a bemeneti stream-et az URI-hoz.
                val inputStream = context.contentResolver.openInputStream(uri)
                // 2. Létrehozunk egy ideiglenes fájlt a cache könyvtárban.
                val file = File(context.cacheDir, "profile_image_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(file)
                
                // 3. Átmásoljuk a tartalmat.
                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                
                // 4. Átadjuk a fájlt a ViewModel-nek.
                viewModel.selectImage(file)
            } catch (e: Exception) {
                // Hiba esetén itt lehetne kezelni (pl. logolás)
            }
        }
    }

    // Scaffold: Az alapvető képernyőszerkezet
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.edit_profile_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.back)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        // Tartalom megjelenítése az állapottól függően
        when {
            uiState.isLoading -> {
                // Töltés közben
                LoadingState(modifier = Modifier.padding(paddingValues))
            }

            uiState.profile != null -> {
                // Ha a profil adatok be vannak töltve, megjelenítjük a szerkesztő felületet
                EditProfileContent(
                    uiState = uiState,
                    onUsernameChange = viewModel::setUsername,
                    onDescriptionChange = viewModel::setDescription,
                    onImagePickerClick = {
                        // Képválasztó indítása (csak képeket engedélyezünk)
                        imagePickerLauncher.launch("image/*")
                    },
                    onSaveClick = viewModel::saveProfile,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            else -> {
                // Hibaállapot, ha nem sikerült betölteni a profilt
                ErrorState(
                    message = "Nem sikerült betölteni a profilt",
                    onRetry = { viewModel.loadProfile() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

/**
 * A szerkesztő felület tartalma.
 */
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
            .verticalScroll(rememberScrollState()) // Görgethető tartalom
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Profilkép szerkesztése szekció
        ProfileImageSection(
            currentImageUrl = uiState.profile?.profileImageUrl,
            selectedImageFile = uiState.selectedImageFile,
            onImagePickerClick = onImagePickerClick,
            isUploading = uiState.isUploadingImage
        )

        // 2. Email cím (csak olvasható)
        EmailCard(email = uiState.profile?.email ?: "")

        // 3. Felhasználónév szerkesztése
        UsernameCard(
            username = uiState.username,
            onUsernameChange = onUsernameChange
        )

        // 4. Bemutatkozás (Description) szerkesztése
        DescriptionCard(
            description = uiState.description,
            onDescriptionChange = onDescriptionChange
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 5. Mentés gomb
        Button(
            onClick = onSaveClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            // Gomb letiltása, ha éppen mentés vagy képfeltöltés zajlik
            enabled = !uiState.isUpdating && !uiState.isUploadingImage
        ) {
            if (uiState.isUpdating || uiState.isUploadingImage) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.save_changes))
            }
        }
    }
}

/**
 * Profilkép megjelenítése és szerkesztése.
 * Kezeli a jelenlegi képet, a kiválasztott új képet és a feltöltési állapotot.
 */
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
            // Profilkép megjelenítése kör alakban
            Surface(
                modifier = Modifier.size(140.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
            ) {
                if (selectedImageFile != null) {
                    // Ha van újonnan kiválasztott kép (lokális fájl), azt jelenítjük meg
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(selectedImageFile)
                            .crossfade(true)
                            .build(),
                        contentDescription = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.selected_profile_image),
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else if (!currentImageUrl.isNullOrBlank()) {
                    // Ha nincs új kép, de van meglévő URL, azt töltjük be
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(currentImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.profile_image),
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Ha nincs kép, egy placeholder ikont jelenítünk meg
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.no_image),
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Töltésjelző réteg (overlay), ha éppen feltöltés zajlik
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

        // Képválasztás gomb
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
            Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.select_image))
        }
    }
}

/**
 * Email cím megjelenítése kártyán.
 * Ez a mező nem szerkeszthető.
 */
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
                text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.email_label),
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
                text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.email_not_editable),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Felhasználónév szerkesztése kártyán.
 */
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
                text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.username_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = username,
                onValueChange = onUsernameChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.username_placeholder)) },
                singleLine = true
            )
        }
    }
}

/**
 * Bemutatkozás (Description) szerkesztése kártyán.
 * Tartalmaz karakterszámlálót is.
 */
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
                text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.bio_label),
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
                placeholder = { Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.bio_placeholder)) },
                maxLines = 5,
                isError = isOverLimit,
                supportingText = {
                    // Karakterszámláló megjelenítése
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

/**
 * Töltési állapot megjelenítése.
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
 * Hibaállapot megjelenítése újrapóbálkozási lehetőséggel.
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
                Text(androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.retry))
            }
        }
    }
}

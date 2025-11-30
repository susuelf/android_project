package com.progress.habittracker.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.progress.habittracker.navigation.Screen
import com.progress.habittracker.ui.theme.DarkBackground
import com.progress.habittracker.ui.theme.DarkSurface
import com.progress.habittracker.ui.theme.PrimaryPurple
import com.progress.habittracker.ui.theme.Progr3SSTheme
import com.progress.habittracker.ui.theme.SuccessCyan
import com.progress.habittracker.ui.theme.TextPrimary
import com.progress.habittracker.ui.theme.TextSecondary
import com.progress.habittracker.ui.theme.TextTertiary
import com.progress.habittracker.ui.viewmodel.AuthViewModel
import com.progress.habittracker.ui.viewmodel.AuthViewModelFactory

/**
 * RegisterScreen - Regisztrációs képernyő
 *
 * Ez a képernyő felelős az új felhasználók regisztrációjáért.
 * Tartalmazza a felhasználónév, email, jelszó és jelszó megerősítés beviteli mezőket.
 *
 * Főbb funkciók:
 * - Felhasználói adatok bekérése (felhasználónév, email, jelszó, jelszó megerősítés).
 * - Validáció (email formátum, jelszó hossza, jelszavak egyezése).
 * - Kommunikáció az AuthViewModel-lel a regisztrációs folyamat kezelésére.
 * - Navigáció a főképernyőre sikeres regisztráció esetén.
 * - Hibaüzenetek megjelenítése (pl. foglalt email, nem egyező jelszavak).
 *
 * @param navController A navigációért felelős vezérlő.
 * @param viewModel Az autentikációs logikát kezelő ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(LocalContext.current)
    )
) {
    // Állapotváltozók a beviteli mezők értékeinek tárolására
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    // Jelszó mezők láthatóságának állapota
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    // Tab váltó állapota (Login/Register). Itt alapértelmezetten a Register aktív.
    var isLoginTab by remember { mutableStateOf(false) }
    
    // Fókusz kezelő a mezők közötti navigációhoz
    val focusManager = LocalFocusManager.current
    
    // Az autentikációs folyamat állapotának figyelése
    val authState by viewModel.authState.collectAsState()
    
    // Jelszó eltérés ellenőrzése: ha a megerősítő mező nem üres és nem egyezik a jelszóval
    val passwordMismatch = confirmPassword.isNotEmpty() && password != confirmPassword
    
    // Hatás (Effect), amely akkor fut le, ha az authState változik.
    // Sikeres regisztráció esetén navigálunk a Home képernyőre.
    LaunchedEffect(authState) {
        if (authState is AuthViewModel.AuthState.Success) {
            // Sikeres regisztráció -> Home Screen
            navController.navigate(Screen.Home.route) {
                // Töröljük az összes auth képernyőt a back stack-ből
                popUpTo(Screen.Register.route) { inclusive = true }
            }
            viewModel.resetState()
        }
    }
    
    // UI - Teljes képernyős doboz sötét háttérrel
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp)
    ) {
        // Oszlop elrendezés, amely görgethető, ha a tartalom nem fér el a képernyőn
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()), // Görgethetőség engedélyezése
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            // Alkalmazás logója és neve: "Progr3SS"
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = TextPrimary)) {
                        append("Progr")
                    }
                    withStyle(style = SpanStyle(color = SuccessCyan)) {
                        append("3")
                    }
                    withStyle(style = SpanStyle(color = TextPrimary)) {
                        append("SS")
                    }
                },
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Alcím
            Text(
                text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.app_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Login/Register Tab Switcher (Váltógomb)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(DarkSurface, RoundedCornerShape(24.dp)),
                horizontalArrangement = Arrangement.Center
            ) {
                // Login Tab Gomb
                Button(
                    onClick = { 
                        // Navigálás vissza a Login képernyőre
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent, // Inaktív állapot
                        contentColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.login),
                        fontWeight = FontWeight.Normal
                    )
                }
                
                // Register Tab Gomb (Aktív)
                Button(
                    onClick = { 
                        isLoginTab = false
                        // Itt már a Register képernyőn vagyunk
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryPurple, // Aktív állapot jelzése színnel
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.register),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Felhasználónév beviteli mező
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.username),
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    placeholder = { 
                        Text(
                            androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.username_placeholder), 
                            color = TextTertiary
                        ) 
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
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
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Email beviteli mező
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.email),
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { 
                        Text(
                            androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.email_placeholder), 
                            color = TextTertiary
                        ) 
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
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
                    shape = RoundedCornerShape(12.dp),
                    isError = email.isNotEmpty() && !viewModel.isValidEmail(email)
                )
                
                if (email.isNotEmpty() && !viewModel.isValidEmail(email)) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.email_invalid),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Jelszó beviteli mező
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.password),
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { 
                        Text(
                            androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.password_placeholder), 
                            color = TextTertiary
                        ) 
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) 
                                    Icons.Default.Visibility 
                                else 
                                    Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) 
                                    androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.password_hide)
                                else 
                                    androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.password_show),
                                tint = TextTertiary
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) 
                        VisualTransformation.None 
                    else 
                        PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
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
                    shape = RoundedCornerShape(12.dp),
                    isError = password.isNotEmpty() && !viewModel.isValidPassword(password)
                )
                
                if (password.isNotEmpty() && !viewModel.isValidPassword(password)) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.password_min_length),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Jelszó megerősítés mező
            // Ha a jelszavak nem egyeznek, piros keretet kap
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.confirm_password),
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = { 
                        Text(
                            androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.confirm_password_placeholder), 
                            color = TextTertiary
                        ) 
                    },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) 
                                    Icons.Default.Visibility 
                                else 
                                    Icons.Default.VisibilityOff,
                                contentDescription = if (confirmPasswordVisible) 
                                    androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.password_hide)
                                else 
                                    androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.password_show),
                                tint = TextTertiary
                            )
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) 
                        VisualTransformation.None 
                    else 
                        PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            // Ha minden valid, indítsuk a regisztrációt
                            if (username.isNotEmpty() && 
                                viewModel.isValidEmail(email) && 
                                viewModel.isValidPassword(password) && 
                                !passwordMismatch) {
                                viewModel.signUp(username, email, password)
                            }
                        }
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface,
                        focusedBorderColor = if (passwordMismatch) MaterialTheme.colorScheme.error else PrimaryPurple,
                        unfocusedBorderColor = if (passwordMismatch) MaterialTheme.colorScheme.error else DarkSurface,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = PrimaryPurple
                    ),
                    shape = RoundedCornerShape(12.dp),
                    isError = passwordMismatch // PIROS SZEGÉLY HA NEM EGYEZIK
                )
                
                // Hibaüzenet ha a jelszavak nem egyeznek
                if (passwordMismatch) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.passwords_do_not_match),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Regisztráció gomb
            Button(
                onClick = {
                    if (username.isNotEmpty() && 
                        viewModel.isValidEmail(email) && 
                        viewModel.isValidPassword(password) && 
                        !passwordMismatch) {
                        viewModel.signUp(username, email, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                // Gomb letiltása, ha töltünk, vagy ha az adatok érvénytelenek
                enabled = authState !is AuthViewModel.AuthState.Loading &&
                        username.isNotEmpty() &&
                        viewModel.isValidEmail(email) &&
                        viewModel.isValidPassword(password) &&
                        !passwordMismatch,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryPurple,
                    contentColor = TextPrimary,
                    disabledContainerColor = PrimaryPurple.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(27.dp)
            ) {
                if (authState is AuthViewModel.AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = TextPrimary
                    )
                } else {
                    Text(
                        androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.register),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Hibaüzenet megjelenítése
            if (authState is AuthViewModel.AuthState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (authState as AuthViewModel.AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // "Vagy regisztrálj ezzel" elválasztó
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Divider(
                    modifier = Modifier.weight(1f),
                    color = TextTertiary.copy(alpha = 0.3f)
                )
                Text(
                    text = "  " + androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.or_register_with) + "  ",
                    color = TextTertiary,
                    style = MaterialTheme.typography.bodySmall
                )
                Divider(
                    modifier = Modifier.weight(1f),
                    color = TextTertiary.copy(alpha = 0.3f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Google regisztráció gomb (csak UI, nincs implementálva)
            OutlinedButton(
                onClick = { 
                    // TODO: Google Sign-Up implementálása
                },
                modifier = Modifier
                    .fillMaxWidth()
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
                // Google "G" icon
                Text(
                    text = "G",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextPrimary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.google),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Előnézet a RegisterScreen-hez.
 */
@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    Progr3SSTheme {
        RegisterScreen(navController = rememberNavController())
    }
}

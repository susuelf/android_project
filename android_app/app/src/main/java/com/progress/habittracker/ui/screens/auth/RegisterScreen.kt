package com.progress.habittracker.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.progress.habittracker.navigation.Screen
import com.progress.habittracker.ui.theme.Progr3SSTheme
import com.progress.habittracker.ui.viewmodel.AuthViewModel
import com.progress.habittracker.ui.viewmodel.AuthViewModelFactory

/**
 * RegisterScreen - Regisztrációs képernyő
 * 
 * Funkciók:
 * - Felhasználónév, email, jelszó, jelszó megerősítés beviteli mezők
 * - Jelszó eltérés kezelése: piros szegély + hibaüzenet
 * - Regisztráció gomb -> AuthViewModel.signUp()
 * - "Már van fiókod?" link -> Login Screen
 * - Validáció és hibaüzenetek
 * - Loading state megjelenítése
 * 
 * OPCIONÁLIS (NINCS IMPLEMENTÁLVA):
 * - Google regisztráció
 * 
 * @param navController Navigációs controller
 * @param viewModel Auth ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(LocalContext.current)
    )
) {
    // State-ek a beviteli mezőkhöz
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    // Focus manager
    val focusManager = LocalFocusManager.current
    
    // Auth state megfigyelése
    val authState by viewModel.authState.collectAsState()
    
    // Jelszó eltérés ellenőrzése
    val passwordMismatch = confirmPassword.isNotEmpty() && password != confirmPassword
    
    // Sikeres regisztráció esetén navigáció
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
    
    // UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Regisztráció") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Logo és cím
            Text(
                text = "Progr3SS",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Hozz létre egy új fiókot",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Felhasználónév mező
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Felhasználónév") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = "Felhasználó ikon")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Email mező
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email cím") },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = "Email ikon")
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
                isError = email.isNotEmpty() && !viewModel.isValidEmail(email)
            )
            
            if (email.isNotEmpty() && !viewModel.isValidEmail(email)) {
                Text(
                    text = "Érvénytelen email cím",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Jelszó mező
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Jelszó") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = "Jelszó ikon")
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) 
                                Icons.Default.Visibility 
                            else 
                                Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) 
                                "Jelszó elrejtése" 
                            else 
                                "Jelszó mutatása"
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
                isError = password.isNotEmpty() && !viewModel.isValidPassword(password)
            )
            
            if (password.isNotEmpty() && !viewModel.isValidPassword(password)) {
                Text(
                    text = "A jelszónak legalább 6 karakter hosszúnak kell lennie",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Jelszó megerősítés mező - PIROS SZEGÉLY HA ELTÉR
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Jelszó megerősítése") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = "Jelszó ikon")
                },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) 
                                Icons.Default.Visibility 
                            else 
                                Icons.Default.VisibilityOff,
                            contentDescription = if (confirmPasswordVisible) 
                                "Jelszó elrejtése" 
                            else 
                                "Jelszó mutatása"
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
                        // Regisztráció gomb funkcionalitás
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
                isError = passwordMismatch // PIROS SZEGÉLY HA NEM EGYEZIK
            )
            
            // Hibaüzenet ha a jelszavak nem egyeznek
            if (passwordMismatch) {
                Text(
                    text = "A jelszavak nem egyeznek",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 4.dp)
                )
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
                    .height(50.dp),
                enabled = authState !is AuthViewModel.AuthState.Loading &&
                        username.isNotEmpty() &&
                        viewModel.isValidEmail(email) &&
                        viewModel.isValidPassword(password) &&
                        !passwordMismatch
            ) {
                if (authState is AuthViewModel.AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Regisztráció", style = MaterialTheme.typography.titleMedium)
                }
            }
            
            // Hibaüzenet megjelenítése
            if (authState is AuthViewModel.AuthState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (authState as AuthViewModel.AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Login link
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Már van fiókod?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                TextButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Text("Jelentkezz be")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // OPCIONÁLIS: Google regisztráció (NINCS IMPLEMENTÁLVA)
            // OutlinedButton(
            //     onClick = { /* Google sign-up */ },
            //     modifier = Modifier.fillMaxWidth()
            // ) {
            //     Icon(painter = painterResource(R.drawable.ic_google), "Google")
            //     Spacer(Modifier.width(8.dp))
            //     Text("Regisztráció Google-lal")
            // }
        }
    }
}

/**
 * Preview a Register Screen-hez
 */
@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    Progr3SSTheme {
        RegisterScreen(navController = rememberNavController())
    }
}

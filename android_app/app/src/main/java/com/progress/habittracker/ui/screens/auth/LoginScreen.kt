package com.progress.habittracker.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
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
 * LoginScreen - Bejelentkezési képernyő
 * 
 * Funkciók:
 * - Email és jelszó beviteli mezők
 * - Bejelentkezés gomb -> AuthViewModel.signIn()
 * - "Még nincs fiókod?" link -> Register Screen
 * - Validáció és hibaüzenetek
 * - Loading state megjelenítése
 * 
 * OPCIONÁLIS (NINCS IMPLEMENTÁLVA):
 * - "Elfelejtett jelszó?" link
 * - Google bejelentkezés
 * 
 * @param navController Navigációs controller
 * @param viewModel Auth ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(LocalContext.current)
    )
) {
    // State-ek a beviteli mezőkhöz
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    // Focus manager a következő mezőre ugráshoz
    val focusManager = LocalFocusManager.current
    
    // Auth state megfigyelése
    val authState by viewModel.authState.collectAsState()
    
    // Sikeres login esetén navigáció
    LaunchedEffect(authState) {
        if (authState is AuthViewModel.AuthState.Success) {
            // Sikeres bejelentkezés -> Home Screen
            navController.navigate(Screen.Home.route) {
                // Töröljük az összes auth képernyőt a back stack-ből
                popUpTo(Screen.Login.route) { inclusive = true }
            }
            viewModel.resetState()
        }
    }
    
    // UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bejelentkezés") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo és cím
            Text(
                text = "Progr3SS",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Jelentkezz be a fiókodba",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
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
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        // Login gomb funkcionalitás
                        if (viewModel.isValidEmail(email) && password.isNotEmpty()) {
                            viewModel.signIn(email, password)
                        }
                    }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // OPCIONÁLIS: Elfelejtett jelszó link (NINCS IMPLEMENTÁLVA)
            // TextButton(
            //     onClick = { navController.navigate(Screen.ResetPassword.route) },
            //     modifier = Modifier.align(Alignment.End)
            // ) {
            //     Text("Elfelejtett jelszó?")
            // }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Bejelentkezés gomb
            Button(
                onClick = {
                    if (viewModel.isValidEmail(email) && password.isNotEmpty()) {
                        viewModel.signIn(email, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = authState !is AuthViewModel.AuthState.Loading &&
                        viewModel.isValidEmail(email) &&
                        password.isNotEmpty()
            ) {
                if (authState is AuthViewModel.AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Bejelentkezés", style = MaterialTheme.typography.titleMedium)
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
            
            // Regisztráció link
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Még nincs fiókod?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                TextButton(
                    onClick = { navController.navigate(Screen.Register.route) }
                ) {
                    Text("Regisztrálj")
                }
            }
            
            // OPCIONÁLIS: Google bejelentkezés (NINCS IMPLEMENTÁLVA)
            // Spacer(modifier = Modifier.height(16.dp))
            // OutlinedButton(
            //     onClick = { /* Google sign-in */ },
            //     modifier = Modifier.fillMaxWidth()
            // ) {
            //     Icon(painter = painterResource(R.drawable.ic_google), "Google")
            //     Spacer(Modifier.width(8.dp))
            //     Text("Bejelentkezés Google-lal")
            // }
        }
    }
}

/**
 * Preview a Login Screen-hez
 */
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    Progr3SSTheme {
        LoginScreen(navController = rememberNavController())
    }
}

package com.progress.habittracker.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
 * LoginScreen - Bejelentkezési képernyő
 *
 * Ez a képernyő felelős a felhasználók bejelentkeztetéséért.
 * Tartalmazza az email és jelszó beviteli mezőket, valamint a bejelentkezés gombot.
 * Lehetőséget biztosít a regisztrációs képernyőre való átváltásra is.
 *
 * Főbb funkciók:
 * - Felhasználói adatok bekérése (email, jelszó).
 * - Validáció (email formátum, üres mezők).
 * - Kommunikáció az AuthViewModel-lel a bejelentkezési folyamat kezelésére.
 * - Navigáció a főképernyőre sikeres bejelentkezés esetén.
 * - Hibaüzenetek megjelenítése sikertelen bejelentkezéskor.
 *
 * @param navController A navigációért felelős vezérlő, amely lehetővé teszi a képernyők közötti váltást.
 * @param viewModel Az autentikációs logikát kezelő ViewModel. Alapértelmezetten egy új példányt hoz létre a Factory segítségével.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(LocalContext.current)
    )
) {
    // Állapotváltozók a beviteli mezők értékeinek tárolására
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    // A jelszó láthatóságának állapota (rejtett/látható)
    var passwordVisible by remember { mutableStateOf(false) }
    
    // Tab váltó állapota (Login/Register). Jelenleg csak UI elemként funkcionál a váltás animációjához vagy jelzéséhez.
    var isLoginTab by remember { mutableStateOf(true) }
    
    // A fókusz kezelője, segítségével tudunk a billentyűzeten a "Következő" gombra kattintva a következő mezőre ugrani
    val focusManager = LocalFocusManager.current
    
    // Az autentikációs folyamat állapotának figyelése a ViewModel-ből (pl. Loading, Success, Error)
    val authState by viewModel.authState.collectAsState()
    
    // Hatás (Effect), amely akkor fut le, ha az authState változik.
    // Ha sikeres a bejelentkezés, navigálunk a Home képernyőre.
    LaunchedEffect(authState) {
        if (authState is AuthViewModel.AuthState.Success) {
            // Sikeres bejelentkezés -> Home Screen
            navController.navigate(Screen.Home.route) {
                // Töröljük az összes auth képernyőt a back stack-ből, hogy a "Vissza" gomb ne vigyen vissza a bejelentkezéshez
                popUpTo(Screen.Login.route) { inclusive = true }
            }
            // ViewModel állapotának alaphelyzetbe állítása, hogy visszatéréskor ne maradjon "Success" állapotban
            viewModel.resetState()
        }
    }
    
    // A képernyő alapvető elrendezése: Teljes képernyős doboz sötét háttérrel
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp)
    ) {
        // Oszlop elrendezés a tartalmak egymás alá helyezéséhez
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally // Középre igazítás vízszintesen
        ) {
            Spacer(modifier = Modifier.height(60.dp)) // Felső margó
            
            // Alkalmazás logója és neve: "Progr3SS"
            // A szöveg formázása (színek) buildAnnotatedString segítségével történik
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
            
            // Alcím: "Habit Planner & Tracker"
            Text(
                text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.app_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Login/Register Tab Switcher (Váltógomb)
            // Ez a komponens vizuálisan jelzi, hogy a felhasználó a Login vagy Register oldalon van-e,
            // és lehetőséget ad a váltásra.
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
                        isLoginTab = true
                        // Itt már a Login képernyőn vagyunk, így nem kell navigálni
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLoginTab) PrimaryPurple else Color.Transparent, // Aktív állapot jelzése színnel
                        contentColor = if (isLoginTab) TextPrimary else TextSecondary
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.login),
                        fontWeight = if (isLoginTab) FontWeight.Bold else FontWeight.Normal
                    )
                }
                
                // Register Tab Gomb
                Button(
                    onClick = { 
                        // Navigálás a Regisztrációs képernyőre
                        navController.navigate(Screen.Register.route)
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
                        text = androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.register),
                        fontWeight = FontWeight.Normal
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
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
                        keyboardType = KeyboardType.Email, // Email billentyűzet
                        imeAction = ImeAction.Next // "Következő" gomb megjelenítése
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) } // Ugrás a jelszó mezőre
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
                    // Hiba jelzése, ha az email nem üres és nem érvényes formátumú
                    isError = email.isNotEmpty() && !viewModel.isValidEmail(email)
                )
                
                // Hibaüzenet megjelenítése érvénytelen email esetén
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
                    // Jelszó láthatóságának kapcsolója (szem ikon)
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
                    // Vizuális transzformáció: csillagok vagy szöveg megjelenítése
                    visualTransformation = if (passwordVisible) 
                        VisualTransformation.None 
                    else 
                        PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done // "Kész" gomb megjelenítése
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus() // Billentyűzet elrejtése
                            // Ha minden érvényes, indítsuk a bejelentkezést
                            if (viewModel.isValidEmail(email) && password.isNotEmpty()) {
                                viewModel.signIn(email, password)
                            }
                        }
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
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // "Elfelejtett jelszó" link (Jelenleg nincs implementálva a funkció)
            TextButton(
                onClick = { 
                    // TODO: Reset Password képernyő implementálása és navigáció
                    // navController.navigate(Screen.ResetPassword.route)
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.forgot_password),
                    color = SuccessCyan,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
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
                    .height(54.dp),
                // Gomb letiltása, ha töltünk, vagy ha az adatok érvénytelenek
                enabled = authState !is AuthViewModel.AuthState.Loading &&
                        viewModel.isValidEmail(email) &&
                        password.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryPurple,
                    contentColor = TextPrimary,
                    disabledContainerColor = PrimaryPurple.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(27.dp)
            ) {
                // Töltésjelző vagy szöveg megjelenítése az állapottól függően
                if (authState is AuthViewModel.AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = TextPrimary
                    )
                } else {
                    Text(
                        androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.login_action),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Hibaüzenet megjelenítése, ha a bejelentkezés sikertelen volt
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
            
            // "Vagy jelentkezz be ezzel" elválasztó
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
                    text = "  " + androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.or_login_with) + "  ",
                    color = TextTertiary,
                    style = MaterialTheme.typography.bodySmall
                )
                Divider(
                    modifier = Modifier.weight(1f),
                    color = TextTertiary.copy(alpha = 0.3f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Google bejelentkezés gomb (Jelenleg csak UI, nincs implementálva)
            OutlinedButton(
                onClick = { 
                    // TODO: Google Sign-In implementálása
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
                // Google "G" betű ikonként
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
        }
    }
}

/**
 * Előnézet a LoginScreen-hez.
 * Lehetővé teszi a UI megtekintését az Android Studio Preview ablakában.
 */
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    Progr3SSTheme {
        LoginScreen(navController = rememberNavController())
    }
}

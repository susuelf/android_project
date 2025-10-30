package com.progr3ss.androidapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.progr3ss.androidapp.databinding.ActivityLoginBinding

/**
 * Login Activity
 * 
 * Bejelentkezési képernyő, ahol a felhasználó megadhatja email címét és jelszavát.
 * 
 * Funkciók:
 * - Email és jelszó validálás
 * - Bejelentkezés a backenden keresztül (POST /auth/local/signin)
 * - Navigálás a Register képernyőre
 * - Jelszó emlékeztető funkció (opcionális)
 * 
 * TODO: Backend integráció implementálása
 */
class LoginActivity : AppCompatActivity() {

    // ViewBinding az activity_login.xml layout eléréséhez
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // ViewBinding inicializálása
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
    }

    /**
     * UI elemek beállítása és event listener-ek hozzáadása
     */
    private fun setupViews() {
        // Bejelentkezés gomb kattintás kezelése
        binding.btnLogin.setOnClickListener {
            handleLogin()
        }

        // Regisztráció link kattintás kezelése
        binding.tvRegister.setOnClickListener {
            navigateToRegister()
        }

        // Elfelejtett jelszó link kattintás kezelése
        binding.tvForgotPassword.setOnClickListener {
            handleForgotPassword()
        }
    }

    /**
     * Bejelentkezés folyamat kezelése
     * 
     * Lépések:
     * 1. Input mezők validálása
     * 2. API hívás a backendre
     * 3. Token mentése
     * 4. Navigálás a Home képernyőre
     */
    private fun handleLogin() {
        // Email és jelszó lekérdezése az input mezőkből
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        // Validálás
        if (!validateInput(email, password)) {
            return
        }

        // Betöltés indikátor megjelenítése
        showLoading(true)

        // TODO: API hívás implementálása
        // - Retrofit service létrehozása
        // - POST /auth/local/signin endpoint hívása
        // - Token mentése DataStore-ba
        // - Navigálás Home képernyőre sikeres bejelentkezés esetén
        
        // Jelenleg: Mock bejelentkezés szimulálása
        simulateLogin(email, password)
    }

    /**
     * Input mezők validálása
     * 
     * @param email A megadott email cím
     * @param password A megadott jelszó
     * @return true ha minden mező helyes, false egyébként
     */
    private fun validateInput(email: String, password: String): Boolean {
        // Email validálás
        if (email.isEmpty()) {
            binding.etEmail.error = "Az email cím megadása kötelező"
            binding.etEmail.requestFocus()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Érvénytelen email cím formátum"
            binding.etEmail.requestFocus()
            return false
        }

        // Jelszó validálás
        if (password.isEmpty()) {
            binding.etPassword.error = "A jelszó megadása kötelező"
            binding.etPassword.requestFocus()
            return false
        }

        if (password.length < 6) {
            binding.etPassword.error = "A jelszó legalább 6 karakter legyen"
            binding.etPassword.requestFocus()
            return false
        }

        return true
    }

    /**
     * Betöltés állapot kezelése
     * 
     * @param isLoading true ha töltés folyamatban van
     */
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
        binding.etEmail.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading
    }

    /**
     * Mock bejelentkezés szimulálása (fejlesztési célra)
     * TODO: Cserélni éles API hívásra
     */
    private fun simulateLogin(email: String, password: String) {
        // Szimuláljuk a hálózati késleltetést
        binding.root.postDelayed({
            showLoading(false)
            
            // Teszt célból: ha az email "test@test.com" és jelszó "test123"
            if (email == "test@test.com" && password == "test123") {
                showMessage("Sikeres bejelentkezés!")
                // TODO: Navigálás Home képernyőre
            } else {
                showMessage("Hibás email cím vagy jelszó")
            }
        }, 1500)
    }

    /**
     * Navigálás a Regisztráció képernyőre
     */
    private fun navigateToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    /**
     * Elfelejtett jelszó funkció kezelése
     * TODO: Reset password képernyő implementálása
     */
    private fun handleForgotPassword() {
        showMessage("Jelszó visszaállítás funkció hamarosan elérhető")
        // TODO: Navigálás a Reset Password képernyőre
    }

    /**
     * Üzenet megjelenítése a felhasználónak (Snackbar)
     */
    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}

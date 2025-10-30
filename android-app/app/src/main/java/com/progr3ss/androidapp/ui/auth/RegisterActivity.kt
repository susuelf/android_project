package com.progr3ss.androidapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.progr3ss.androidapp.databinding.ActivityRegisterBinding

/**
 * Register Activity
 * 
 * Regisztrációs képernyő, ahol új felhasználók hozhatnak létre fiókot.
 * 
 * Funkciók:
 * - Felhasználónév, email és jelszó validálás
 * - Jelszó megerősítés ellenőrzése
 * - Regisztráció a backenden keresztül (POST /auth/local/signup)
 * - Navigálás vissza a Login képernyőre
 * - Opcionálisan: Google regisztráció
 * 
 * TODO: Backend integráció implementálása
 */
class RegisterActivity : AppCompatActivity() {

    // ViewBinding az activity_register.xml layout eléréséhez
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // ViewBinding inicializálása
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
    }

    /**
     * UI elemek beállítása és event listener-ek hozzáadása
     */
    private fun setupViews() {
        // Vissza gomb a toolbar-on
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // Regisztráció gomb kattintás kezelése
        binding.btnRegister.setOnClickListener {
            handleRegister()
        }

        // Bejelentkezés link kattintás kezelése
        binding.tvLogin.setOnClickListener {
            finish() // Visszalépés a Login képernyőre
        }
    }

    /**
     * Regisztráció folyamat kezelése
     * 
     * Lépések:
     * 1. Input mezők validálása
     * 2. Jelszó egyezés ellenőrzése
     * 3. API hívás a backendre
     * 4. Token mentése
     * 5. Navigálás a Home képernyőre
     */
    private fun handleRegister() {
        // Input mezők értékeinek lekérdezése
        val username = binding.etUsername.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etPasswordConfirm.text.toString()

        // Validálás
        if (!validateInput(username, email, password, confirmPassword)) {
            return
        }

        // Betöltés indikátor megjelenítése
        showLoading(true)

        // TODO: API hívás implementálása
        // - Retrofit service létrehozása
        // - POST /auth/local/signup endpoint hívása
        // - Token mentése DataStore-ba
        // - Navigálás Home képernyőre sikeres regisztráció esetén
        
        // Jelenleg: Mock regisztráció szimulálása
        simulateRegister(username, email)
    }

    /**
     * Input mezők validálása
     * 
     * @param username A megadott felhasználónév
     * @param email A megadott email cím
     * @param password A megadott jelszó
     * @param confirmPassword A jelszó megerősítése
     * @return true ha minden mező helyes, false egyébként
     */
    private fun validateInput(
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        // Felhasználónév validálás
        if (username.isEmpty()) {
            binding.etUsername.error = "A felhasználónév megadása kötelező"
            binding.etUsername.requestFocus()
            return false
        }

        if (username.length < 3) {
            binding.etUsername.error = "A felhasználónév legalább 3 karakter legyen"
            binding.etUsername.requestFocus()
            return false
        }

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

        // Jelszó megerősítés validálás
        if (confirmPassword.isEmpty()) {
            binding.etPasswordConfirm.error = "A jelszó megerősítése kötelező"
            binding.etPasswordConfirm.requestFocus()
            return false
        }

        // Jelszavak egyezésének ellenőrzése
        if (password != confirmPassword) {
            binding.etPasswordConfirm.error = "A jelszavak nem egyeznek"
            binding.etPasswordConfirm.requestFocus()
            
            // Piros keret megjelenítése mindkét jelszó mezőnél
            binding.tilPassword.boxStrokeColor = getColor(android.R.color.holo_red_dark)
            binding.tilPasswordConfirm.boxStrokeColor = getColor(android.R.color.holo_red_dark)
            
            return false
        }

        // Ha minden rendben, visszaállítjuk az alapértelmezett keret színt
        binding.tilPassword.boxStrokeColor = getColor(com.google.android.material.R.color.material_on_surface_stroke)
        binding.tilPasswordConfirm.boxStrokeColor = getColor(com.google.android.material.R.color.material_on_surface_stroke)

        return true
    }

    /**
     * Betöltés állapot kezelése
     * 
     * @param isLoading true ha töltés folyamatban van
     */
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !isLoading
        binding.etUsername.isEnabled = !isLoading
        binding.etEmail.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading
        binding.etPasswordConfirm.isEnabled = !isLoading
    }

    /**
     * Mock regisztráció szimulálása (fejlesztési célra)
     * TODO: Cserélni éles API hívásra
     */
    private fun simulateRegister(username: String, email: String) {
        // Szimuláljuk a hálózati késleltetést
        binding.root.postDelayed({
            showLoading(false)
            showMessage("Sikeres regisztráció! Üdvözlünk, $username!")
            
            // TODO: Navigálás Home képernyőre
            // Jelenleg: visszalépés a Login képernyőre
            finish()
        }, 1500)
    }

    /**
     * Üzenet megjelenítése a felhasználónak (Snackbar)
     */
    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}

package com.perez.horushealth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout // ¡Importación nueva!

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        // 1. Enlazamos los CONTENEDORES (Para mostrar los errores en rojo)
        val layoutCorreo = findViewById<TextInputLayout>(R.id.idcorreo)
        val layoutContrasena = findViewById<TextInputLayout>(R.id.idcontrasena)

        // 2. Enlazamos los CAMPOS DE TEXTO (Para leer lo que el usuario teclea)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)

        // 3. Enlazamos los BOTONES
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        btnLogin.setOnClickListener {
            // Limpiamos los errores anteriores antes de volver a validar
            layoutCorreo.error = null
            layoutContrasena.error = null

            // Leemos el texto de los EditText
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // --- VALIDACIÓN DE CORREO ---
            if (email.isEmpty()) {
                layoutCorreo.error = "El correo no puede estar vacío" // El error va al contenedor
                return@setOnClickListener
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                layoutCorreo.error = "Ingresa un formato de correo válido"
                return@setOnClickListener
            }

            // --- VALIDACIÓN DE CONTRASEÑA ---
            val passwordRegex = Regex("^[a-zA-Z0-9@#\$%&*\\-_]+$")

            if (password.isEmpty()) {
                layoutContrasena.error = "La contraseña no puede estar vacía"
                return@setOnClickListener
            } else if (!password.matches(passwordRegex)) {
                layoutContrasena.error = "No uses puntos. Solo letras, números y @ # $ % & * - _"
                return@setOnClickListener
            } else if (password.length < 6) {
                layoutContrasena.error = "La contraseña debe tener al menos 6 caracteres"
                return@setOnClickListener
            }

            // --- INGRESO EXITOSO ---
            Toast.makeText(this, "Ingresando como: $email", Toast.LENGTH_LONG).show()

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
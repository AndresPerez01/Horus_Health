package com.perez.horushealth.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.perez.horushealth.R
import com.perez.horushealth.data.LocalStorage

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        LocalStorage.ensureSeedData(this)

        if (LocalStorage.getSessionUser(this) != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.login)

        val layoutCorreo = findViewById<TextInputLayout>(R.id.idcorreo)
        val layoutContrasena = findViewById<TextInputLayout>(R.id.idcontrasena)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        btnLogin.setOnClickListener {
            layoutCorreo.error = null
            layoutContrasena.error = null

            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty()) {
                layoutCorreo.error = "El correo no puede estar vacío"
                return@setOnClickListener
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                layoutCorreo.error = "Ingresa un formato de correo válido"
                return@setOnClickListener
            }

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

            val user = LocalStorage.login(this, email, password)
            if (user == null) {
                layoutContrasena.error = "Credenciales incorrectas"
                return@setOnClickListener
            }

            Toast.makeText(this, "Ingresando como: ${user.name}", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}

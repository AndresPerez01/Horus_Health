package com.perez.horushealth.ui.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.perez.horushealth.R
import com.perez.horushealth.data.AppDatabase
import com.perez.horushealth.data.Usuario
import com.perez.horushealth.data.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Verificamos si ya hay una sesión activa
        if (SessionManager.getSession(this) != null) {
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
        val btnHuella = findViewById<MaterialButton>(R.id.btnHuella)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        // Recuperar el último correo exitoso para no tener que escribirlo siempre
        val prefs = getSharedPreferences("PreferenciasHorus", Context.MODE_PRIVATE)
        val ultimoCorreo = prefs.getString("ultimo_correo", "")
        if (!ultimoCorreo.isNullOrEmpty()) {
            etEmail.setText(ultimoCorreo)
        }

        btnLogin.setOnClickListener {
            layoutCorreo.error = null
            layoutContrasena.error = null

            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                layoutCorreo.error = "Ingresa un formato de correo válido"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                layoutContrasena.error = "La contraseña no puede estar vacía"
                return@setOnClickListener
            }

            // Consulta a Room mediante Corrutinas (Login Manual)
            lifecycleScope.launch(Dispatchers.IO) {
                val dao = AppDatabase.getBaseDatos(this@LoginActivity).horusDao()
                val user = dao.getUsuario(email)

                withContext(Dispatchers.Main) {
                    if (user != null && user.contrasena == password) {
                        // Guardamos el correo localmente para el próximo inicio
                        prefs.edit().putString("ultimo_correo", email).apply()

                        SessionManager.saveSession(this@LoginActivity, user.cedula)
                        Toast.makeText(this@LoginActivity, "Ingresando como: ${user.nombre}", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        layoutContrasena.error = "Credenciales incorrectas"
                    }
                }
            }
        }

        btnHuella.setOnClickListener {
            layoutCorreo.error = null
            val email = etEmail.text.toString().trim()

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                layoutCorreo.error = "Escribe tu correo para buscar tu huella"
                return@setOnClickListener
            }

            // Consulta a Room para validar permisos biométricos
            lifecycleScope.launch(Dispatchers.IO) {
                val dao = AppDatabase.getBaseDatos(this@LoginActivity).horusDao()
                val user = dao.getUsuario(email)

                withContext(Dispatchers.Main) {
                    if (user == null) {
                        layoutCorreo.error = "Este correo no está registrado"
                    } else if (!user.biometriaActivada) {
                        Toast.makeText(this@LoginActivity, "No habilitaste la huella al registrarte", Toast.LENGTH_LONG).show()
                    } else {
                        // Si todo es correcto, lanzamos el sensor
                        mostrarPromptHuella(user, prefs)
                    }
                }
            }
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    // --- FUNCIÓN QUE CONECTA CON EL HARDWARE DE HUELLA ---
    private fun mostrarPromptHuella(usuario: Usuario, prefs: SharedPreferences) {
        val executor: Executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext, "Lector cancelado", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    // Login biométrico exitoso
                    prefs.edit().putString("ultimo_correo", usuario.correo).apply()
                    SessionManager.saveSession(this@LoginActivity, usuario.cedula)

                    Toast.makeText(applicationContext, "Huella confirmada. Ingresando...", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Huella no reconocida. Intenta de nuevo.", Toast.LENGTH_SHORT).show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Acceso a Horus Health")
            .setSubtitle("Usa tu huella para ingresar como ${usuario.nombre}")
            .setNegativeButtonText("Usar Contraseña")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
package com.perez.horushealth.ui.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt          // <- Librería de HUELLA DIGITAL
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

/*
 * ============================================================================
 *  PANTALLA DE INICIO DE SESIÓN   (layout: login.xml)
 * ============================================================================
 *  ES LA PANTALLA DE ARRANQUE DE LA APP (está marcada como LAUNCHER
 *  en el AndroidManifest.xml).
 *
 *  Tiene DOS formas de entrar:
 *    1) Correo + contraseña  -> se validan contra Room.
 *    2) Huella digital       -> solo si el usuario tiene biometriaActivada = true.
 * ============================================================================
 */
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()   // Muestra la pantalla de carga (logo) mientras arranca la app
        super.onCreate(savedInstanceState)

        // ---- AUTO-LOGIN ----
        // Si SessionManager ya tiene una cédula guardada, el usuario NO cerró sesión:
        // lo mandamos directo a la pantalla principal y ni siquiera mostramos el login.
        if (SessionManager.getSession(this) != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()   // finish() = cerramos esta pantalla para que "atrás" no vuelva al login
            return
        }

        setContentView(R.layout.login)   // Enlaza esta Activity con su diseño XML

        // findViewById: conecta cada variable de Kotlin con su elemento del XML (por su id)
        val layoutCorreo = findViewById<TextInputLayout>(R.id.idcorreo)
        val layoutContrasena = findViewById<TextInputLayout>(R.id.idcontrasena)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
        val btnHuella = findViewById<MaterialButton>(R.id.btnHuella)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        // COMODIDAD: recordamos el último correo usado para no escribirlo cada vez.
        // (Otro uso de SharedPreferences, distinto al de la sesión.)
        val prefs = getSharedPreferences("PreferenciasHorus", Context.MODE_PRIVATE)
        val ultimoCorreo = prefs.getString("ultimo_correo", "")
        if (!ultimoCorreo.isNullOrEmpty()) {
            etEmail.setText(ultimoCorreo)
        }

        // ==================== LOGIN 1: CORREO + CONTRASEÑA ====================
        btnLogin.setOnClickListener {
            // Limpiamos errores previos antes de volver a validar
            layoutCorreo.error = null
            layoutContrasena.error = null

            val email = etEmail.text.toString().trim()      // trim() quita espacios sobrantes
            val password = etPassword.text.toString().trim()

            // VALIDACIÓN 1: formato de correo (Patterns.EMAIL_ADDRESS es un regex de Android)
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                layoutCorreo.error = "Ingresa un formato de correo válido"
                return@setOnClickListener   // corta aquí: no sigue al resto del código
            }

            // VALIDACIÓN 2: contraseña no vacía
            if (password.isEmpty()) {
                layoutContrasena.error = "La contraseña no puede estar vacía"
                return@setOnClickListener
            }

            // ---- CONSULTA A ROOM (con corrutinas) ----
            // Dispatchers.IO = hilo secundario para leer la BD (Room prohíbe hacerlo en el principal)
            lifecycleScope.launch(Dispatchers.IO) {
                val dao = AppDatabase.getBaseDatos(this@LoginActivity).horusDao()
                val user = dao.getUsuario(email)   // SELECT ... WHERE correo = email

                // withContext(Main) = volvemos al hilo principal para poder tocar la interfaz
                withContext(Dispatchers.Main) {
                    // Comparamos la contraseña. (En una app real se compararía el HASH.)
                    if (user != null && user.contrasena == password) {
                        prefs.edit().putString("ultimo_correo", email).apply()

                        // Guardamos la sesión: a partir de aquí la app sabe quién es el usuario
                        SessionManager.saveSession(this@LoginActivity, user.cedula)

                        Toast.makeText(this@LoginActivity, "Ingresando como: ${user.nombre}", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        // Mensaje genérico: no revelamos si falló el correo o la contraseña
                        layoutContrasena.error = "Credenciales incorrectas"
                    }
                }
            }
        }

        // ==================== LOGIN 2: HUELLA DIGITAL ====================
        btnHuella.setOnClickListener {
            layoutCorreo.error = null
            val email = etEmail.text.toString().trim()

            // Necesitamos el correo para saber A QUÉ USUARIO pertenece la huella
            // (la app es multiusuario, el sensor solo dice "sí/no", no dice quién es).
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                layoutCorreo.error = "Escribe tu correo para buscar tu huella"
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val dao = AppDatabase.getBaseDatos(this@LoginActivity).horusDao()
                val user = dao.getUsuario(email)

                withContext(Dispatchers.Main) {
                    if (user == null) {
                        layoutCorreo.error = "Este correo no está registrado"
                    } else if (!user.biometriaActivada) {
                        // El usuario existe pero nunca activó la huella (columna biometriaActivada = false)
                        Toast.makeText(this@LoginActivity, "No habilitaste la huella al registrarte", Toast.LENGTH_LONG).show()
                    } else {
                        // Todo correcto -> lanzamos el sensor
                        mostrarPromptHuella(user, prefs)
                    }
                }
            }
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    /**
     * ==================== EL SENSOR DE HUELLA ====================
     * IMPORTANTE PARA LA DEFENSA: la app NO guarda ni ve la huella.
     * La huella la valida ANDROID contra las huellas del sistema.
     * Nosotros solo recibimos un aviso (callback) de si fue correcta o no.
     */
    private fun mostrarPromptHuella(usuario: Usuario, prefs: SharedPreferences) {
        // Executor: en qué hilo se ejecutarán las respuestas del sensor (aquí, el principal)
        val executor: Executor = ContextCompat.getMainExecutor(this)

        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {   // objeto anónimo con los 3 posibles resultados

                // 1) ERROR: el usuario canceló, o no hay sensor, o se bloqueó por muchos intentos
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext, "Lector cancelado", Toast.LENGTH_SHORT).show()
                }

                // 2) ÉXITO: la huella coincidió -> aquí SÍ iniciamos sesión
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    prefs.edit().putString("ultimo_correo", usuario.correo).apply()
                    SessionManager.saveSession(this@LoginActivity, usuario.cedula)

                    Toast.makeText(applicationContext, "Huella confirmada. Ingresando...", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                }

                // 3) FALLO: leyó un dedo pero no coincide. El diálogo sigue abierto para reintentar.
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Huella no reconocida. Intenta de nuevo.", Toast.LENGTH_SHORT).show()
                }
            })

        // PromptInfo = los textos del cuadro de diálogo del sistema
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Acceso a Horus Health")
            .setSubtitle("Usa tu huella para ingresar como ${usuario.nombre}")
            .setNegativeButtonText("Usar Contraseña")   // Botón obligatorio de escape
            .build()

        biometricPrompt.authenticate(promptInfo)   // <- ESTA LÍNEA ABRE EL SENSOR
    }
}

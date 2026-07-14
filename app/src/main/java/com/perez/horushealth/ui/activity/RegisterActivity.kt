package com.perez.horushealth.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.perez.horushealth.R
import com.perez.horushealth.data.AppDatabase
import com.perez.horushealth.data.Usuario
import com.perez.horushealth.model.Country
import com.perez.horushealth.ui.adapter.CountryAdapter
import com.perez.horushealth.data.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.Executor

class RegisterActivity : AppCompatActivity() {

    private var selectedCountry: Country? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        val layoutNombre = findViewById<TextInputLayout>(R.id.layoutNombre)
        val layoutCedula = findViewById<TextInputLayout>(R.id.layoutCedula)
        val layoutCorreo = findViewById<TextInputLayout>(R.id.layoutCorreoReg)
        val layoutCountry = findViewById<TextInputLayout>(R.id.layoutCountry)
        val layoutTelefono = findViewById<TextInputLayout>(R.id.layoutTelefono)
        val layoutFecha = findViewById<TextInputLayout>(R.id.layoutFecha)
        val layoutContra = findViewById<TextInputLayout>(R.id.layoutContraReg)
        val layoutConfirmar = findViewById<TextInputLayout>(R.id.layoutConfirmar)

        val etNombre = findViewById<TextInputEditText>(R.id.etNombre)
        val etCedula = findViewById<TextInputEditText>(R.id.etCedula)
        val etCorreo = findViewById<TextInputEditText>(R.id.etCorreoReg)
        val atvCountry = findViewById<MaterialAutoCompleteTextView>(R.id.atvCountry)
        val etTelefono = findViewById<TextInputEditText>(R.id.etTelefono)
        val etFecha = findViewById<TextInputEditText>(R.id.etFecha)
        val etContra = findViewById<TextInputEditText>(R.id.etContraReg)
        val etConfirmar = findViewById<TextInputEditText>(R.id.etConfirmar)
        val cbBiometria = findViewById<MaterialCheckBox>(R.id.cbBiometria)
        val tvFortalezaReg = findViewById<TextView>(R.id.tvFortalezaReg)

        // Indicador de fortaleza de la contraseña en vivo
        etContra.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val (texto, color) = calcularFortaleza(etContra.text.toString())
                tvFortalezaReg.text = "Fortaleza: $texto"
                tvFortalezaReg.setTextColor(color)
            }
        })

        val countries = listOf(
            Country("Ecuador", "+593", "🇪🇨"),
            Country("Colombia", "+57", "🇨🇴"),
            Country("Perú", "+51", "🇵🇪"),
            Country("Chile", "+56", "🇨🇱"),
            Country("Argentina", "+54", "🇦🇷"),
            Country("México", "+52", "🇲🇽"),
        )
        val adapter = CountryAdapter(this, countries)
        atvCountry.setAdapter(adapter)

        selectedCountry = countries[0]
        atvCountry.setText(selectedCountry?.toString(), false)

        atvCountry.setOnItemClickListener { _, _, position, _ ->
            selectedCountry = adapter.getItem(position)
        }

        etFecha.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecciona tu fecha de nacimiento")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                val calendar = TimeZone.getTimeZone("UTC")
                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                formatter.timeZone = calendar
                val dateString = formatter.format(Date(selection))
                etFecha.setText(dateString)
                layoutFecha.error = null
            }
            datePicker.show(supportFragmentManager, "DATE_PICKER")
        }

        val btnCrearCuenta = findViewById<MaterialButton>(R.id.btnCrearCuenta)
        val tvYaTienesCuenta = findViewById<TextView>(R.id.tvYaTienesCuenta)
        val tvBackHeader = findViewById<TextView>(R.id.tvBackHeader)

        btnCrearCuenta.setOnClickListener {
            layoutNombre.error = null
            layoutCedula.error = null
            layoutCorreo.error = null
            layoutCountry.error = null
            layoutTelefono.error = null
            layoutFecha.error = null
            layoutContra.error = null
            layoutConfirmar.error = null

            val nombre = etNombre.text.toString().trim()
            val cedula = etCedula.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val telefono = etTelefono.text.toString().trim()
            val fecha = etFecha.text.toString().trim()
            val contra = etContra.text.toString().trim()
            val confirmar = etConfirmar.text.toString().trim()
            val aceptaBiometria = cbBiometria?.isChecked ?: false

            val nombreRegex = Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")
            if (nombre.isEmpty() || nombre.length < 4 || !nombre.matches(nombreRegex)) {
                layoutNombre.error = "Ingresa tu nombre y apellido completo (solo letras)"
                return@setOnClickListener
            }

            if (cedula.isEmpty() || cedula.length != 10) {
                layoutCedula.error = "La cédula debe tener exactamente 10 dígitos"
                return@setOnClickListener
            }

            if (correo.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                layoutCorreo.error = "Ingresa un formato de correo válido"
                return@setOnClickListener
            }

            if (telefono.isEmpty() || telefono.length < 7) {
                layoutTelefono.error = "Teléfono inválido"
                return@setOnClickListener
            }

            if (fecha.isEmpty()) {
                layoutFecha.error = "Fecha obligatoria"
                return@setOnClickListener
            }

            val passwordRegex = Regex("^[a-zA-Z0-9@#\$%&*\\-_]+$")
            if (contra.isEmpty() || contra.length < 6 || !contra.matches(passwordRegex)) {
                layoutContra.error = "Mínimo 6 caracteres válidos"
                return@setOnClickListener
            }

            if (confirmar != contra) {
                layoutConfirmar.error = "Las contraseñas no coinciden"
                return@setOnClickListener
            }

            // Validamos que no exista antes de pedir huella
            lifecycleScope.launch(Dispatchers.IO) {
                val dao = AppDatabase.getBaseDatos(this@RegisterActivity).horusDao()
                val usuarioExistentePorCorreo = dao.getUsuario(correo)
                val usuarioExistentePorCedula = dao.getUsuarioPorCedula(cedula)

                withContext(Dispatchers.Main) {
                    if (usuarioExistentePorCorreo != null) {
                        layoutCorreo.error = "Ya existe una cuenta con este correo"
                    } else if (usuarioExistentePorCedula != null) {
                        layoutCedula.error = "Esta cédula ya está registrada"
                    } else {
                        val nuevoUsuario = Usuario(
                            cedula = cedula,
                            nombre = nombre,
                            correo = correo,
                            telefono = telefono,
                            fechaNacimiento = fecha,
                            contrasena = contra,
                            paisCodigo = selectedCountry?.code ?: "+593",
                            biometriaActivada = aceptaBiometria
                        )

                        // Si aceptó biometría, lanzamos el sensor. Si no, guardamos directo.
                        if (aceptaBiometria) {
                            configurarHuellaDigital(nuevoUsuario)
                        } else {
                            guardarUsuarioYContinuar(nuevoUsuario)
                        }
                    }
                }
            }
        }

        tvYaTienesCuenta.setOnClickListener { finish() }
        tvBackHeader.setOnClickListener { finish() }
    }

    // --- FUNCIÓN PARA MOSTRAR EL SENSOR DE HUELLA ---
    private fun configurarHuellaDigital(usuario: Usuario) {
        // Verificamos primero si el dispositivo puede usar biometría.
        // En un emulador sin huella enrolada esto evita que el registro se quede bloqueado.
        val biometricManager = androidx.biometric.BiometricManager.from(this)
        val autenticadores = androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
        when (biometricManager.canAuthenticate(autenticadores)) {
            androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS -> {
                // Hay biometría disponible y enrolada: continuamos con el sensor.
            }
            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Toast.makeText(
                    this,
                    "No hay ninguna huella registrada en el dispositivo. Se creará tu cuenta sin biometría; puedes activarla luego desde los ajustes del teléfono.",
                    Toast.LENGTH_LONG
                ).show()
                guardarUsuarioYContinuar(usuario.copy(biometriaActivada = false))
                return
            }
            else -> {
                Toast.makeText(
                    this,
                    "Este dispositivo no tiene sensor de huella disponible. Se creará tu cuenta sin biometría.",
                    Toast.LENGTH_LONG
                ).show()
                guardarUsuarioYContinuar(usuario.copy(biometriaActivada = false))
                return
            }
        }

        val executor: Executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext, "Registro de huella cancelado", Toast.LENGTH_SHORT).show()
                    // Si cancela, desmarcamos el CheckBox para que decida qué hacer
                    findViewById<MaterialCheckBox>(R.id.cbBiometria).isChecked = false
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    // La huella fue correcta, guardamos el usuario con el estado "true"
                    guardarUsuarioYContinuar(usuario)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Huella no reconocida", Toast.LENGTH_SHORT).show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Vincular Huella Digital")
            .setSubtitle("Confirma tu identidad para vincularla a Horus Health")
            .setNegativeButtonText("Cancelar")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    // --- INDICADOR DE FORTALEZA DE CONTRASEÑA ---
    private fun calcularFortaleza(pass: String): Pair<String, Int> {
        if (pass.isEmpty()) return "—" to 0xFF667085.toInt()
        var puntos = 0
        if (pass.length >= 6) puntos++
        if (pass.length >= 10) puntos++
        if (pass.any { it.isDigit() }) puntos++
        if (pass.any { it.isLetter() }) puntos++
        if (pass.any { !it.isLetterOrDigit() }) puntos++
        return when {
            puntos <= 2 -> "Débil" to 0xFFD32F2F.toInt()
            puntos <= 3 -> "Media" to 0xFFF9A825.toInt()
            else -> "Fuerte" to 0xFF2E7D32.toInt()
        }
    }

    // --- FUNCIÓN PARA GUARDAR EN LA BASE DE DATOS (ROOM) ---
    private fun guardarUsuarioYContinuar(usuario: Usuario) {
        lifecycleScope.launch(Dispatchers.IO) {
            val dao = AppDatabase.getBaseDatos(this@RegisterActivity).horusDao()
            dao.addUsuario(usuario)
            SessionManager.saveSession(this@RegisterActivity, usuario.cedula)

            withContext(Dispatchers.Main) {
                startActivity(Intent(this@RegisterActivity, RegisterSuccessActivity::class.java))
                finish()
            }
        }
    }
}
package com.perez.horushealth.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.perez.horushealth.R
import com.perez.horushealth.data.LocalStorage
import com.perez.horushealth.model.Country
import com.perez.horushealth.model.UserProfile
import com.perez.horushealth.ui.adapter.CountryAdapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class RegisterActivity : AppCompatActivity() {

    private var selectedCountry: Country? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        // 1. Vinculación de Contenedores
        val layoutNombre = findViewById<TextInputLayout>(R.id.layoutNombre)
        val layoutCorreo = findViewById<TextInputLayout>(R.id.layoutCorreoReg)
        val layoutCountry = findViewById<TextInputLayout>(R.id.layoutCountry)
        val layoutTelefono = findViewById<TextInputLayout>(R.id.layoutTelefono)
        val layoutFecha = findViewById<TextInputLayout>(R.id.layoutFecha)
        val layoutContra = findViewById<TextInputLayout>(R.id.layoutContraReg)
        val layoutConfirmar = findViewById<TextInputLayout>(R.id.layoutConfirmar)

        // 2. Vinculación de Campos de Entrada
        val etNombre = findViewById<TextInputEditText>(R.id.etNombre)
        val etCorreo = findViewById<TextInputEditText>(R.id.etCorreoReg)
        val atvCountry = findViewById<MaterialAutoCompleteTextView>(R.id.atvCountry)
        val etTelefono = findViewById<TextInputEditText>(R.id.etTelefono)
        val etFecha = findViewById<TextInputEditText>(R.id.etFecha)
        val etContra = findViewById<TextInputEditText>(R.id.etContraReg)
        val etConfirmar = findViewById<TextInputEditText>(R.id.etConfirmar)

        // 3. Configuración del Selector de País
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
        
        // Seleccionar Ecuador por defecto
        selectedCountry = countries[0]
        atvCountry.setText(selectedCountry?.toString(), false)

        atvCountry.setOnItemClickListener { _, _, position, _ ->
            selectedCountry = adapter.getItem(position)
        }

        // 4. Control del campo Fecha (MaterialDatePicker)
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

        // 5. Vinculación de Botones y Textos de acción
        val btnCrearCuenta = findViewById<MaterialButton>(R.id.btnCrearCuenta)
        val tvYaTienesCuenta = findViewById<TextView>(R.id.tvYaTienesCuenta)
        val tvBackHeader = findViewById<TextView>(R.id.tvBackHeader)

        btnCrearCuenta.setOnClickListener {
            // Reiniciamos errores
            layoutNombre.error = null
            layoutCorreo.error = null
            layoutCountry.error = null
            layoutTelefono.error = null
            layoutFecha.error = null
            layoutContra.error = null
            layoutConfirmar.error = null

            val nombre = etNombre.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val telefono = etTelefono.text.toString().trim()
            val fecha = etFecha.text.toString().trim()
            val contra = etContra.text.toString().trim()
            val confirmar = etConfirmar.text.toString().trim()

            // A. Control del Nombre Completo
            val nombreRegex = Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")
            if (nombre.isEmpty()) {
                layoutNombre.error = "El nombre no puede estar vacío"
                return@setOnClickListener
            } else if (nombre.length < 4) {
                layoutNombre.error = "Ingresa tu nombre y apellido completo"
                return@setOnClickListener
            } else if (!nombre.matches(nombreRegex)) {
                layoutNombre.error = "El nombre solo debe contener letras"
                return@setOnClickListener
            }
            
            // B. Control del Correo Electrónico
            if (correo.isEmpty()) {
                layoutCorreo.error = "El correo electrónico es obligatorio"
                return@setOnClickListener
            } else if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                layoutCorreo.error = "Por favor, ingresa un formato de correo válido"
                return@setOnClickListener
            }

            if (selectedCountry == null) {
                layoutCountry.error = "Selecciona un país"
                return@setOnClickListener
            }

            if (telefono.isEmpty() || (telefono.length < 7)) {
                layoutTelefono.error = "Teléfono inválido"
                return@setOnClickListener
            }

            if (fecha.isEmpty()) {
                layoutFecha.error = "Fecha obligatoria"
                return@setOnClickListener
            }

            val passwordRegex = Regex("^[a-zA-Z0-9@#\$%&*\\-_]+$")
            if (contra.isEmpty()) {
                layoutContra.error = "La contraseña no puede estar vacía"
                return@setOnClickListener
            } else if (contra.length < 6) {
                layoutContra.error = "La contraseña debe tener al menos 6 caracteres"
                return@setOnClickListener
            } else if (!contra.matches(passwordRegex)) {
                layoutContra.error = "No uses puntos ni comas. Solo letras, números y @ # $ % & * - _"
                return@setOnClickListener
            }

            if (confirmar.isEmpty()) {
                layoutConfirmar.error = "Debes confirmar tu contraseña"
                return@setOnClickListener
            } else if (confirmar != contra) {
                layoutConfirmar.error = "Las contraseñas no coinciden"
                return@setOnClickListener
            }

            val result = LocalStorage.registerUser(
                context = this,
                user = UserProfile(
                    name = nombre,
                    email = correo,
                    phone = telefono,
                    birthDate = fecha,
                    password = contra,
                    countryCode = selectedCountry?.code ?: "+593"
                )
            )

            if (result.isSuccess) {
                startActivity(Intent(this, RegisterSuccessActivity::class.java))
                finish()
            } else {
                layoutCorreo.error = result.exceptionOrNull()?.message ?: "Error al registrar"
            }
        }

        tvYaTienesCuenta.setOnClickListener { finish() }
        tvBackHeader.setOnClickListener { finish() }
    }
}

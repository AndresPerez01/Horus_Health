package com.perez.horushealth

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Calendar

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        // 1. Vinculación de Contenedores (Para pintar los errores en rojo)
        val layoutNombre = findViewById<TextInputLayout>(R.id.layoutNombre)
        val layoutCorreo = findViewById<TextInputLayout>(R.id.layoutCorreoReg)
        val layoutTelefono = findViewById<TextInputLayout>(R.id.layoutTelefono)
        val layoutFecha = findViewById<TextInputLayout>(R.id.layoutFecha)
        val layoutContra = findViewById<TextInputLayout>(R.id.layoutContraReg)
        val layoutConfirmar = findViewById<TextInputLayout>(R.id.layoutConfirmar)

        // 2. Vinculación de Campos de Entrada (Para leer los datos)
        val etNombre = findViewById<TextInputEditText>(R.id.etNombre)
        val etCorreo = findViewById<TextInputEditText>(R.id.etCorreoReg)
        val etTelefono = findViewById<TextInputEditText>(R.id.etTelefono)
        val etFecha = findViewById<TextInputEditText>(R.id.etFecha)
        val etContra = findViewById<TextInputEditText>(R.id.etContraReg)
        val etConfirmar = findViewById<TextInputEditText>(R.id.etConfirmar)

        // 3. Vinculación de Botones y Textos de acción
        val btnCrearCuenta = findViewById<MaterialButton>(R.id.btnCrearCuenta)
        val tvYaTienesCuenta = findViewById<TextView>(R.id.tvYaTienesCuenta)
        val tvBackHeader = findViewById<TextView>(R.id.tvBackHeader)

        // 4. Control del campo Fecha (Despliega el calendario nativo)
        etFecha.setOnClickListener {
            val calendario = Calendar.getInstance()
            val anio = calendario.get(Calendar.YEAR)
            val mes = calendario.get(Calendar.MONTH)
            val dia = calendario.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, year, month, dayOfMonth ->
                val fechaSeleccionada = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
                etFecha.setText(fechaSeleccionada)
                layoutFecha.error = null // Limpia el error si ya seleccionó fecha
            }, anio, mes, dia)

            datePicker.show()
        }

        // 5. Control del Botón "Crear mi cuenta"
        btnCrearCuenta.setOnClickListener {
            // Reiniciamos todos los errores antes de validar
            layoutNombre.error = null
            layoutCorreo.error = null
            layoutTelefono.error = null
            layoutFecha.error = null
            layoutContra.error = null
            layoutConfirmar.error = null

            // Captura de datos ingresados por el usuario
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

            // C. Control del Teléfono
            if (telefono.isEmpty()) {
                layoutTelefono.error = "El número de teléfono es obligatorio"
                return@setOnClickListener
            } else if (telefono.length < 9) {
                layoutTelefono.error = "Ingresa un número telefónico válido"
                return@setOnClickListener
            }

            // D. Control de la Fecha de Nacimiento
            if (fecha.isEmpty()) {
                layoutFecha.error = "Selecciona tu fecha de nacimiento"
                return@setOnClickListener
            }

            // E. Control de la Contraseña (Misma restricción: Sin puntos)
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

            // F. Control de la Confirmación de Contraseña
            if (confirmar.isEmpty()) {
                layoutConfirmar.error = "Debes confirmar tu contraseña"
                return@setOnClickListener
            } else if (confirmar != contra) {
                layoutConfirmar.error = "Las contraseñas ingresadas no coinciden"
                return@setOnClickListener
            }

            val result = LocalStorage.registerUser(
                context = this,
                user = UserProfile(
                    name = nombre,
                    email = correo,
                    phone = telefono,
                    birthDate = fecha,
                    password = contra
                )
            )
            if (result.isFailure) {
                layoutCorreo.error = result.exceptionOrNull()?.message ?: "No se pudo crear la cuenta"
                return@setOnClickListener
            }

            val intent = Intent(this, RegisterSuccessActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 6. Control del texto "¿Ya tienes cuenta? Inicia sesión"
        tvYaTienesCuenta.setOnClickListener {
            // Simplemente cerramos esta actividad para regresar al Login que está detrás
            finish()
        }

        tvBackHeader.setOnClickListener {
            finish()
        }
    }
}
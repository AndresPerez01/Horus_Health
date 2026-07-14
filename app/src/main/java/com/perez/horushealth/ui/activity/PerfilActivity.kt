package com.perez.horushealth.ui.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.perez.horushealth.R
import com.perez.horushealth.data.AppDatabase
import com.perez.horushealth.data.SessionManager
import com.perez.horushealth.data.TarjetaManager
import com.perez.horushealth.data.Usuario
import com.perez.horushealth.model.Country
import com.perez.horushealth.ui.adapter.CountryAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Patterns
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.Executor

class PerfilActivity : AppCompatActivity() {

    private var cedulaUsuario: String? = null
    private var usuarioActual: Usuario? = null

    private val paises = listOf(
        Country("Ecuador", "+593", "🇪🇨"),
        Country("Colombia", "+57", "🇨🇴"),
        Country("Perú", "+51", "🇵🇪"),
        Country("Chile", "+56", "🇨🇱"),
        Country("Argentina", "+54", "🇦🇷"),
        Country("México", "+52", "🇲🇽"),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.perfil_usuario)

        cedulaUsuario = SessionManager.getSession(this)
        val tvPerfilCorreo = findViewById<TextView>(R.id.tvPerfilCorreo)
        val tvTarjetaSubtitulo = findViewById<TextView>(R.id.tvTarjetaSubtitulo)
        val tvHuellaSubtitulo = findViewById<TextView>(R.id.tvHuellaSubtitulo)

        cedulaUsuario?.let { cedula ->
            lifecycleScope.launch(Dispatchers.IO) {
                val dao = AppDatabase.getBaseDatos(this@PerfilActivity).horusDao()
                val user = dao.getUsuarioPorCedula(cedula)

                withContext(Dispatchers.Main) {
                    usuarioActual = user
                    if (user != null) {
                        tvPerfilCorreo.text = user.correo
                    }
                    refrescarSubtituloTarjeta(tvTarjetaSubtitulo)
                    refrescarSubtituloHuella(tvHuellaSubtitulo)
                }
            }
        }

        val btnNavInicio = findViewById<LinearLayout>(R.id.btnNavInicio)
        btnNavInicio.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        findViewById<MaterialCardView>(R.id.cardEditarDatos).setOnClickListener {
            mostrarDialogoEditarDatos(tvPerfilCorreo)
        }
        findViewById<MaterialCardView>(R.id.cardEditarTarjeta).setOnClickListener {
            mostrarDialogoTarjeta(tvTarjetaSubtitulo)
        }
        findViewById<MaterialCardView>(R.id.cardCambiarPassword).setOnClickListener {
            mostrarDialogoCambiarPassword()
        }
        findViewById<MaterialCardView>(R.id.cardHuella).setOnClickListener {
            gestionarHuella(tvHuellaSubtitulo)
        }
    }

    private fun refrescarSubtituloHuella(tv: TextView) {
        tv.text = if (usuarioActual?.biometriaActivada == true) {
            "Activada · toca para desactivar"
        } else {
            "No activada · toca para registrar tu huella"
        }
    }

    private fun refrescarSubtituloTarjeta(tv: TextView) {
        val cedula = cedulaUsuario ?: return
        val tarjeta = TarjetaManager.getTarjeta(this, cedula)
        tv.text = if (tarjeta != null) {
            "${tarjeta.marca} terminada en **** ${tarjeta.ultimos4}"
        } else {
            "Sin tarjeta registrada"
        }
    }

    // ------------------- ACTUALIZAR DATOS -------------------
    private fun mostrarDialogoEditarDatos(tvPerfilCorreo: TextView) {
        val user = usuarioActual
        if (user == null) {
            Toast.makeText(this, "No se pudo cargar tu información", Toast.LENGTH_SHORT).show()
            return
        }

        val vista = layoutInflater.inflate(R.layout.dialog_editar_datos, null)
        val layoutNombre = vista.findViewById<TextInputLayout>(R.id.layoutDlgNombre)
        val layoutCorreo = vista.findViewById<TextInputLayout>(R.id.layoutDlgCorreo)
        val layoutTelefono = vista.findViewById<TextInputLayout>(R.id.layoutDlgTelefono)
        val layoutFecha = vista.findViewById<TextInputLayout>(R.id.layoutDlgFecha)
        val etNombre = vista.findViewById<TextInputEditText>(R.id.etDlgNombre)
        val etCorreo = vista.findViewById<TextInputEditText>(R.id.etDlgCorreo)
        val etTelefono = vista.findViewById<TextInputEditText>(R.id.etDlgTelefono)
        val etFecha = vista.findViewById<TextInputEditText>(R.id.etDlgFecha)
        val atvCountry = vista.findViewById<MaterialAutoCompleteTextView>(R.id.atvDlgCountry)

        // Selector de país reutilizando el adapter de la app
        val adapter = CountryAdapter(this, paises)
        atvCountry.setAdapter(adapter)
        var paisSeleccionado = paises.firstOrNull { it.code == user.paisCodigo } ?: paises[0]
        atvCountry.setText(paisSeleccionado.toString(), false)
        atvCountry.setOnItemClickListener { _, _, position, _ ->
            adapter.getItem(position)?.let { paisSeleccionado = it }
        }

        etNombre.setText(user.nombre)
        etCorreo.setText(user.correo)
        etTelefono.setText(user.telefono)
        etFecha.setText(user.fechaNacimiento)

        etFecha.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecciona tu fecha de nacimiento")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()
            datePicker.addOnPositiveButtonClickListener { selection ->
                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                formatter.timeZone = TimeZone.getTimeZone("UTC")
                etFecha.setText(formatter.format(Date(selection)))
                layoutFecha.error = null
            }
            datePicker.show(supportFragmentManager, "DATE_PICKER_PERFIL")
        }

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Actualizar mis datos")
            .setView(vista)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar", null)
            .create()
        dialog.show()

        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            layoutNombre.error = null
            layoutCorreo.error = null
            layoutTelefono.error = null
            layoutFecha.error = null

            val nombre = etNombre.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val telefono = etTelefono.text.toString().trim()
            val fecha = etFecha.text.toString().trim()

            val nombreRegex = Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")
            if (nombre.length < 4 || !nombre.matches(nombreRegex)) {
                layoutNombre.error = "Ingresa tu nombre completo (solo letras)"
                return@setOnClickListener
            }
            if (correo.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                layoutCorreo.error = "Ingresa un formato de correo válido"
                return@setOnClickListener
            }
            if (telefono.length < 7) {
                layoutTelefono.error = "Teléfono inválido"
                return@setOnClickListener
            }
            if (fecha.isEmpty()) {
                layoutFecha.error = "Fecha obligatoria"
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val dao = AppDatabase.getBaseDatos(this@PerfilActivity).horusDao()
                // Validamos que el correo no lo tenga otro usuario
                val existente = dao.getUsuario(correo)
                if (existente != null && existente.cedula != user.cedula) {
                    withContext(Dispatchers.Main) {
                        layoutCorreo.error = "Ese correo ya está en uso por otra cuenta"
                    }
                    return@launch
                }

                val actualizado = user.copy(
                    nombre = nombre,
                    correo = correo,
                    telefono = telefono,
                    fechaNacimiento = fecha,
                    paisCodigo = paisSeleccionado.code
                )
                dao.actualizarUsuario(actualizado)
                withContext(Dispatchers.Main) {
                    usuarioActual = actualizado
                    tvPerfilCorreo.text = actualizado.correo
                    Toast.makeText(this@PerfilActivity, "Datos actualizados", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }
    }

    // ------------------- TARJETA -------------------
    private fun mostrarDialogoTarjeta(tvSubtitulo: TextView) {
        val cedula = cedulaUsuario
        if (cedula == null) {
            Toast.makeText(this, "No se pudo cargar tu sesión", Toast.LENGTH_SHORT).show()
            return
        }

        val vista = layoutInflater.inflate(R.layout.dialog_editar_tarjeta, null)
        val layoutTitular = vista.findViewById<TextInputLayout>(R.id.layoutDlgTitular)
        val layoutNumero = vista.findViewById<TextInputLayout>(R.id.layoutDlgNumero)
        val layoutExp = vista.findViewById<TextInputLayout>(R.id.layoutDlgExp)
        val layoutCvv = vista.findViewById<TextInputLayout>(R.id.layoutDlgCvv)
        val etTitular = vista.findViewById<TextInputEditText>(R.id.etDlgTitular)
        val etNumero = vista.findViewById<TextInputEditText>(R.id.etDlgNumero)
        val etExp = vista.findViewById<TextInputEditText>(R.id.etDlgExp)
        val etCvv = vista.findViewById<TextInputEditText>(R.id.etDlgCvv)

        // Vistas de la tarjeta (preview)
        val tvCardNumero = vista.findViewById<TextView>(R.id.tvCardNumero)
        val tvCardTitular = vista.findViewById<TextView>(R.id.tvCardTitular)
        val tvCardExp = vista.findViewById<TextView>(R.id.tvCardExp)
        val tvCardBrand = vista.findViewById<TextView>(R.id.tvCardBrand)

        // Precargamos lo que ya está guardado (no el número completo)
        val tarjetaGuardada = TarjetaManager.getTarjeta(this, cedula)
        if (tarjetaGuardada != null) {
            etTitular.setText(tarjetaGuardada.titular)
            etExp.setText(tarjetaGuardada.expiracion)
        }

        // --- Sincronización en vivo del preview ---
        etTitular.addTextChangedListener(SimpleWatcher {
            val t = etTitular.text.toString().trim().uppercase()
            tvCardTitular.text = if (t.isEmpty()) "NOMBRE APELLIDO" else t
        })
        etNumero.addTextChangedListener(SimpleWatcher {
            val soloDigitos = etNumero.text.toString().filter { it.isDigit() }
            tvCardNumero.text = formatearNumeroPreview(soloDigitos)
            tvCardBrand.text = marcaCorta(soloDigitos)
        })
        etExp.addTextChangedListener(object : TextWatcher {
            private var editando = false
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (editando) return
                editando = true
                val digitos = (s?.toString() ?: "").filter { it.isDigit() }.take(4)
                val formateado = when {
                    digitos.length >= 3 -> digitos.substring(0, 2) + "/" + digitos.substring(2)
                    else -> digitos
                }
                if (formateado != s.toString()) {
                    etExp.setText(formateado)
                    etExp.setSelection(formateado.length)
                }
                tvCardExp.text = if (formateado.isEmpty()) "MM/AA" else formateado
                editando = false
            }
        })

        // Estado inicial del preview
        etTitular.text?.let { if (it.isNotEmpty()) tvCardTitular.text = it.toString().uppercase() }
        etExp.text?.let { if (it.isNotEmpty()) tvCardExp.text = it.toString() }

        val builder = MaterialAlertDialogBuilder(this)
            .setTitle(if (tarjetaGuardada != null) "Editar tarjeta" else "Agregar tarjeta")
            .setView(vista)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar", null)
        if (tarjetaGuardada != null) {
            builder.setNeutralButton("Eliminar", null)
        }
        val dialog = builder.create()
        dialog.show()

        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            layoutTitular.error = null
            layoutNumero.error = null
            layoutExp.error = null
            layoutCvv.error = null

            val titular = etTitular.text.toString().trim()
            val numero = etNumero.text.toString().filter { it.isDigit() }
            val exp = etExp.text.toString().trim()
            val cvv = etCvv.text.toString().trim()

            if (titular.length < 4) {
                layoutTitular.error = "Ingresa el nombre del titular"
                return@setOnClickListener
            }
            if (numero.length != 16 || !numero.all { it.isDigit() }) {
                layoutNumero.error = "El número debe tener 16 dígitos"
                return@setOnClickListener
            }
            if (!Regex("^(0[1-9]|1[0-2])/\\d{2}$").matches(exp)) {
                layoutExp.error = "Formato inválido (MM/AA)"
                return@setOnClickListener
            }
            if (cvv.length !in 3..4) {
                layoutCvv.error = "CVV inválido"
                return@setOnClickListener
            }

            // El CVV NO se persiste: solo se valida.
            val tarjeta = TarjetaManager.Tarjeta(
                titular = titular,
                marca = detectarMarca(numero),
                ultimos4 = numero.takeLast(4),
                expiracion = exp
            )
            TarjetaManager.guardarTarjeta(this, cedula, tarjeta)
            refrescarSubtituloTarjeta(tvSubtitulo)
            Toast.makeText(this, "Tarjeta guardada", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        if (tarjetaGuardada != null) {
            dialog.getButton(android.app.AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                TarjetaManager.eliminarTarjeta(this, cedula)
                refrescarSubtituloTarjeta(tvSubtitulo)
                Toast.makeText(this, "Tarjeta eliminada", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
    }

    private fun formatearNumeroPreview(digitos: String): String {
        val relleno = (digitos + "•".repeat(16)).take(16)
        return relleno.chunked(4).joinToString(" ")
    }

    private fun marcaCorta(numero: String): String = when {
        numero.startsWith("4") -> "VISA"
        numero.startsWith("5") -> "MASTERCARD"
        numero.startsWith("3") -> "AMEX"
        else -> "TARJETA"
    }

    private fun detectarMarca(numero: String): String = when {
        numero.startsWith("4") -> "Visa"
        numero.startsWith("5") -> "Mastercard"
        numero.startsWith("3") -> "American Express"
        else -> "Tarjeta"
    }

    // ------------------- HUELLA DIGITAL -------------------
    private fun gestionarHuella(tvHuellaSubtitulo: TextView) {
        val user = usuarioActual
        if (user == null) {
            Toast.makeText(this, "No se pudo cargar tu información", Toast.LENGTH_SHORT).show()
            return
        }

        // Si ya está activada, ofrecemos desactivarla.
        if (user.biometriaActivada) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Desactivar huella")
                .setMessage("¿Quieres dejar de usar tu huella para iniciar sesión? Podrás volver a activarla cuando quieras.")
                .setPositiveButton("Desactivar") { _, _ ->
                    guardarEstadoHuella(user, false, tvHuellaSubtitulo, "Huella desactivada")
                }
                .setNegativeButton("Cancelar", null)
                .show()
            return
        }

        // No está activada: verificamos que el dispositivo pueda usar biometría.
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                lanzarPromptRegistroHuella(user, tvHuellaSubtitulo)
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Sin huellas registradas")
                    .setMessage("Aún no tienes ninguna huella registrada en este teléfono. Ve a Ajustes › Seguridad › Huella digital, registra una y vuelve aquí para activarla en Horus Health.")
                    .setPositiveButton("Entendido", null)
                    .show()
            }
            else -> {
                Toast.makeText(
                    this,
                    "Este dispositivo no tiene un sensor de huella disponible.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun lanzarPromptRegistroHuella(user: Usuario, tvHuellaSubtitulo: TextView) {
        val executor: Executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext, "Registro de huella cancelado", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    guardarEstadoHuella(user, true, tvHuellaSubtitulo, "Huella activada correctamente")
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Huella no reconocida", Toast.LENGTH_SHORT).show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Vincular Huella Digital")
            .setSubtitle("Confirma tu identidad para activarla en Horus Health")
            .setNegativeButtonText("Cancelar")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun guardarEstadoHuella(
        user: Usuario,
        activada: Boolean,
        tvHuellaSubtitulo: TextView,
        mensaje: String
    ) {
        val actualizado = user.copy(biometriaActivada = activada)
        lifecycleScope.launch(Dispatchers.IO) {
            AppDatabase.getBaseDatos(this@PerfilActivity).horusDao().actualizarUsuario(actualizado)
            withContext(Dispatchers.Main) {
                usuarioActual = actualizado
                refrescarSubtituloHuella(tvHuellaSubtitulo)
                Toast.makeText(this@PerfilActivity, mensaje, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ------------------- CAMBIAR CONTRASEÑA -------------------
    private fun mostrarDialogoCambiarPassword() {
        val user = usuarioActual
        if (user == null) {
            Toast.makeText(this, "No se pudo cargar tu información", Toast.LENGTH_SHORT).show()
            return
        }

        val vista = layoutInflater.inflate(R.layout.dialog_cambiar_password, null)
        val layoutActual = vista.findViewById<TextInputLayout>(R.id.layoutDlgActual)
        val layoutNueva = vista.findViewById<TextInputLayout>(R.id.layoutDlgNueva)
        val layoutConfirmar = vista.findViewById<TextInputLayout>(R.id.layoutDlgConfirmar)
        val etActual = vista.findViewById<TextInputEditText>(R.id.etDlgActual)
        val etNueva = vista.findViewById<TextInputEditText>(R.id.etDlgNueva)
        val etConfirmar = vista.findViewById<TextInputEditText>(R.id.etDlgConfirmar)
        val tvFortaleza = vista.findViewById<TextView>(R.id.tvFortaleza)

        etNueva.addTextChangedListener(SimpleWatcher {
            val (texto, color) = calcularFortaleza(etNueva.text.toString())
            tvFortaleza.text = "Fortaleza: $texto"
            tvFortaleza.setTextColor(color)
        })

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Cambiar contraseña")
            .setView(vista)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar", null)
            .create()
        dialog.show()

        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            layoutActual.error = null
            layoutNueva.error = null
            layoutConfirmar.error = null

            val actual = etActual.text.toString().trim()
            val nueva = etNueva.text.toString().trim()
            val confirmar = etConfirmar.text.toString().trim()

            if (actual != user.contrasena) {
                layoutActual.error = "La contraseña actual no es correcta"
                return@setOnClickListener
            }
            val passwordRegex = Regex("^[a-zA-Z0-9@#\$%&*\\-_]+$")
            if (nueva.length < 6 || !nueva.matches(passwordRegex)) {
                layoutNueva.error = "Mínimo 6 caracteres válidos"
                return@setOnClickListener
            }
            if (nueva == actual) {
                layoutNueva.error = "La nueva contraseña debe ser diferente"
                return@setOnClickListener
            }
            if (confirmar != nueva) {
                layoutConfirmar.error = "Las contraseñas no coinciden"
                return@setOnClickListener
            }

            val actualizado = user.copy(contrasena = nueva)
            lifecycleScope.launch(Dispatchers.IO) {
                AppDatabase.getBaseDatos(this@PerfilActivity).horusDao().actualizarUsuario(actualizado)
                withContext(Dispatchers.Main) {
                    usuarioActual = actualizado
                    Toast.makeText(this@PerfilActivity, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }
    }

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

    /** TextWatcher simplificado que solo reacciona al texto final. */
    private class SimpleWatcher(private val onChanged: () -> Unit) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) = onChanged()
    }
}

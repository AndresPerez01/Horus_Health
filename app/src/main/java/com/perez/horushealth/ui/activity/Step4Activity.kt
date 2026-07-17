package com.perez.horushealth.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.perez.horushealth.R
import com.perez.horushealth.data.AppDatabase
import com.perez.horushealth.data.Cita
import com.perez.horushealth.data.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/*
 * ============================================================================
 *  PASO 4: RESUMEN Y CONFIRMACIÓN   (layout: step4_resumen.xml)
 * ============================================================================
 *  Es el paso final del CAMINO MANUAL (Step1 -> 2 -> 2a -> 3 -> 4 -> 5).
 *  AQUÍ ES DONDE SE GUARDA LA CITA EN LA BASE DE DATOS (camino manual).
 *  (En el camino automático la cita se guarda en Step5Activity.)
 * ============================================================================
 */
class Step4Activity : AppCompatActivity() {

    private var lugarCompleto = "Asignación en recepción"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.step4_resumen)

        val tvEspecialidad = findViewById<TextView>(R.id.tvResumenEspecialidad)
        val tvMedico = findViewById<TextView>(R.id.tvResumenMedico)
        val tvClinica = findViewById<TextView>(R.id.tvResumenClinica)
        val tvConsultorio = findViewById<TextView>(R.id.tvResumenConsultorio)
        val tvFecha = findViewById<TextView>(R.id.tvResumenFecha)
        val tvHora = findViewById<TextView>(R.id.tvResumenHora)
        val tvPaciente = findViewById<TextView>(R.id.tvResumenNombrePaciente)

        // ---- RECIBIMOS LOS DATOS QUE VIENEN ARRASTRÁNDOSE DESDE LOS PASOS ANTERIORES ----
        // Los datos viajan entre pantallas con Intent.putExtra() / getStringExtra().
        // El "?: """ es el operador Elvis: si el dato no llegó, usa cadena vacía en vez de null.
        val especialidad = intent.getStringExtra("ESPECIALIDAD_SELECCIONADA") ?: ""
        val idMedico = intent.getStringExtra("MEDICO_ID") ?: ""      // la licencia del médico (FK)
        val nombreMedico = intent.getStringExtra("MEDICO_NOMBRE") ?: ""
        val fecha = intent.getStringExtra("FECHA_SELECCIONADA") ?: ""  // formato "2026-07-16"
        val hora = intent.getStringExtra("HORA_SELECCIONADA") ?: ""    // formato "10:00"

        tvEspecialidad.text = especialidad
        tvMedico.text = nombreMedico
        tvFecha.text = fecha
        tvHora.text = "$hora h"

        // 1. Buscamos al Médico y al Usuario en Room simultáneamente
        val cedulaActiva = SessionManager.getSession(this)

        lifecycleScope.launch(Dispatchers.IO) {
            val dao = AppDatabase.getBaseDatos(this@Step4Activity).horusDao()

            // Traemos todos los médicos de la especialidad y buscamos el nuestro
            val medicos = dao.getMedicosPorEspecialidad(especialidad)
            val medico = medicos.find { it.licencia == idMedico }

            val usuario = if (cedulaActiva != null) dao.getUsuarioPorCedula(cedulaActiva) else null

            withContext(Dispatchers.Main) {
                if (medico != null) {
                    tvClinica.text = medico.clinica
                    tvConsultorio.text = medico.pisoYHabitacion
                    lugarCompleto = "${medico.clinica} - ${medico.pisoYHabitacion}"
                }
                if (usuario != null) {
                    tvPaciente.text = usuario.nombre
                }
            }
        }

        val btnEditar = findViewById<MaterialButton>(R.id.btnEditarResumen)
        btnEditar.setOnClickListener { finish() }

        // ==================== BOTÓN "CONFIRMAR CITA" ====================
        val btnConfirmar = findViewById<MaterialButton>(R.id.btnConfirmarCita)
        btnConfirmar.setOnClickListener {
            // PROTECCIÓN 1 (doble clic): si el usuario pulsa dos veces rápido se crearían
            // DOS citas iguales. Desactivamos el botón al primer clic.
            btnConfirmar.isEnabled = false

            // Validamos que la sesión no se haya perdido
            if (cedulaActiva == null) {
                Toast.makeText(this, "Error de sesión. Vuelve a iniciar sesión.", Toast.LENGTH_SHORT).show()
                btnConfirmar.isEnabled = true // Lo volvemos a prender por si el usuario quiere reintentar
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val dao = AppDatabase.getBaseDatos(this@Step4Activity).horusDao()

                // PROTECCIÓN 2 (doble reserva): volvemos a preguntar a la BD si esa hora
                // sigue libre. Es una "revalidación de último segundo": entre que el usuario
                // vio los horarios (Paso 3) y confirmó aquí, otro paciente pudo tomarla.
                val horasOcupadas = dao.getHorasOcupadasPorMedico(idMedico, fecha)

                if (horasOcupadas.contains(hora)) {
                    // La hora ya no está libre -> avisamos y salimos sin guardar
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@Step4Activity, "Lo sentimos, esta hora acaba de ser reservada", Toast.LENGTH_LONG).show()
                        finish()
                    }
                } else {
                    // Hora libre -> construimos la fila de la tabla "citas".
                    // No ponemos idCita porque es autoGenerate: Room le asigna el número.
                    val nuevaCita = Cita(
                        pacienteCedula = cedulaActiva,  // FK -> usuarios
                        medicoLicencia = idMedico,      // FK -> medicos
                        fecha = fecha,
                        hora = hora,
                        estado = "Activa"               // estado inicial de la cita
                    )

                    dao.addCita(nuevaCita)   // <<<<< AQUÍ SE GUARDA LA CITA EN LA BASE DE DATOS

                    withContext(Dispatchers.Main) {
                        val intent = Intent(this@Step4Activity, Step5Activity::class.java)
                        intent.putExtra("ESPECIALIDAD", especialidad)
                        // ... (el resto de tus putExtra)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }
}
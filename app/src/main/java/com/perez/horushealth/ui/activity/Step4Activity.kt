package com.perez.horushealth.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.perez.horushealth.R
import com.perez.horushealth.data.AppDatabase
import com.perez.horushealth.data.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

        val especialidad = intent.getStringExtra("ESPECIALIDAD_SELECCIONADA") ?: ""
        val idMedico = intent.getStringExtra("MEDICO_ID") ?: ""
        val nombreMedico = intent.getStringExtra("MEDICO_NOMBRE") ?: ""
        val fecha = intent.getStringExtra("FECHA_SELECCIONADA") ?: ""
        val hora = intent.getStringExtra("HORA_SELECCIONADA") ?: ""

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

        val btnConfirmar = findViewById<MaterialButton>(R.id.btnConfirmarCita)
        btnConfirmar.setOnClickListener {
            val intent = Intent(this, Step5Activity::class.java)
            intent.putExtra("ESPECIALIDAD", especialidad)
            intent.putExtra("MEDICO", nombreMedico)
            intent.putExtra("MEDICO_ID", idMedico)
            intent.putExtra("LUGAR", lugarCompleto)
            intent.putExtra("FECHA", fecha)
            intent.putExtra("HORA", hora)
            startActivity(intent)
        }
    }
}
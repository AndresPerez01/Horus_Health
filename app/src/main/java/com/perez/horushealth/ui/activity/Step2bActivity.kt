package com.perez.horushealth.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.perez.horushealth.R
import com.tu_paquete.horushealth.RepositorioMedicos

class Step2bActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.step2b_asignacion_auto)

        val tvEspecialidad = findViewById<TextView>(R.id.tvAutoEspecialidad)
        val tvMedico = findViewById<TextView>(R.id.tvAutoMedico)
        val tvClinica = findViewById<TextView>(R.id.tvAutoClinica)
        val tvConsultorio = findViewById<TextView>(R.id.tvAutoConsultorio)
        val tvFecha = findViewById<TextView>(R.id.tvAutoFecha)
        val tvHora = findViewById<TextView>(R.id.tvAutoHora)

        val especialidad = intent.getStringExtra("ESPECIALIDAD_SELECCIONADA") ?: ""
        val idMedico = intent.getStringExtra("MEDICO_ID") ?: ""
        val fecha = intent.getStringExtra("FECHA_SELECCIONADA") ?: ""
        val hora = intent.getStringExtra("HORA_SELECCIONADA") ?: ""
        var nombreMedico = intent.getStringExtra("MEDICO_NOMBRE") ?: ""
        var lugarCompleto = ""

        val medico = RepositorioMedicos.listaMaestra.find { it.id == idMedico }
        if (medico != null) {
            nombreMedico = medico.nombre
            tvClinica.text = medico.clinica
            tvConsultorio.text = medico.pisoYHabitacion
            lugarCompleto = "${medico.clinica} - ${medico.pisoYHabitacion}"
        }

        tvEspecialidad.text = especialidad
        tvMedico.text = nombreMedico
        tvFecha.text = fecha
        tvHora.text = "$hora h"

        // Botón Cambiar (Descartar y volver al Paso 2)
        val btnCambiar = findViewById<MaterialButton>(R.id.btnDescartarAuto)
        btnCambiar.setOnClickListener {
            finish()
        }

        // Botón Confirmar (Ir directo a la pantalla de Éxito - Step 5)
        val btnConfirmar = findViewById<MaterialButton>(R.id.btnConfirmarAuto)
        btnConfirmar.setOnClickListener {
            val intent = Intent(this, Step5Activity::class.java)
            intent.putExtra("ESPECIALIDAD", especialidad)
            intent.putExtra("MEDICO", nombreMedico)
            intent.putExtra("LUGAR", lugarCompleto)
            intent.putExtra("FECHA", fecha)
            intent.putExtra("HORA", hora)
            startActivity(intent)
        }
    }
}
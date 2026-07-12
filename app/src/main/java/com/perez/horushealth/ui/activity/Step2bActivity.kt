package com.perez.horushealth.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.perez.horushealth.R

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
        val nombreMedico = intent.getStringExtra("MEDICO_NOMBRE") ?: ""

        // Como no queremos hacer otra consulta a la BD solo por texto,
        // recuperamos los extras que pasamos desde el Step2
        val clinica = intent.getStringExtra("MEDICO_CLINICA") ?: ""
        val piso = intent.getStringExtra("MEDICO_PISO") ?: ""
        val lugarCompleto = "$clinica - $piso"

        tvClinica.text = clinica
        tvConsultorio.text = piso
        tvEspecialidad.text = especialidad
        tvMedico.text = nombreMedico
        tvFecha.text = fecha
        tvHora.text = "$hora h"

        val btnCambiar = findViewById<MaterialButton>(R.id.btnDescartarAuto)
        btnCambiar.setOnClickListener {
            finish()
        }

        val btnConfirmar = findViewById<MaterialButton>(R.id.btnConfirmarAuto)
        btnConfirmar.setOnClickListener {
            val intent = Intent(this, Step5Activity::class.java)
            intent.putExtra("ESPECIALIDAD", especialidad)
            intent.putExtra("MEDICO", nombreMedico)
            intent.putExtra("MEDICO_ID", idMedico) // Es crucial pasar el ID de la BD a Step5
            intent.putExtra("LUGAR", lugarCompleto)
            intent.putExtra("FECHA", fecha)
            intent.putExtra("HORA", hora)
            startActivity(intent)
        }
    }
}
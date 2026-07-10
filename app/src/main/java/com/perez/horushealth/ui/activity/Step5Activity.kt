package com.perez.horushealth.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.perez.horushealth.R

class Step5Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.step5_exito)

        // 1. Vincular los TextViews del XML
        val tvEspecialidad = findViewById<TextView>(R.id.tvExitoEspecialidad)
        val tvMedico = findViewById<TextView>(R.id.tvExitoMedico)
        val tvLugar = findViewById<TextView>(R.id.tvExitoLugar)
        val tvFecha = findViewById<TextView>(R.id.tvExitoFecha)
        val tvHora = findViewById<TextView>(R.id.tvExitoHora)

        // 2. Extraer datos pasados por el Intent desde el Paso 4
        val especialidad = intent.getStringExtra("ESPECIALIDAD") ?: ""
        val medico = intent.getStringExtra("MEDICO") ?: ""
        val lugar = intent.getStringExtra("LUGAR") ?: ""
        val fecha = intent.getStringExtra("FECHA") ?: ""
        val hora = intent.getStringExtra("HORA") ?: ""

        // 3. Escribir los datos en pantalla
        tvEspecialidad.text = especialidad
        tvMedico.text = medico
        tvLugar.text = lugar
        tvFecha.text = fecha
        tvHora.text = "$hora h"

        // 4. Configurar el botón de Volver al Inicio
        val btnVolver = findViewById<MaterialButton>(R.id.btnVolverInicio)
        btnVolver.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            // Estas banderas destruyen las pantallas 1, 2, 3, 4 y 5 de la memoria
            // para que al llegar a "agend.xml" sea un inicio fresco.
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
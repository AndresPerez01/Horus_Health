package com.perez.horushealth.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.perez.horushealth.R
import com.perez.horushealth.data.LocalStorage
import com.tu_paquete.horushealth.RepositorioMedicos

class Step4Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.step4_resumen) // Confirma que este sea tu XML correcto

        // 1. Vincular los TextViews del XML
        val tvEspecialidad = findViewById<TextView>(R.id.tvResumenEspecialidad)
        val tvMedico = findViewById<TextView>(R.id.tvResumenMedico)
        val tvClinica = findViewById<TextView>(R.id.tvResumenClinica)
        val tvConsultorio = findViewById<TextView>(R.id.tvResumenConsultorio)
        val tvFecha = findViewById<TextView>(R.id.tvResumenFecha)
        val tvHora = findViewById<TextView>(R.id.tvResumenHora)
        val tvPaciente = findViewById<TextView>(R.id.tvResumenNombrePaciente)

        // 2. Extraer los datos guardados en el Intent
        val especialidad = intent.getStringExtra("ESPECIALIDAD_SELECCIONADA") ?: "No seleccionada"
        val idMedico = intent.getStringExtra("MEDICO_ID") ?: ""
        var nombreMedico = intent.getStringExtra("MEDICO_NOMBRE") ?: "Automático"
        val fecha = intent.getStringExtra("FECHA_SELECCIONADA") ?: "Sin fecha"
        val hora = intent.getStringExtra("HORA_SELECCIONADA") ?: "Sin hora"
        var lugarCompleto = "Asignación en recepción"

        // 3. LA SOLUCIÓN: Buscar al médico por ID, y si falla, buscarlo por Nombre
        var medico = RepositorioMedicos.listaMaestra.find { it.id == idMedico }
        if (medico == null) {
            medico = RepositorioMedicos.listaMaestra.find { it.nombre == nombreMedico }
        }

        // Si lo encontró de alguna de las dos formas, extraemos los datos
        if (medico != null) {
            nombreMedico = medico.nombre
            tvClinica.text = medico.clinica
            tvConsultorio.text = medico.pisoYHabitacion
            lugarCompleto = "${medico.clinica} - ${medico.pisoYHabitacion}"
        }

        // 4. Obtener paciente logueado
        val usuarioLogueado = LocalStorage.getSessionUser(this)

        // 5. SOBREESCRIBIR los datos visuales
        tvEspecialidad.text = especialidad
        tvMedico.text = nombreMedico
        tvFecha.text = fecha
        tvHora.text = "$hora h"

        if (usuarioLogueado != null) {
            tvPaciente.text = usuarioLogueado.name
        }

        // 6. Botones Finales
        val btnEditar = findViewById<MaterialButton>(R.id.btnEditarResumen)
        btnEditar.setOnClickListener {
            finish()
        }

        val btnConfirmar = findViewById<MaterialButton>(R.id.btnConfirmarCita)
        btnConfirmar.setOnClickListener {
            // Empaquetamos los datos precisos para enviarlos al Paso 5 (Éxito)
            val intent = Intent(this, Step5Activity::class.java)
            intent.putExtra("ESPECIALIDAD", especialidad)
            intent.putExtra("MEDICO", nombreMedico)
            intent.putExtra("LUGAR", lugarCompleto) // ¡Ahora sí viajará la clínica correcta!
            intent.putExtra("FECHA", fecha)
            intent.putExtra("HORA", hora)
            startActivity(intent)
        }
    }
}
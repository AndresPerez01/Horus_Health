package com.perez.horushealth.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.perez.horushealth.R
import com.perez.horushealth.utils.GestorHorarios
import com.tu_paquete.horushealth.Medico
import com.tu_paquete.horushealth.RepositorioMedicos
import java.time.LocalDate
import java.time.LocalTime

class Step2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.step2_medic)

        val especialidad = intent.getStringExtra("ESPECIALIDAD_SELECCIONADA") ?: ""

        val cardElegirMedico = findViewById<MaterialCardView>(R.id.cardElegirMedico)
        val cardAsignarAutomaticamente = findViewById<MaterialCardView>(R.id.cardAsignarAutomaticamente)

        // Opción 1: Elegir Manualmente (Va al Paso 2a)
        cardElegirMedico.setOnClickListener {
            val intent = Intent(this, Step2aActivity::class.java)
            intent.putExtra("ESPECIALIDAD_SELECCIONADA", especialidad)
            startActivity(intent)
        }

        // Opción 2: Asignación Automática
        cardAsignarAutomaticamente.setOnClickListener {
            asignarCitaAutomaticamente(especialidad)
        }

        val btnRegresar = findViewById<FloatingActionButton>(R.id.btnRegresarCircular)
        btnRegresar.setOnClickListener {
            finish()
        }
    }

    private fun asignarCitaAutomaticamente(especialidad: String) {
        // 1. Filtramos los médicos por la especialidad
        val medicos = RepositorioMedicos.listaMaestra.filter { it.especialidad == especialidad }

        if (medicos.isEmpty()) {
            Toast.makeText(this, "No hay médicos disponibles para esta especialidad", Toast.LENGTH_SHORT).show()
            return
        }

        var fechaAsignada: LocalDate? = null
        var horaAsignada: LocalTime? = null
        var medicoAsignado: Medico? = null

        // 2. Buscamos el turno más cercano desde HOY hasta en los próximos 7 días
        var fechaActualBusqueda = LocalDate.now()

        for (i in 0..7) {
            var horaMasTempranaDelDia: LocalTime? = null
            var mejorMedicoDelDia: Medico? = null

            for (medico in medicos) {
                // Obtenemos los horarios usando nuestra regla de 2 horas
                val horariosDisponibles = GestorHorarios.obtenerHorariosDisponibles(medico, fechaActualBusqueda)

                if (horariosDisponibles.isNotEmpty()) {
                    val primerTurno = horariosDisponibles.first() // El primer turno es el más temprano

                    // Comparamos para ver si es el más temprano de todos los médicos en este día
                    if (horaMasTempranaDelDia == null || primerTurno.isBefore(horaMasTempranaDelDia)) {
                        horaMasTempranaDelDia = primerTurno
                        mejorMedicoDelDia = medico
                    }
                }
            }

            // Si encontramos un turno en este día, detenemos la búsqueda (es el más cercano absoluto)
            if (horaMasTempranaDelDia != null && mejorMedicoDelDia != null) {
                fechaAsignada = fechaActualBusqueda
                horaAsignada = horaMasTempranaDelDia
                medicoAsignado = mejorMedicoDelDia
                break
            }

            // Si no hay turnos hoy, buscamos mañana
            fechaActualBusqueda = fechaActualBusqueda.plusDays(1)
        }

        // 3. Enviamos los resultados a la nueva pantalla de confirmación automática
        if (medicoAsignado != null && fechaAsignada != null && horaAsignada != null) {
            val intent = Intent(this, Step2bActivity::class.java)
            intent.putExtra("ESPECIALIDAD_SELECCIONADA", especialidad)
            intent.putExtra("MEDICO_ID", medicoAsignado.id)
            intent.putExtra("MEDICO_NOMBRE", medicoAsignado.nombre)
            intent.putExtra("FECHA_SELECCIONADA", fechaAsignada.toString())
            intent.putExtra("HORA_SELECCIONADA", horaAsignada.toString())
            startActivity(intent)
        } else {
            Toast.makeText(this, "Lo sentimos, no hay turnos disponibles esta semana.", Toast.LENGTH_LONG).show()
        }
    }
}
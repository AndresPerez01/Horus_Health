package com.perez.horushealth.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.perez.horushealth.R
import com.perez.horushealth.data.AppDatabase
import com.perez.horushealth.data.MedicoEntity
import com.perez.horushealth.utils.GestorHorarios
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime

/*
 * ============================================================================
 *  PASO 2: ¿CÓMO QUIERES CONTINUAR?   (layout: step2_medic.xml)
 * ============================================================================
 *  Aquí el flujo se BIFURCA en dos caminos:
 *    A) "Elegir mi médico"        -> Step2a (lista) -> Step3 (fecha) -> Step4 -> Step5
 *    B) "Asignar automáticamente" -> Step2b (propuesta) -------------------> Step5
 * ============================================================================
 */
class Step2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.step2_medic)

        // Recibimos la especialidad que se eligió en el Paso 1
        val especialidad = intent.getStringExtra("ESPECIALIDAD_SELECCIONADA") ?: ""

        val cardElegirMedico = findViewById<MaterialCardView>(R.id.cardElegirMedico)
        val cardAsignarAutomaticamente = findViewById<MaterialCardView>(R.id.cardAsignarAutomaticamente)

        cardElegirMedico.setOnClickListener {
            val intent = Intent(this, Step2aActivity::class.java)
            intent.putExtra("ESPECIALIDAD_SELECCIONADA", especialidad)
            startActivity(intent)
        }

        cardAsignarAutomaticamente.setOnClickListener {
            asignarCitaAutomaticamente(especialidad)
        }

        val btnRegresar = findViewById<FloatingActionButton>(R.id.btnRegresarCircular)
        btnRegresar.setOnClickListener {
            finish()
        }
    }

    /**
     * ================= ALGORITMO DE ASIGNACIÓN AUTOMÁTICA =================
     * Objetivo: encontrar LA CITA MÁS PRÓXIMA POSIBLE de esa especialidad.
     *
     * Cómo funciona (dos bucles anidados):
     *   BUCLE EXTERNO -> avanza día por día, desde hoy, hasta 7 días adelante.
     *   BUCLE INTERNO -> por cada día, revisa TODOS los médicos de la especialidad
     *                    y se queda con el que tenga el turno MÁS TEMPRANO.
     *   En cuanto encuentra un día con hueco, corta con "break" (ya es el más próximo).
     */
    private fun asignarCitaAutomaticamente(especialidad: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val dao = AppDatabase.getBaseDatos(this@Step2Activity).horusDao()
            // Traemos de Room todos los médicos de esa especialidad
            val medicos = dao.getMedicosPorEspecialidad(especialidad)

            withContext(Dispatchers.Main) {
                if (medicos.isEmpty()) {
                    Toast.makeText(this@Step2Activity, "No hay médicos disponibles para esta especialidad", Toast.LENGTH_SHORT).show()
                    return@withContext
                }

                var fechaAsignada: LocalDate? = null
                var horaAsignada: LocalTime? = null
                var medicoAsignado: MedicoEntity? = null // Usamos MedicoEntity

                var fechaActualBusqueda = LocalDate.now()   // Empezamos a buscar desde HOY

                // BUCLE EXTERNO: día por día (hoy + 7 días como máximo)
                for (i in 0..7) {
                    var horaMasTempranaDelDia: LocalTime? = null
                    var mejorMedicoDelDia: MedicoEntity? = null

                    // BUCLE INTERNO: comparamos todos los médicos de ese día
                    for (medico in medicos) {
                        // GestorHorarios aplica las reglas (jornada + 2h de anticipación)
                        val horariosDisponibles = GestorHorarios.obtenerHorariosDisponibles(medico, fechaActualBusqueda)

                        if (horariosDisponibles.isNotEmpty()) {
                            val primerTurno = horariosDisponibles.first()  // su hueco más temprano

                            // ¿Es el más temprano encontrado hasta ahora? Entonces nos lo guardamos.
                            if (horaMasTempranaDelDia == null || primerTurno.isBefore(horaMasTempranaDelDia)) {
                                horaMasTempranaDelDia = primerTurno
                                mejorMedicoDelDia = medico
                            }
                        }
                    }

                    // Si este día tuvo algún hueco, ya es el más próximo posible -> paramos
                    if (horaMasTempranaDelDia != null && mejorMedicoDelDia != null) {
                        fechaAsignada = fechaActualBusqueda
                        horaAsignada = horaMasTempranaDelDia
                        medicoAsignado = mejorMedicoDelDia
                        break   // <- corta el bucle externo
                    }

                    // Ese día estaba lleno: probamos con el día siguiente
                    fechaActualBusqueda = fechaActualBusqueda.plusDays(1)
                }

                if (medicoAsignado != null && fechaAsignada != null && horaAsignada != null) {
                    val intent = Intent(this@Step2Activity, Step2bActivity::class.java)
                    intent.putExtra("ESPECIALIDAD_SELECCIONADA", especialidad)
                    intent.putExtra("MEDICO_ID", medicoAsignado.licencia) // Pasamos la licencia
                    intent.putExtra("MEDICO_NOMBRE", medicoAsignado.nombre)
                    intent.putExtra("MEDICO_CLINICA", medicoAsignado.clinica) // Pasamos datos extras
                    intent.putExtra("MEDICO_PISO", medicoAsignado.pisoYHabitacion)
                    intent.putExtra("FECHA_SELECCIONADA", fechaAsignada.toString())
                    intent.putExtra("HORA_SELECCIONADA", horaAsignada.toString())
                    startActivity(intent)
                } else {
                    Toast.makeText(this@Step2Activity, "Lo sentimos, no hay turnos disponibles esta semana.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
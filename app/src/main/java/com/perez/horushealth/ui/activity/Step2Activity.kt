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

class Step2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.step2_medic)

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

    private fun asignarCitaAutomaticamente(especialidad: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val dao = AppDatabase.getBaseDatos(this@Step2Activity).horusDao()
            // Buscamos los médicos en la base de datos
            val medicos = dao.getMedicosPorEspecialidad(especialidad)

            withContext(Dispatchers.Main) {
                if (medicos.isEmpty()) {
                    Toast.makeText(this@Step2Activity, "No hay médicos disponibles para esta especialidad", Toast.LENGTH_SHORT).show()
                    return@withContext
                }

                var fechaAsignada: LocalDate? = null
                var horaAsignada: LocalTime? = null
                var medicoAsignado: MedicoEntity? = null // Usamos MedicoEntity

                var fechaActualBusqueda = LocalDate.now()

                for (i in 0..7) {
                    var horaMasTempranaDelDia: LocalTime? = null
                    var mejorMedicoDelDia: MedicoEntity? = null

                    for (medico in medicos) {
                        val horariosDisponibles = GestorHorarios.obtenerHorariosDisponibles(medico, fechaActualBusqueda)

                        if (horariosDisponibles.isNotEmpty()) {
                            val primerTurno = horariosDisponibles.first()

                            if (horaMasTempranaDelDia == null || primerTurno.isBefore(horaMasTempranaDelDia)) {
                                horaMasTempranaDelDia = primerTurno
                                mejorMedicoDelDia = medico
                            }
                        }
                    }

                    if (horaMasTempranaDelDia != null && mejorMedicoDelDia != null) {
                        fechaAsignada = fechaActualBusqueda
                        horaAsignada = horaMasTempranaDelDia
                        medicoAsignado = mejorMedicoDelDia
                        break
                    }

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
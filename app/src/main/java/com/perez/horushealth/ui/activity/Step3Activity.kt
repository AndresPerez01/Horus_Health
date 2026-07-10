package com.perez.horushealth.ui.activity

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.perez.horushealth.R
import com.perez.horushealth.ui.adapter.HoraAdapter
import com.tu_paquete.horushealth.RepositorioMedicos
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Calendar

class Step3Activity : AppCompatActivity() {

    private var fechaSeleccionada: LocalDate? = null
    private var horaSeleccionada: LocalTime? = null

    private var especialidadElegida: String = ""
    private var idMedicoElegido: String = ""
    private var nombreMedico: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.step3_fecha_hora)

        // 1. Recibimos datos
        especialidadElegida = intent.getStringExtra("ESPECIALIDAD_SELECCIONADA") ?: ""
        idMedicoElegido = intent.getStringExtra("MEDICO_ID") ?: ""
        nombreMedico = intent.getStringExtra("MEDICO_NOMBRE") ?: ""

        val cardFecha = findViewById<MaterialCardView>(R.id.cardSeleccionarFecha)
        val tvFechaMostrada = findViewById<TextView>(R.id.tvFechaMostrada)

        // Configuramos la vista en cuadrícula (3 columnas) para los horarios
        val rvManana = findViewById<RecyclerView>(R.id.rvHorariosManana)
        val rvTarde = findViewById<RecyclerView>(R.id.rvHorariosTarde)
        rvManana.layoutManager = GridLayoutManager(this, 3)
        rvTarde.layoutManager = GridLayoutManager(this, 3)

        // 2. Abrir el DatePickerDialog al tocar la tarjeta
        cardFecha.setOnClickListener {
            val calendario = Calendar.getInstance()
            val year = calendario.get(Calendar.YEAR)
            val month = calendario.get(Calendar.MONTH)
            val day = calendario.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, añoSeleccionado, mesSeleccionado, diaSeleccionado ->
                // Guardamos la fecha elegida
                fechaSeleccionada = LocalDate.of(añoSeleccionado, mesSeleccionado + 1, diaSeleccionado)
                tvFechaMostrada.text = "$diaSeleccionado/${mesSeleccionado + 1}/$añoSeleccionado"

                // Reseteamos la hora seleccionada porque cambió de día
                horaSeleccionada = null

                // Calculamos y mostramos los horarios
                calcularHorariosDisponibles()

            }, year, month, day)

            datePickerDialog.datePicker.minDate = System.currentTimeMillis()
            datePickerDialog.show()
        }

        // Botón Regresar
        val btnRegresar = findViewById<FloatingActionButton>(R.id.btnRegresarCircular)
        btnRegresar.setOnClickListener {
            finish()
        }

        // Botón Continuar
        val btnContinuar = findViewById<MaterialButton>(R.id.btnContinuarPaso3)
        btnContinuar.setOnClickListener {
            if (fechaSeleccionada == null || horaSeleccionada == null) {
                Toast.makeText(this, "Selecciona una fecha y hora", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Enviamos todo al Paso 4
            val intent = Intent(this, Step4Activity::class.java)
            intent.putExtra("ESPECIALIDAD_SELECCIONADA", especialidadElegida)
            intent.putExtra("MEDICO_NOMBRE", nombreMedico)
            intent.putExtra("FECHA_SELECCIONADA", fechaSeleccionada.toString())
            intent.putExtra("HORA_SELECCIONADA", horaSeleccionada.toString())
            startActivity(intent)
        }
    }

    private fun calcularHorariosDisponibles() {
        if (fechaSeleccionada == null) return

        // Buscamos los datos del médico
        val medico = RepositorioMedicos.listaMaestra.find { it.id == idMedicoElegido } ?: return

        val turnosDisponibles = mutableListOf<LocalTime>()
        val ahora = LocalDateTime.now()
        val limiteHoraMinima = ahora.plusHours(2)

        val citasOcupadasBaseDatos = listOf(LocalTime.of(9, 0), LocalTime.of(14, 0))

        for (hora in medico.horaInicio until medico.horaFin) {
            val horaDelTurno = LocalTime.of(hora, 0)
            val fechaHoraDelTurno = LocalDateTime.of(fechaSeleccionada, horaDelTurno)

            if (citasOcupadasBaseDatos.contains(horaDelTurno)) {
                continue
            }

            if (fechaSeleccionada == LocalDate.now()) {
                if (fechaHoraDelTurno.isAfter(limiteHoraMinima)) {
                    turnosDisponibles.add(horaDelTurno)
                }
            } else if (fechaSeleccionada!!.isAfter(LocalDate.now())) {
                turnosDisponibles.add(horaDelTurno)
            }
        }

        // Dividimos en Mañana y Tarde
        val horariosManana = turnosDisponibles.filter { it.hour < 12 }
        val horariosTarde = turnosDisponibles.filter { it.hour >= 12 }

        val rvManana = findViewById<RecyclerView>(R.id.rvHorariosManana)
        val rvTarde = findViewById<RecyclerView>(R.id.rvHorariosTarde)

        // Asignamos el adaptador. Cuando el usuario hace clic, se guarda en 'horaSeleccionada'
        rvManana.adapter = HoraAdapter(horariosManana) { horaElegida ->
            horaSeleccionada = horaElegida
            // Limpiamos la selección de la tarde si eligió algo en la mañana
            rvTarde.adapter?.notifyDataSetChanged()
        }

        rvTarde.adapter = HoraAdapter(horariosTarde) { horaElegida ->
            horaSeleccionada = horaElegida
            // Limpiamos la selección de la mañana si eligió algo en la tarde
            rvManana.adapter?.notifyDataSetChanged()
        }
    }
}
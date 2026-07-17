package com.perez.horushealth.ui.activity

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.perez.horushealth.R
import com.perez.horushealth.data.AppDatabase
import com.perez.horushealth.data.MedicoEntity
import com.perez.horushealth.ui.adapter.HoraAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Calendar

/*
 * ============================================================================
 *  PASO 3: ELEGIR FECHA Y HORA   (layout: step3_fecha_hora.xml)
 * ============================================================================
 *  Solo se llega aquí por el CAMINO MANUAL (después de elegir médico en Step2a).
 *
 *  Es la pantalla con más lógica: calcula qué horas mostrar cruzando
 *    (turnos posibles del médico)  -  (horas ya ocupadas en la base de datos)
 *  y las separa en Mañana / Tarde en dos RecyclerView de rejilla.
 * ============================================================================
 */
class Step3Activity : AppCompatActivity() {

    // Variables de estado: guardan lo que el usuario va eligiendo.
    // Son "?" (nullable) porque al abrir la pantalla todavía no ha elegido nada.
    private var fechaSeleccionada: LocalDate? = null
    private var horaSeleccionada: LocalTime? = null

    // Datos que vienen arrastrándose de los pasos anteriores
    private var especialidadElegida: String = ""
    private var idMedicoElegido: String = ""
    private var nombreMedico: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.step3_fecha_hora)

        especialidadElegida = intent.getStringExtra("ESPECIALIDAD_SELECCIONADA") ?: ""
        idMedicoElegido = intent.getStringExtra("MEDICO_ID") ?: ""
        nombreMedico = intent.getStringExtra("MEDICO_NOMBRE") ?: ""

        val cardFecha = findViewById<MaterialCardView>(R.id.cardSeleccionarFecha)
        val tvFechaMostrada = findViewById<TextView>(R.id.tvFechaMostrada)

        val rvManana = findViewById<RecyclerView>(R.id.rvHorariosManana)
        val rvTarde = findViewById<RecyclerView>(R.id.rvHorariosTarde)
        // GridLayoutManager(this, 3) = las horas se muestran en REJILLA de 3 columnas
        // (a diferencia del historial, que usa LinearLayoutManager = lista vertical)
        rvManana.layoutManager = GridLayoutManager(this, 3)
        rvTarde.layoutManager = GridLayoutManager(this, 3)

        cardFecha.setOnClickListener {
            val calendario = Calendar.getInstance()
            val year = calendario.get(Calendar.YEAR)
            val month = calendario.get(Calendar.MONTH)
            val day = calendario.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, añoSeleccionado, mesSeleccionado, diaSeleccionado ->
                fechaSeleccionada = LocalDate.of(añoSeleccionado, mesSeleccionado + 1, diaSeleccionado)
                tvFechaMostrada.text = "$diaSeleccionado/${mesSeleccionado + 1}/$añoSeleccionado"
                horaSeleccionada = null

                calcularHorariosDisponibles()
            }, year, month, day)

            // minDate = hoy: el calendario NO deja seleccionar días pasados
            datePickerDialog.datePicker.minDate = System.currentTimeMillis()
            datePickerDialog.show()
        }

        val btnRegresar = findViewById<FloatingActionButton>(R.id.btnRegresarCircular)
        btnRegresar.setOnClickListener { finish() }

        val btnContinuar = findViewById<MaterialButton>(R.id.btnContinuarPaso3)
        btnContinuar.setOnClickListener {
            if (fechaSeleccionada == null || horaSeleccionada == null) {
                Toast.makeText(this, "Selecciona una fecha y hora", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, Step4Activity::class.java)
            intent.putExtra("ESPECIALIDAD_SELECCIONADA", especialidadElegida)
            intent.putExtra("MEDICO_ID", idMedicoElegido) // Crucial mantener el ID viajando
            intent.putExtra("MEDICO_NOMBRE", nombreMedico)
            intent.putExtra("FECHA_SELECCIONADA", fechaSeleccionada.toString())
            intent.putExtra("HORA_SELECCIONADA", horaSeleccionada.toString())
            startActivity(intent)
        }
    }

    /**
     * ============ EL CÁLCULO DE HORARIOS (la función clave de esta pantalla) ============
     * Se llama cada vez que el usuario elige una fecha nueva.
     *
     * Pasos:
     *   1. Busca al médico en Room.
     *   2. Pide a la BD las horas YA OCUPADAS de ese médico ese día.
     *   3. Genera los turnos de la jornada (horaInicio..horaFin).
     *   4. Descarta los ocupados y los que no cumplen las 2h de anticipación.
     *   5. Los separa en mañana (<12h) y tarde (>=12h) y los pinta.
     */
    private fun calcularHorariosDisponibles() {
        if (fechaSeleccionada == null) return   // Sin fecha no hay nada que calcular

        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getBaseDatos(this@Step3Activity)
            val medicos = db.horusDao().getMedicosPorEspecialidad(especialidadElegida)
            val medico = medicos.find { it.licencia == idMedicoElegido } ?: return@launch

            // 1. Convertimos la fecha que escogió el usuario a Texto (String) para Room
            val fechaTexto = fechaSeleccionada.toString()

            // 2. Le pedimos a Room la lista real de horas ocupadas (nos devuelve Strings como "09:00")
            val horasOcupadasTextos = db.horusDao().getHorasOcupadasPorMedico(idMedicoElegido, fechaTexto)

            // 3. Convertimos esos textos a objetos LocalTime para poder compararlos fácilmente
            val citasOcupadasReales = horasOcupadasTextos.map { LocalTime.parse(it) }

            val turnosDisponibles = mutableListOf<LocalTime>()
            val ahora = LocalDateTime.now()
            val limiteHoraMinima = ahora.plusHours(2)

            for (hora in medico.horaInicio until medico.horaFin) {
                val horaDelTurno = LocalTime.of(hora, 0)
                val fechaHoraDelTurno = LocalDateTime.of(fechaSeleccionada, horaDelTurno)

                // Si la hora generada coincide con una hora real de la BD, la saltamos
                if (citasOcupadasReales.contains(horaDelTurno)) {
                    continue
                }

                if (fechaSeleccionada == LocalDate.now()) {
                    if (fechaHoraDelTurno.isAfter(limiteHoraMinima)) turnosDisponibles.add(horaDelTurno)
                } else if (fechaSeleccionada!!.isAfter(LocalDate.now())) {
                    turnosDisponibles.add(horaDelTurno)
                }
            }

            // filter = nos quedamos solo con los que cumplen la condición.
            // "it" es cada hora de la lista.
            val horariosManana = turnosDisponibles.filter { it.hour < 12 }
            val horariosTarde = turnosDisponibles.filter { it.hour >= 12 }

            withContext(Dispatchers.Main) {
                val rvManana = findViewById<RecyclerView>(R.id.rvHorariosManana)
                val rvTarde = findViewById<RecyclerView>(R.id.rvHorariosTarde)

                rvManana.adapter = HoraAdapter(horariosManana) { horaElegida ->
                    horaSeleccionada = horaElegida
                    rvTarde.adapter?.notifyDataSetChanged()
                }

                rvTarde.adapter = HoraAdapter(horariosTarde) { horaElegida ->
                    horaSeleccionada = horaElegida
                    rvManana.adapter?.notifyDataSetChanged()
                }
            }
        }
    }
}
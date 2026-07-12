package com.perez.horushealth.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.perez.horushealth.R
import com.perez.horushealth.data.AppDatabase
import com.perez.horushealth.data.Cita
import com.perez.horushealth.data.AppDatabase.CitaConMedico
import com.perez.horushealth.data.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistorialActivity : AppCompatActivity() {

    private lateinit var rvProximas: RecyclerView
    private lateinit var rvPasadas: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.historial)

        rvProximas = findViewById(R.id.rvProximas)
        rvPasadas = findViewById(R.id.rvPasadas)

        rvProximas.layoutManager = LinearLayoutManager(this)
        rvPasadas.layoutManager = LinearLayoutManager(this)

        findViewById<MaterialCardView>(R.id.btnVolverAtras).setOnClickListener { finish() }
        findViewById<MaterialButton>(R.id.btnVolverInicio).setOnClickListener { finish() }

        cargarCitas()
    }

    private fun cargarCitas() {
        val cedulaUsuario = SessionManager.getSession(this) ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            val dao = AppDatabase.getBaseDatos(this@HistorialActivity).horusDao()
            val todasLasCitas = dao.getCitasDePaciente(cedulaUsuario)

            val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val hoyString = formatoFecha.format(Date())
            val hoyPuro = formatoFecha.parse(hoyString)

            val proximas = mutableListOf<CitaConMedico>()
            val pasadas = mutableListOf<CitaConMedico>()

            for (item in todasLasCitas) {
                val estadoActual = item.cita.estado ?: ""

                // 1. Si la cita ya fue cancelada o finalizada, va directo al historial inferior.
                if (estadoActual.equals("Cancelada", ignoreCase = true) ||
                    estadoActual.equals("Realizada", ignoreCase = true)) {
                    pasadas.add(item)
                    continue
                }

                // 2. Si el estado es otro (Pendiente, Activa, etc.), verificamos la fecha.
                try {
                    val fechaCita = formatoFecha.parse(item.cita.fecha)
                    // Si la fecha existe y es estrictamente anterior a hoy, la movemos a pasadas.
                    if (fechaCita != null && fechaCita.before(hoyPuro)) {
                        pasadas.add(item)
                    } else {
                        // Es hoy o en el futuro.
                        proximas.add(item)
                    }
                } catch (e: Exception) {
                    // 🔥 EL SALVAVIDAS: Si el formato de fecha no es exacto y da error,
                    // asumimos que es una cita nueva/próxima en lugar de mandarla al historial por accidente.
                    proximas.add(item)
                }
            }

            withContext(Dispatchers.Main) {
                rvProximas.adapter = CitasAdapter(proximas, true) { cita ->
                    cancelarCitaBD(cita)
                }

                rvPasadas.adapter = CitasAdapter(pasadas, false) { }
            }
        }
    }

    private fun cancelarCitaBD(cita: Cita) {
        lifecycleScope.launch(Dispatchers.IO) {
            val dao = AppDatabase.getBaseDatos(this@HistorialActivity).horusDao()
            val citaActualizada = cita.copy(estado = "Cancelada")
            dao.actualizarCita(citaActualizada)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@HistorialActivity, "Cita cancelada correctamente", Toast.LENGTH_SHORT).show()
                cargarCitas()
            }
        }
    }
}

// --- ADAPTADOR DE RECYCLERVIEW ---
class CitasAdapter(
    private val listaCitas: List<CitaConMedico>,
    private val esListaProxima: Boolean,
    private val onCancelarClick: (Cita) -> Unit
) : RecyclerView.Adapter<CitasAdapter.CitaViewHolder>() {

    private var expandedPosition = -1

    class CitaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardContenedor: MaterialCardView = view.findViewById(R.id.cardContenedor)
        val franjaEstado: View = view.findViewById(R.id.franjaEstado)
        val tvEspecialidad: TextView = view.findViewById(R.id.tvEspecialidad)
        val tvEstado: TextView = view.findViewById(R.id.tvEstado)
        val cardBadge: MaterialCardView = view.findViewById(R.id.cardBadge)
        val tvMedico: TextView = view.findViewById(R.id.tvMedico)
        val tvFechaHora: TextView = view.findViewById(R.id.tvFechaHora)
        val layoutDetalles: LinearLayout = view.findViewById(R.id.layoutDetalles)
        val tvClinica: TextView = view.findViewById(R.id.tvClinica)
        val tvHabitacion: TextView = view.findViewById(R.id.tvHabitacion)
        val btnCancelarCita: MaterialButton = view.findViewById(R.id.btnCancelarCita)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cita, parent, false)
        return CitaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        val item = listaCitas[position]
        val cita = item.cita
        val medico = item.medico

        // 1. Llenamos los textos básicos
        val prefix = if (medico.genero.lowercase() == "femenino") "Dra." else "Dr."
        holder.tvEspecialidad.text = medico.especialidad
        holder.tvMedico.text = "👩‍⚕️ $prefix ${medico.nombre}"
        holder.tvFechaHora.text = "📅 ${cita.fecha} · ${cita.hora}"
        holder.tvClinica.text = "📍 ${medico.clinica} - ${medico.direccion}"
        holder.tvHabitacion.text = "🚪 ${medico.pisoYHabitacion}"

        // 2. Lógica visual a prueba de balas
        val estadoStr = cita.estado?.lowercase() ?: ""

        if (estadoStr == "cancelada") {
            // Diseño de Cita Cancelada (Rojo)
            holder.tvEstado.text = "Cancelada"
            holder.tvEstado.setTextColor(Color.parseColor("#D32F2F"))
            holder.cardBadge.setCardBackgroundColor(Color.parseColor("#FFEBEE"))
            holder.franjaEstado.setBackgroundColor(Color.parseColor("#D32F2F"))
            holder.btnCancelarCita.visibility = View.GONE

        } else if (esListaProxima) {
            // Diseño de Cita Próxima (Azul) - ¡Y AQUÍ FORZAMOS EL BOTÓN VISIBLE!
            holder.tvEstado.text = "Próxima"
            holder.tvEstado.setTextColor(Color.parseColor("#3266E3"))
            holder.cardBadge.setCardBackgroundColor(Color.parseColor("#F0F4FF"))
            holder.franjaEstado.setBackgroundColor(Color.parseColor("#3266E3"))
            holder.btnCancelarCita.visibility = View.VISIBLE

        } else {
            // Diseño de Cita Realizada/Pasada (Gris)
            holder.tvEstado.text = cita.estado ?: "Realizada"
            holder.tvEstado.setTextColor(Color.parseColor("#9CA3AF"))
            holder.cardBadge.setCardBackgroundColor(Color.parseColor("#F4F6F9"))
            holder.franjaEstado.setBackgroundColor(Color.parseColor("#9CA3AF"))
            holder.btnCancelarCita.visibility = View.GONE
        }

        // 3. Manejo de la expansión del menú (Acordeón)
        val isExpanded = position == expandedPosition
        holder.layoutDetalles.visibility = if (isExpanded) View.VISIBLE else View.GONE

        holder.cardContenedor.setOnClickListener {
            val previousExpandedPosition = expandedPosition
            expandedPosition = if (isExpanded) -1 else position
            notifyItemChanged(previousExpandedPosition)
            notifyItemChanged(expandedPosition)
        }

        // 4. Acción del botón cancelar
        holder.btnCancelarCita.setOnClickListener {
            expandedPosition = -1 // Cierra el menú visualmente
            onCancelarClick(cita) // Llama a la base de datos
        }
    }

    override fun getItemCount(): Int = listaCitas.size
}
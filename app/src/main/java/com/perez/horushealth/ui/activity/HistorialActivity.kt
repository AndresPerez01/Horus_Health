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

/*
 * ============================================================================
 *  PANTALLA "MIS CITAS" / HISTORIAL   (layout: historial.xml)
 * ============================================================================
 *  Muestra DOS listas (dos RecyclerView):
 *    - PRÓXIMAS: citas activas de hoy en adelante (se pueden cancelar).
 *    - HISTORIAL: citas canceladas, realizadas o ya pasadas.
 *
 *  Cada fila de la lista se dibuja con el layout item_cita.xml.
 * ============================================================================
 */
class HistorialActivity : AppCompatActivity() {

    // lateinit = "prometo darle valor antes de usarla" (se inicializan en onCreate)
    private lateinit var rvProximas: RecyclerView
    private lateinit var rvPasadas: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.historial)

        rvProximas = findViewById(R.id.rvProximas)
        rvPasadas = findViewById(R.id.rvPasadas)

        // LayoutManager: le dice al RecyclerView CÓMO colocar los ítems.
        // LinearLayoutManager = uno debajo de otro (lista vertical).
        rvProximas.layoutManager = LinearLayoutManager(this)
        rvPasadas.layoutManager = LinearLayoutManager(this)

        findViewById<MaterialCardView>(R.id.btnVolverAtras).setOnClickListener { finish() }
        findViewById<MaterialButton>(R.id.btnVolverInicio).setOnClickListener { finish() }

        cargarCitas()
    }

    /**
     * Carga las citas del usuario y las REPARTE en las dos listas.
     * Se llama al abrir la pantalla y otra vez después de cancelar (para refrescar).
     */
    private fun cargarCitas() {
        // "?: return" = si no hay sesión, salimos del método sin hacer nada
        val cedulaUsuario = SessionManager.getSession(this) ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            val dao = AppDatabase.getBaseDatos(this@HistorialActivity).horusDao()
            // getCitasDePaciente devuelve CitaConMedico: la cita YA VIENE con su médico (@Relation)
            val todasLasCitas = dao.getCitasDePaciente(cedulaUsuario)

            val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val hoyString = formatoFecha.format(Date())
            val hoyPuro = formatoFecha.parse(hoyString)

            val proximas = mutableListOf<CitaConMedico>()
            val pasadas = mutableListOf<CitaConMedico>()

            // CLASIFICADOR: recorremos cada cita y decidimos en qué lista va
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
                    //
                    // DATO IMPORTANTE PARA LA DEFENSA: las fechas se guardan en formato ISO
                    // ("2026-07-16") pero aquí se intentan leer como "dd/MM/yyyy", así que este
                    // parse SIEMPRE falla y todas las citas acaban en "próximas".
                    // Por eso el HISTORIAL se ve vacío. Lo correcto sería unificar el formato.
                    proximas.add(item)
                }
            }

            withContext(Dispatchers.Main) {
                // Le entregamos a cada RecyclerView su adaptador con su lista.
                // El "true" indica que es la lista de próximas (muestra el botón Cancelar).
                // El bloque { cita -> ... } es un LAMBDA: la acción que se ejecuta al cancelar.
                rvProximas.adapter = CitasAdapter(proximas, true) { cita ->
                    cancelarCitaBD(cita)
                }

                // "false" = lista de pasadas: sin botón cancelar, por eso el lambda va vacío.
                rvPasadas.adapter = CitasAdapter(pasadas, false) { }
            }
        }
    }

    /**
     * CANCELAR UNA CITA.
     * OJO: no se BORRA de la base de datos (no hay DELETE).
     * Se hace un UPDATE cambiando el estado a "Cancelada", así queda el registro histórico
     * y además su hora vuelve a quedar libre (ver el SQL de getHorasOcupadasPorMedico).
     */
    private fun cancelarCitaBD(cita: Cita) {
        lifecycleScope.launch(Dispatchers.IO) {
            val dao = AppDatabase.getBaseDatos(this@HistorialActivity).horusDao()
            // copy() = crea una copia del objeto cambiando solo ese campo (las data class son inmutables)
            val citaActualizada = cita.copy(estado = "Cancelada")
            dao.actualizarCita(citaActualizada)   // @Update: busca por idCita y reemplaza

            withContext(Dispatchers.Main) {
                Toast.makeText(this@HistorialActivity, "Cita cancelada correctamente", Toast.LENGTH_SHORT).show()
                cargarCitas()   // Recargamos para que la cita salte de "Próximas" a "Historial"
            }
        }
    }
}

/*
 * ============================================================================
 *  ADAPTADOR DEL RECYCLERVIEW  (¿qué es un Adapter?)
 * ============================================================================
 *  Un RecyclerView es una lista eficiente: solo crea en memoria las filas que
 *  caben en pantalla y las RECICLA al hacer scroll (de ahí su nombre).
 *
 *  El Adapter es el "traductor": coge la lista de datos (List<CitaConMedico>)
 *  y la convierte en vistas (item_cita.xml).
 *
 *  Todo Adapter obliga a implementar 3 métodos:
 *    1. onCreateViewHolder -> CREA una fila vacía (infla el XML).
 *    2. onBindViewHolder   -> RELLENA esa fila con los datos de la posición N.
 *    3. getItemCount       -> dice CUÁNTAS filas hay.
 *
 *  Parámetros del constructor:
 *    - listaCitas      : los datos a mostrar.
 *    - esListaProxima  : true = lista de próximas (con botón cancelar) / false = historial.
 *    - onCancelarClick : lambda que el Adapter "dispara" hacia la Activity al cancelar.
 * ============================================================================
 */
class CitasAdapter(
    private val listaCitas: List<CitaConMedico>,
    private val esListaProxima: Boolean,
    private val onCancelarClick: (Cita) -> Unit
) : RecyclerView.Adapter<CitasAdapter.CitaViewHolder>() {

    // Guarda qué fila está desplegada (efecto acordeón). -1 = ninguna abierta.
    private var expandedPosition = -1

    /**
     * ViewHolder = una "ficha" que guarda las referencias a los elementos de UNA fila.
     * Sirve para no repetir findViewById en cada scroll (eso haría la lista lenta).
     */
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

    // 1) CREA la fila: "infla" (convierte en objetos) el XML item_cita.xml
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cita, parent, false)
        return CitaViewHolder(view)
    }

    // 2) RELLENA la fila con los datos de la cita nº "position"
    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        val item = listaCitas[position]
        val cita = item.cita       // datos de la tabla citas
        val medico = item.medico   // datos de la tabla medicos (vienen unidos por @Relation)

        // 1. Llenamos los textos básicos
        // Elegimos "Dr." o "Dra." según el género guardado en la tabla medicos
        val prefix = if (medico.genero.lowercase() == "femenino") "Dra." else "Dr."
        holder.tvEspecialidad.text = medico.especialidad
        holder.tvMedico.text = "👩‍⚕️ $prefix ${medico.nombre}"
        holder.tvFechaHora.text = "📅 ${cita.fecha} · ${cita.hora}"
        holder.tvClinica.text = "📍 ${medico.clinica} - ${medico.direccion}"
        holder.tvHabitacion.text = "🚪 ${medico.pisoYHabitacion}"

        // 2. Lógica visual: el COLOR y el botón dependen del estado de la cita
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

        // 3. ACORDEÓN: al tocar la tarjeta se despliegan/ocultan los detalles (clínica, consultorio).
        //    VISIBLE = se ve / GONE = se oculta y NO ocupa espacio.
        val isExpanded = position == expandedPosition
        holder.layoutDetalles.visibility = if (isExpanded) View.VISIBLE else View.GONE

        holder.cardContenedor.setOnClickListener {
            val previousExpandedPosition = expandedPosition
            // Si ya estaba abierta la cerramos (-1); si no, abrimos esta.
            expandedPosition = if (isExpanded) -1 else position
            // notifyItemChanged obliga al RecyclerView a redibujar esas dos filas
            notifyItemChanged(previousExpandedPosition)  // cierra la anterior
            notifyItemChanged(expandedPosition)          // abre la nueva
        }

        // 4. Botón cancelar: el Adapter NO toca la base de datos.
        //    Solo avisa a la Activity mediante el lambda (así el Adapter solo se ocupa de la vista).
        holder.btnCancelarCita.setOnClickListener {
            expandedPosition = -1  // Cierra el menú visualmente
            onCancelarClick(cita)  // <- dispara cancelarCitaBD() en HistorialActivity
        }
    }

    // 3) Cuántas filas debe pintar el RecyclerView
    override fun getItemCount(): Int = listaCitas.size
}
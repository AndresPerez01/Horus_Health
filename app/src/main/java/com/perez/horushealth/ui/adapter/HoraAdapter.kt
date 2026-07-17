package com.perez.horushealth.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.perez.horushealth.R
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/*
 * ============================================================================
 *  ADAPTADOR DE HORAS  (usado en el Paso 3, layout de cada botón: item_hora.xml)
 * ============================================================================
 *  Pinta la rejilla de horas disponibles (08:00, 09:00...) y controla
 *  cuál está seleccionada para pintarla en azul.
 *
 *  Recibe:
 *   - listaHoras         : las horas libres ya calculadas por Step3Activity.
 *   - onHoraSeleccionada : lambda para avisar a la Activity qué hora se tocó.
 * ============================================================================
 */
class HoraAdapter(
    private val listaHoras: List<LocalTime>,
    private val onHoraSeleccionada: (LocalTime) -> Unit
) : RecyclerView.Adapter<HoraAdapter.HoraViewHolder>() {

    // Variable para rastrear cuál botón está seleccionado actualmente (-1 = ninguno)
    private var posicionSeleccionada = -1

    // ViewHolder: guarda la referencia al botón de UNA celda de la rejilla
    class HoraViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val btnHora = view.findViewById<MaterialButton>(R.id.btnHoraItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HoraViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_hora, parent, false)
        return HoraViewHolder(view)
    }

    override fun onBindViewHolder(holder: HoraViewHolder, position: Int) {
        val hora = listaHoras[position]

        // Formateamos la hora para que se vea como "08:00"
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        holder.btnHora.text = hora.format(formatter)

        // Lógica visual: Si este botón es el seleccionado, lo pintamos azul. Si no, blanco.
        if (position == posicionSeleccionada) {
            holder.btnHora.setBackgroundColor(Color.parseColor("#3266E3")) // Fondo azul
            holder.btnHora.setTextColor(Color.WHITE) // Letras blancas
        } else {
            holder.btnHora.setBackgroundColor(Color.WHITE) // Fondo blanco
            holder.btnHora.setTextColor(Color.parseColor("#424242")) // Letras grises
        }

        // Qué pasa al hacer clic en una hora
        holder.btnHora.setOnClickListener {
            // Guardamos la posición anterior y la nueva para actualizar solo esos dos botones
            val posicionAnterior = posicionSeleccionada
            posicionSeleccionada = holder.adapterPosition

            // Le avisamos al RecyclerView que repinte los botones que cambiaron
            notifyItemChanged(posicionAnterior)
            notifyItemChanged(posicionSeleccionada)

            // Le enviamos la hora elegida al Activity
            onHoraSeleccionada(hora)
        }
    }

    override fun getItemCount(): Int = listaHoras.size
}
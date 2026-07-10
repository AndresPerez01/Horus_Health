package com.perez.horushealth.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.perez.horushealth.R
import com.tu_paquete.horushealth.Medico // Asegúrate de importar tu clase Medico

class MedicoAdapter(
    private val listaMedicos: List<Medico>,
    private val onMedicoClick: (Medico) -> Unit // Esto nos permite manejar el clic en el botón "Elegir"
) : RecyclerView.Adapter<MedicoAdapter.MedicoViewHolder>() {

    // Esta clase interna busca y enlaza los elementos visuales del XML item_medico
    class MedicoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre = view.findViewById<TextView>(R.id.tvNombreMedico)
        val tvSubespecialidad = view.findViewById<TextView>(R.id.tvSubespecialidad)
        val tvLicencia = view.findViewById<TextView>(R.id.tvLicencia)
        val tvClinica = view.findViewById<TextView>(R.id.tvClinica)
        val tvDireccion = view.findViewById<TextView>(R.id.tvDireccion)
        val tvPiso = view.findViewById<TextView>(R.id.tvPiso)
        val btnElegir = view.findViewById<MaterialButton>(R.id.btnElegirMedico)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_medico, parent, false)
        return MedicoViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicoViewHolder, position: Int) {
        val medico = listaMedicos[position]

        // Aquí "inyectamos" los datos en el diseño
        holder.tvNombre.text = medico.nombre
        holder.tvSubespecialidad.text = medico.subespecialidad
        holder.tvLicencia.text = "Licencia: ${medico.licencia}"
        holder.tvClinica.text = medico.clinica
        holder.tvDireccion.text = medico.direccion
        holder.tvPiso.text = medico.pisoYHabitacion

        // Configuramos qué pasa cuando le dan clic a "Elegir"
        holder.btnElegir.setOnClickListener {
            onMedicoClick(medico)
        }
    }

    override fun getItemCount(): Int {
        return listaMedicos.size
    }
}
package com.perez.horushealth.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.perez.horushealth.R
import com.perez.horushealth.data.MedicoEntity // 🔥 Usamos la entidad de Room

class MedicoAdapter(
    private val listaMedicos: List<MedicoEntity>,
    private val onMedicoClick: (MedicoEntity) -> Unit
) : RecyclerView.Adapter<MedicoAdapter.MedicoViewHolder>() {

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

        holder.tvNombre.text = medico.nombre
        holder.tvSubespecialidad.text = medico.subespecialidad
        holder.tvLicencia.text = "Licencia: ${medico.licencia}"
        holder.tvClinica.text = medico.clinica
        holder.tvDireccion.text = medico.direccion
        holder.tvPiso.text = medico.pisoYHabitacion

        holder.btnElegir.setOnClickListener {
            onMedicoClick(medico)
        }
    }

    override fun getItemCount(): Int {
        return listaMedicos.size
    }
}
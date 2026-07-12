package com.perez.horushealth.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.perez.horushealth.R
import com.perez.horushealth.data.AppDatabase
import com.perez.horushealth.ui.adapter.MedicoAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Step2aActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.step2a_lista_medicos)

        val especialidadElegida = intent.getStringExtra("ESPECIALIDAD_SELECCIONADA") ?: ""

        val tvTitulo = findViewById<TextView>(R.id.tvTituloEspecialidad)
        tvTitulo.text = especialidadElegida.uppercase()

        val rvMedicos = findViewById<RecyclerView>(R.id.rvMedicos)
        rvMedicos.layoutManager = LinearLayoutManager(this)

        // Buscamos los médicos en Room en segundo plano
        lifecycleScope.launch(Dispatchers.IO) {
            val dao = AppDatabase.getBaseDatos(this@Step2aActivity).horusDao()
            val medicosFiltrados = dao.getMedicosPorEspecialidad(especialidadElegida)

            // Volvemos a la UI para pintar la lista
            withContext(Dispatchers.Main) {
                val adapter = MedicoAdapter(medicosFiltrados) { medicoSeleccionado ->
                    val intent = Intent(this@Step2aActivity, Step3Activity::class.java)

                    intent.putExtra("ESPECIALIDAD_SELECCIONADA", especialidadElegida)
                    intent.putExtra("MEDICO_ID", medicoSeleccionado.licencia) // Enviamos la licencia
                    intent.putExtra("MEDICO_NOMBRE", medicoSeleccionado.nombre)

                    startActivity(intent)
                }
                rvMedicos.adapter = adapter
            }
        }

        val btnRegresar = findViewById<FloatingActionButton>(R.id.btnRegresarCircular)
        btnRegresar.setOnClickListener {
            finish()
        }
    }
}
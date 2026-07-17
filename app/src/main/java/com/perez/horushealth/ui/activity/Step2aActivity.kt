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

/*
 * ============================================================================
 *  PASO 2a: LISTA DE MÉDICOS   (layout: step2a_lista_medicos.xml)
 * ============================================================================
 *  Solo se llega aquí por el CAMINO MANUAL (el usuario eligió "Elegir mi médico").
 *  Muestra los médicos de la especialidad y, al elegir uno, va al Paso 3.
 * ============================================================================
 */
class Step2aActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.step2a_lista_medicos)

        // Especialidad que viene arrastrada desde el Paso 1
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
                // El bloque { medicoSeleccionado -> ... } se ejecuta cuando el usuario
                // pulsa "Elegir" en una tarjeta (el Adapter dispara este lambda).
                val adapter = MedicoAdapter(medicosFiltrados) { medicoSeleccionado ->
                    val intent = Intent(this@Step2aActivity, Step3Activity::class.java)

                    // Seguimos arrastrando los datos al siguiente paso.
                    // MEDICO_ID es la LICENCIA: es la clave con la que luego se guarda
                    // la cita (foreign key medicoLicencia).
                    intent.putExtra("ESPECIALIDAD_SELECCIONADA", especialidadElegida)
                    intent.putExtra("MEDICO_ID", medicoSeleccionado.licencia)
                    intent.putExtra("MEDICO_NOMBRE", medicoSeleccionado.nombre)

                    startActivity(intent)
                }
                rvMedicos.adapter = adapter   // Entregamos el adaptador a la lista
            }
        }

        val btnRegresar = findViewById<FloatingActionButton>(R.id.btnRegresarCircular)
        btnRegresar.setOnClickListener {
            finish()
        }
    }
}
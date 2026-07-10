package com.perez.horushealth.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.perez.horushealth.R
import com.perez.horushealth.ui.adapter.MedicoAdapter
import com.tu_paquete.horushealth.RepositorioMedicos
import com.google.android.material.floatingactionbutton.FloatingActionButton

class Step2aActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.step2a_lista_medicos)

        // 1. Recuperamos la especialidad del intent
        val especialidadElegida = intent.getStringExtra("ESPECIALIDAD_SELECCIONADA") ?: ""

        // 2. Cambiamos el título en pantalla
        val tvTitulo = findViewById<TextView>(R.id.tvTituloEspecialidad)
        tvTitulo.text = especialidadElegida.uppercase()

        // 3. Filtramos nuestra base de datos simulada
        val medicosFiltrados = RepositorioMedicos.listaMaestra.filter {
            it.especialidad == especialidadElegida
        }

        // 4. Configuramos el RecyclerView
        val rvMedicos = findViewById<RecyclerView>(R.id.rvMedicos)
        rvMedicos.layoutManager = LinearLayoutManager(this)

        // 5. ¡AQUÍ ESTABA EL ERROR! (Le quitamos los comentarios y enviamos los datos)
        val adapter = MedicoAdapter(medicosFiltrados) { medicoSeleccionado ->

            val intent = Intent(this, Step3Activity::class.java) // Asegúrate de que Step3Activity sea tu pantalla de Fecha/Hora

            // "Empaquetamos" todos los datos recolectados hasta ahora para la siguiente pantalla
            intent.putExtra("ESPECIALIDAD_SELECCIONADA", especialidadElegida)
            intent.putExtra("MEDICO_ID", medicoSeleccionado.id)
            intent.putExtra("MEDICO_NOMBRE", medicoSeleccionado.nombre)

            startActivity(intent)
        }

        rvMedicos.adapter = adapter

        // 6. Botón de regresar
        val btnRegresar = findViewById<FloatingActionButton>(R.id.btnRegresarCircular)
        btnRegresar.setOnClickListener {
            finish()
        }
    }
}
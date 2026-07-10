package com.perez.horushealth.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.perez.horushealth.R

class Step1Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Asegúrate de que el nombre del layout coincida con el nombre de tu archivo XML
        setContentView(R.layout.step1_specialty)

        // 1. Vinculamos TODAS las tarjetas de especialidades desde el XML
        val cardMedicinaGeneral = findViewById<MaterialCardView>(R.id.cardMedicinaGeneral)
        val cardCardiologia = findViewById<MaterialCardView>(R.id.cardCardiologia)
        val cardGeriatria = findViewById<MaterialCardView>(R.id.cardGeriatria)
        val cardOftalmologia = findViewById<MaterialCardView>(R.id.cardOftalmologia)
        val cardDermatologia = findViewById<MaterialCardView>(R.id.cardDermatologia)
        val cardTraumatologia = findViewById<MaterialCardView>(R.id.cardTraumatologia)

        // 2. Asignamos la acción de clic a CADA UNA de las tarjetas
        cardMedicinaGeneral.setOnClickListener { irAlPaso2("Medicina General") }
        cardCardiologia.setOnClickListener { irAlPaso2("Cardiología") }
        cardGeriatria.setOnClickListener { irAlPaso2("Geriatría") }
        cardOftalmologia.setOnClickListener { irAlPaso2("Oftalmología") }
        cardDermatologia.setOnClickListener { irAlPaso2("Dermatología") }
        cardTraumatologia.setOnClickListener { irAlPaso2("Traumatología") }

        // 3. Configurar el botón de Regresar
        val btnRegresar = findViewById<FloatingActionButton>(R.id.btnRegresarCircular)
        btnRegresar.setOnClickListener {
            // finish() cierra esta pantalla y te devuelve a la anterior automáticamente
            finish()
        }

        // (Opcional) Si quieres que el botón de Perfil haga algo, puedes agregarlo aquí
        /*
        val btnPerfil = findViewById<FloatingActionButton>(R.id.btnPerfilCircular)
        btnPerfil.setOnClickListener {
            // Lógica para ir al perfil
        }
        */
    }

    // Función auxiliar para navegar a la siguiente pantalla pasando el dato
    private fun irAlPaso2(especialidad: String) {
        val intent = Intent(this, Step2Activity::class.java)
        intent.putExtra("ESPECIALIDAD_SELECCIONADA", especialidad)
        startActivity(intent)
    }
}
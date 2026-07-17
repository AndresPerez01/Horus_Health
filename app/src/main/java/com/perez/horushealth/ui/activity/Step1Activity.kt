package com.perez.horushealth.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.perez.horushealth.R

/*
 * ============================================================================
 *  PASO 1: ELEGIR ESPECIALIDAD   (layout: step1_specialty.xml)
 * ============================================================================
 *  Primera pantalla del flujo de agendamiento. Solo pide UNA decisión
 *  (principio de diseño: una decisión por pantalla).
 *
 *  Las 6 especialidades están "quemadas" (fijas) en el código y en el XML.
 *  El texto que se envía debe coincidir EXACTAMENTE con la columna
 *  "especialidad" de la tabla medicos, porque luego se filtra con:
 *      SELECT * FROM medicos WHERE especialidad = :especialidad
 * ============================================================================
 */
class Step1Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.step1_specialty)

        // Cada tarjeta del XML es una especialidad
        val cardMedicinaGeneral = findViewById<MaterialCardView>(R.id.cardMedicinaGeneral)
        val cardCardiologia = findViewById<MaterialCardView>(R.id.cardCardiologia)
        val cardGeriatria = findViewById<MaterialCardView>(R.id.cardGeriatria)
        val cardOftalmologia = findViewById<MaterialCardView>(R.id.cardOftalmologia)
        val cardDermatologia = findViewById<MaterialCardView>(R.id.cardDermatologia)
        val cardTraumatologia = findViewById<MaterialCardView>(R.id.cardTraumatologia)

        // Todas hacen lo mismo, solo cambia el texto que mandan al Paso 2.
        // ¡El texto debe estar escrito igual que en la base de datos (con tildes)!
        cardMedicinaGeneral.setOnClickListener { irAlPaso2("Medicina General") }
        cardCardiologia.setOnClickListener { irAlPaso2("Cardiología") }
        cardGeriatria.setOnClickListener { irAlPaso2("Geriatría") }
        cardOftalmologia.setOnClickListener { irAlPaso2("Oftalmología") }
        cardDermatologia.setOnClickListener { irAlPaso2("Dermatología") }
        cardTraumatologia.setOnClickListener { irAlPaso2("Traumatología") }

        // Botón circular de regresar: finish() cierra esta pantalla y vuelve a la anterior
        val btnRegresar = findViewById<FloatingActionButton>(R.id.btnRegresarCircular)
        btnRegresar.setOnClickListener {
            finish()
        }
    }

    /**
     * Navega al Paso 2 llevándose la especialidad elegida.
     * putExtra() = "mete este dato en la maleta del Intent" para que
     * la siguiente pantalla lo lea con getStringExtra("ESPECIALIDAD_SELECCIONADA").
     */
    private fun irAlPaso2(especialidad: String) {
        val intent = Intent(this, Step2Activity::class.java)
        intent.putExtra("ESPECIALIDAD_SELECCIONADA", especialidad)
        startActivity(intent)
    }
}

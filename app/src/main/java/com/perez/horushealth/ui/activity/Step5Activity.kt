package com.perez.horushealth.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.perez.horushealth.R
import com.perez.horushealth.data.AppDatabase
import com.perez.horushealth.data.Cita
import com.perez.horushealth.data.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/*
 * ============================================================================
 *  PASO 5: CITA CONFIRMADA / ÉXITO   (layout: step5_exito.xml)
 * ============================================================================
 *  Es la última pantalla de AMBOS caminos, pero se comporta distinto:
 *
 *   - CAMINO AUTOMÁTICO (Step2b -> Step5): AQUÍ se guarda la cita en Room,
 *     porque Step2b le pasa el MEDICO_ID.
 *   - CAMINO MANUAL (Step4 -> Step5): la cita YA se guardó en Step4.
 *     Step4 no envía MEDICO_ID, así que el "if" de abajo NO se cumple
 *     y por eso NO se duplica la cita.
 * ============================================================================
 */
class Step5Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.step5_exito)

        val tvEspecialidad = findViewById<TextView>(R.id.tvExitoEspecialidad)
        val tvMedico = findViewById<TextView>(R.id.tvExitoMedico)
        val tvLugar = findViewById<TextView>(R.id.tvExitoLugar)
        val tvFecha = findViewById<TextView>(R.id.tvExitoFecha)
        val tvHora = findViewById<TextView>(R.id.tvExitoHora)

        val especialidad = intent.getStringExtra("ESPECIALIDAD") ?: ""
        val medicoNombre = intent.getStringExtra("MEDICO") ?: ""
        val medicoId = intent.getStringExtra("MEDICO_ID") ?: "" // La licencia
        val lugar = intent.getStringExtra("LUGAR") ?: ""
        val fecha = intent.getStringExtra("FECHA") ?: ""
        val hora = intent.getStringExtra("HORA") ?: ""

        tvEspecialidad.text = especialidad
        tvMedico.text = medicoNombre
        tvLugar.text = lugar
        tvFecha.text = fecha
        tvHora.text = "$hora h"

        // 🔥 INSERTAR LA CITA EN ROOM (solo ocurre en el camino AUTOMÁTICO)
        val pacienteCedula = SessionManager.getSession(this)

        // Este "if" es el que decide si se guarda o no:
        //   - viene de Step2b (automático) -> medicoId tiene valor  -> SÍ guarda
        //   - viene de Step4  (manual)     -> medicoId llega vacío   -> NO guarda (ya se guardó allí)
        if (pacienteCedula != null && medicoId.isNotEmpty()) {
            lifecycleScope.launch(Dispatchers.IO) {
                val dao = AppDatabase.getBaseDatos(this@Step5Activity).horusDao()

                val nuevaCita = Cita(
                    pacienteCedula = pacienteCedula,
                    medicoLicencia = medicoId,
                    fecha = fecha,
                    hora = hora,
                    estado = "Programada"
                )

                dao.addCita(nuevaCita)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Step5Activity, "Cita guardada en base de datos", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val btnVolver = findViewById<MaterialButton>(R.id.btnVolverInicio)
        btnVolver.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            // CLEAR_TOP: cierra todas las pantallas del flujo (Step1..Step5) que quedaron
            // apiladas detrás, para que "atrás" no devuelva al usuario a agendar otra vez.
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
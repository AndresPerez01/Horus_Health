package com.perez.horushealth.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.perez.horushealth.R
import com.perez.horushealth.data.AppDatabase
import com.perez.horushealth.data.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/*
 * ============================================================================
 *  PANTALLA PRINCIPAL / INICIO
 * ============================================================================
 *  OJO (pregunta trampa típica): su layout NO se llama "main.xml",
 *  se llama "agend.xml".
 *
 *  Contiene: saludo personalizado, botón Agendar Cita, acceso a Mis citas,
 *  cerrar sesión y la barra inferior (Inicio / Perfil).
 * ============================================================================
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.agend)   // <- el layout de esta pantalla

        // ---- GUARDIÁN DE SESIÓN ----
        // Nadie debería llegar aquí sin haber iniciado sesión.
        // Si no hay cédula guardada, lo devolvemos al Login.
        val cedulaUsuario = SessionManager.getSession(this)
        if (cedulaUsuario == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)

        // ---- SALUDO PERSONALIZADO ("Hola, DANNY 👋") ----
        // Buscamos el usuario en Room usando la cédula de la sesión.
        lifecycleScope.launch(Dispatchers.IO) {          // hilo secundario: consultar la BD
            val dao = AppDatabase.getBaseDatos(this@MainActivity).horusDao()
            val user = dao.getUsuarioPorCedula(cedulaUsuario)

            withContext(Dispatchers.Main) {             // hilo principal: pintar en pantalla
                if (user != null) {
                    // split(" ")[0] = nos quedamos SOLO con el primer nombre.
                    // Ej: "Danny Constante" -> "Danny" -> uppercase() -> "DANNY"
                    val firstName = user.nombre.split(" ")[0].uppercase()
                    tvGreeting.text = "Hola, $firstName 👋"
                }
            }
        }

        // ---- TARJETA "MIS CITAS" -> abre el historial ----
        val cardMisCitas = findViewById<MaterialCardView>(R.id.cardMisCitas)
        cardMisCitas.setOnClickListener {
            startActivity(Intent(this, HistorialActivity::class.java))
        }

        // ---- BOTÓN "+ AGENDAR CITA" -> arranca el flujo de agendamiento (Paso 1) ----
        val btnAgendarCita = findViewById<MaterialButton>(R.id.btnAgendarCita)
        btnAgendarCita.setOnClickListener {
            startActivity(Intent(this, Step1Activity::class.java))
        }

        // ---- CERRAR SESIÓN ----
        val btnCerrarSesion = findViewById<MaterialButton>(R.id.btnCerrarSesion)
        btnCerrarSesion.setOnClickListener {
            SessionManager.clearSession(this)   // borra la cédula guardada (no borra la cuenta)

            val intent = Intent(this, LoginActivity::class.java)
            // FLAGS: limpian TODO el historial de pantallas para que con el botón "atrás"
            // el usuario no pueda volver a entrar a la app ya deslogueado.
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // ---- BARRA INFERIOR: botón Perfil ----
        val btnNavPerfil = findViewById<LinearLayout>(R.id.btnNavPerfil)
        btnNavPerfil.setOnClickListener {
            startActivity(Intent(this, PerfilActivity::class.java))
        }
    }
}

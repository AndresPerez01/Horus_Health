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

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.agend)

        val cedulaUsuario = SessionManager.getSession(this)

        if (cedulaUsuario == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)

        // Buscamos el nombre del usuario en la base de datos
        lifecycleScope.launch(Dispatchers.IO) {
            val dao = AppDatabase.getBaseDatos(this@MainActivity).horusDao()
            val user = dao.getUsuarioPorCedula(cedulaUsuario)

            withContext(Dispatchers.Main) {
                if (user != null) {
                    val firstName = user.nombre.split(" ")[0].uppercase()
                    tvGreeting.text = "Hola, $firstName 👋"
                }
            }
        }

        val cardMisCitas = findViewById<MaterialCardView>(R.id.cardMisCitas)
        cardMisCitas.setOnClickListener {
            startActivity(Intent(this, HistorialActivity::class.java))
        }

        val btnAgendarCita = findViewById<MaterialButton>(R.id.btnAgendarCita)
        btnAgendarCita.setOnClickListener {
            startActivity(Intent(this, Step1Activity::class.java))
        }

        val btnCerrarSesion = findViewById<MaterialButton>(R.id.btnCerrarSesion)
        btnCerrarSesion.setOnClickListener {
            SessionManager.clearSession(this)
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        val btnNavPerfil = findViewById<LinearLayout>(R.id.btnNavPerfil)
        btnNavPerfil.setOnClickListener {
            startActivity(Intent(this, PerfilActivity::class.java))
        }
    }
}
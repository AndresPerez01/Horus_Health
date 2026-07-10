package com.perez.horushealth.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.perez.horushealth.R
import com.perez.horushealth.data.LocalStorage
import android.widget.LinearLayout

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.agend)

        // --- VERIFICACIÓN DE SESIÓN ---
        val user = LocalStorage.getSessionUser(this)

        if (user == null) {
            // Si no hay usuario, lo mandamos al Login y cerramos esta pantalla
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return // El return es vital para que no intente ejecutar el código de abajo
        }

        // Si llegamos aquí, sí hay usuario. Mostramos su nombre:
        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        val firstName = user.name.split(" ")[0].uppercase()
        tvGreeting.text = "Hola, $firstName 👋"

        // --- CLICS DE LOS BOTONES ---
        val cardMisCitas = findViewById<MaterialCardView>(R.id.cardMisCitas)
        cardMisCitas.setOnClickListener {
            startActivity(Intent(this, HistorialActivity::class.java))
        }

        val btnAgendarCita = findViewById<MaterialButton>(R.id.btnAgendarCita)
        btnAgendarCita.setOnClickListener {
            val intent = Intent(this, Step1Activity::class.java)
            startActivity(intent)
        }

        val btnCerrarSesion = findViewById<MaterialButton>(R.id.btnCerrarSesion)
        btnCerrarSesion.setOnClickListener {
            LocalStorage.logout(this)
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        val btnNavPerfil = findViewById<LinearLayout>(R.id.btnNavPerfil)
        btnNavPerfil.setOnClickListener {
            val intent = Intent(this, PerfilActivity::class.java)
            startActivity(intent)
        }
    }
}
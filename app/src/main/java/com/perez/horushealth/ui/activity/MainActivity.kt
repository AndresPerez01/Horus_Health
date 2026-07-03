package com.perez.horushealth.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.perez.horushealth.R
import com.perez.horushealth.data.LocalStorage

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.agend)

        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        val user = LocalStorage.getSessionUser(this)
        
        if (user != null) {
            val firstName = user.name.split(" ")[0].uppercase()
            tvGreeting.text = "Hola, $firstName 👋"
        }

        val cardMisCitas = findViewById<MaterialCardView>(R.id.cardMisCitas)
        cardMisCitas.setOnClickListener {
            startActivity(Intent(this, HistorialActivity::class.java))
        }

        val btnAgendarCita = findViewById<MaterialButton>(R.id.btnAgendarCita)
        btnAgendarCita.setOnClickListener {
            // TODO: Implement booking flow
        }

        // --- LÓGICA DE CERRAR SESIÓN ---
        val btnCerrarSesion = findViewById<MaterialButton>(R.id.btnCerrarSesion)
        btnCerrarSesion.setOnClickListener {
            // 1. Limpiamos la sesión en el almacenamiento local
            LocalStorage.logout(this)
            
            // 2. Redirigimos al Login y limpiamos el stack de actividades
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}

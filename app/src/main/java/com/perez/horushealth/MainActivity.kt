package com.perez.horushealth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.agend)

        val cardMisCitas = findViewById<MaterialCardView>(R.id.cardMisCitas)

        cardMisCitas.setOnClickListener {
            val intent = Intent(this, HistorialActivity::class.java)
            startActivity(intent)
        }

        val btnAgendarCita = findViewById<MaterialButton>(R.id.btnAgendarCita)
        btnAgendarCita.setOnClickListener {
        }
    }
}
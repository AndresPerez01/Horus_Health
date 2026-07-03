package com.perez.horushealth.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.perez.horushealth.R

class HistorialActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.historial)

        val btnVolverAtras = findViewById<MaterialCardView>(R.id.btnVolverAtras)
        btnVolverAtras.setOnClickListener { finish() }

        val btnVolverInicio = findViewById<MaterialButton>(R.id.btnVolverInicio)
        btnVolverInicio.setOnClickListener { finish() }
    }
}

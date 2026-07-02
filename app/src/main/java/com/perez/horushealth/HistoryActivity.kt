package com.perez.horushealth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class HistorialActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Carga el xml del historial que me pediste antes
        setContentView(R.layout.historial)
    }
}
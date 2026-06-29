package com.perez.horushealth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Solo cargamos el diseño base, nada más
        setContentView(R.layout.agend)
    }
}
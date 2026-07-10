package com.perez.horushealth.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.perez.horushealth.R
import com.perez.horushealth.data.LocalStorage

class PerfilActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.perfil_usuario)

        // Mostrar el correo real del usuario en el encabezado
        val user = LocalStorage.getSessionUser(this)
        val tvPerfilCorreo = findViewById<TextView>(R.id.tvPerfilCorreo)
        if (user != null) {
            tvPerfilCorreo.text = user.email
        }

        // Navegación Inferior (Volver a Inicio)
        val btnNavInicio = findViewById<LinearLayout>(R.id.btnNavInicio)
        btnNavInicio.setOnClickListener {
            // Regresamos al MainActivity cerrando esta pantalla
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        // Opciones de configuración (Por ahora solo muestran mensajes)
        val cardEditarDatos = findViewById<MaterialCardView>(R.id.cardEditarDatos)
        cardEditarDatos.setOnClickListener {
            Toast.makeText(this, "Opción: Editar Datos Personales", Toast.LENGTH_SHORT).show()
        }

        val cardEditarTarjeta = findViewById<MaterialCardView>(R.id.cardEditarTarjeta)
        cardEditarTarjeta.setOnClickListener {
            Toast.makeText(this, "Opción: Administrar Tarjetas", Toast.LENGTH_SHORT).show()
        }

        val cardCambiarPassword = findViewById<MaterialCardView>(R.id.cardCambiarPassword)
        cardCambiarPassword.setOnClickListener {
            Toast.makeText(this, "Opción: Cambiar Contraseña", Toast.LENGTH_SHORT).show()
        }
    }
}
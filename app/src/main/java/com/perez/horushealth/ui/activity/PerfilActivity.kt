package com.perez.horushealth.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.perez.horushealth.R
import com.perez.horushealth.data.AppDatabase
import com.perez.horushealth.data.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PerfilActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.perfil_usuario)

        val cedulaUsuario = SessionManager.getSession(this)
        val tvPerfilCorreo = findViewById<TextView>(R.id.tvPerfilCorreo)

        if (cedulaUsuario != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                val dao = AppDatabase.getBaseDatos(this@PerfilActivity).horusDao()
                val user = dao.getUsuarioPorCedula(cedulaUsuario)

                withContext(Dispatchers.Main) {
                    if (user != null) {
                        tvPerfilCorreo.text = user.correo
                    }
                }
            }
        }

        val btnNavInicio = findViewById<LinearLayout>(R.id.btnNavInicio)
        btnNavInicio.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

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
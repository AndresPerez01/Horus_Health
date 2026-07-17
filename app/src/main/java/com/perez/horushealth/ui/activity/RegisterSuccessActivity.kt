package com.perez.horushealth.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.perez.horushealth.R

/*
 * ============================================================================
 *  PANTALLA DE REGISTRO EXITOSO   (layout: register_success.xml)
 * ============================================================================
 *  Pantalla de confirmación tras crear la cuenta. No tiene lógica:
 *  solo muestra el mensaje y un botón.
 *
 *  NOTA: RegisterActivity ya dejó la sesión abierta (SessionManager), así que
 *  al ir al Login este detecta la sesión y redirige solo a la pantalla principal.
 * ============================================================================
 */
class RegisterSuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_success)

        val btnGoToLogin = findViewById<MaterialButton>(R.id.btnGoToLogin)
        btnGoToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            // CLEAR_TOP + SINGLE_TOP: reutiliza el Login si ya existe en la pila
            // en vez de crear otro encima (evita pantallas duplicadas).
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }
}

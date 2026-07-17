package com.perez.horushealth.data

import android.content.Context

/*
 * ============================================================================
 *  GESTOR DE SESIÓN  ("¿quién está usando la app ahora mismo?")
 * ============================================================================
 *  NO usa Room. Usa SharedPreferences: un almacén simple clave->valor que
 *  Android guarda en un archivo XML privado de la app.
 *
 *  ¿Por qué no Room? Porque solo necesitamos guardar UN dato pequeño
 *  (la cédula del usuario logueado) y debe leerse muy rápido al abrir la app.
 *
 *  "object" = SINGLETON de Kotlin: existe una sola instancia global,
 *  se llama directo como SessionManager.getSession(...) sin hacer new.
 * ============================================================================
 */
object SessionManager {
    private const val PREFS_NAME = "horus_health_session"   // Nombre del archivo XML
    private const val KEY_LOGGED_IN_CEDULA = "logged_in_cedula" // La clave dentro de ese archivo

    /**
     * Guarda la cédula cuando el usuario inicia sesión correctamente.
     * Se llama desde: LoginActivity (login normal y por huella) y RegisterActivity.
     * apply() = guarda en segundo plano (no bloquea). commit() sería síncrono.
     */
    fun saveSession(context: Context, cedula: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) // MODE_PRIVATE: solo esta app lo lee
            .edit()
            .putString(KEY_LOGGED_IN_CEDULA, cedula)
            .apply()
    }

    /**
     * Devuelve la cédula del usuario activo, o null si nadie ha iniciado sesión.
     * Es la "llave" con la que el resto de pantallas consultan al usuario en Room
     * (dao.getUsuarioPorCedula). Si devuelve null -> se manda al Login.
     */
    fun getSession(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LOGGED_IN_CEDULA, null)   // null = valor por defecto si no existe
    }

    /**
     * Borra la sesión: se llama al pulsar "Cerrar Sesión" en la pantalla principal.
     * OJO: solo borra la sesión, NO borra la cuenta de la base de datos.
     */
    fun clearSession(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}

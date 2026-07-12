package com.perez.horushealth.data

import android.content.Context

object SessionManager {
    private const val PREFS_NAME = "horus_health_session"
    private const val KEY_LOGGED_IN_CEDULA = "logged_in_cedula"

    // Guarda la cédula cuando el usuario inicia sesión correctamente
    fun saveSession(context: Context, cedula: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LOGGED_IN_CEDULA, cedula)
            .apply()
    }

    // Obtiene la cédula del usuario activo (retorna null si nadie ha iniciado sesión)
    fun getSession(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LOGGED_IN_CEDULA, null)
    }

    // Borra la sesión cuando el usuario cierra sesión
    fun clearSession(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}
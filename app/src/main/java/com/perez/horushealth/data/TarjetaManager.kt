package com.perez.horushealth.data

import android.content.Context

/**
 * Almacena los datos de la tarjeta de cada usuario en SharedPreferences.
 * Por seguridad NO se guarda el número completo, solo los últimos 4 dígitos,
 * el titular, la marca y la fecha de expiración. Se indexa por cédula para que
 * cada usuario tenga su propia tarjeta.
 */
object TarjetaManager {
    private const val PREFS_NAME = "horus_health_tarjetas"

    data class Tarjeta(
        val titular: String,
        val marca: String,
        val ultimos4: String,
        val expiracion: String
    )

    fun guardarTarjeta(context: Context, cedula: String, tarjeta: Tarjeta) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("titular_$cedula", tarjeta.titular)
            .putString("marca_$cedula", tarjeta.marca)
            .putString("ultimos4_$cedula", tarjeta.ultimos4)
            .putString("exp_$cedula", tarjeta.expiracion)
            .apply()
    }

    fun getTarjeta(context: Context, cedula: String): Tarjeta? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val ultimos4 = prefs.getString("ultimos4_$cedula", null) ?: return null
        return Tarjeta(
            titular = prefs.getString("titular_$cedula", "") ?: "",
            marca = prefs.getString("marca_$cedula", "") ?: "",
            ultimos4 = ultimos4,
            expiracion = prefs.getString("exp_$cedula", "") ?: ""
        )
    }

    fun eliminarTarjeta(context: Context, cedula: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove("titular_$cedula")
            .remove("marca_$cedula")
            .remove("ultimos4_$cedula")
            .remove("exp_$cedula")
            .apply()
    }
}

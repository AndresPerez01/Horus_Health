package com.perez.horushealth.model

/*
 * ============================================================================
 *  MODELO: PAÍS  (para el selector de país / código telefónico)
 * ============================================================================
 *  NO es una tabla de Room (no tiene @Entity): es solo un objeto en memoria.
 *  La lista de países está fija en RegisterActivity y PerfilActivity.
 *
 *  De este objeto, lo único que se guarda en la base de datos es "code"
 *  (ej: "+593"), en la columna paisCodigo de la tabla usuarios.
 * ============================================================================
 */
data class Country(
    val name: String,   // "Ecuador"
    val code: String,   // "+593"  <- esto es lo que se guarda en Room
    val flag: String    // "🇪🇨" (emoji)
) {
    /**
     * toString() se sobrescribe para que el desplegable muestre un texto bonito.
     * El ArrayAdapter llama a este método automáticamente para pintar cada fila.
     * Resultado: "🇪🇨 Ecuador (+593)"
     */
    override fun toString(): String {
        return "$flag $name ($code)"
    }
}

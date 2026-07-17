package com.perez.horushealth.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tu_paquete.horushealth.RepositorioMedicos // Lista fija de médicos (MedicData.kt)
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/*
 * ============================================================================
 *  LA BASE DE DATOS (Room)  <- "¿Dónde está la base de datos?" => AQUÍ
 * ============================================================================
 *  Room es una capa sobre SQLite. Esta clase es el punto central que:
 *    1) declara qué tablas existen,
 *    2) entrega el DAO (las consultas),
 *    3) crea el archivo físico de la BD en el teléfono.
 * ============================================================================
 */

// @Database: aquí se REGISTRAN las 3 tablas de la app.
//   - entities  = las clases @Entity que forman las tablas.
//   - version   = número de versión del esquema. Si algún día cambias una tabla,
//                 hay que subir este número y escribir una "migración".
@Database(entities = [Usuario::class, MedicoEntity::class, Cita::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    // Room implementa este método solo: nos devuelve el DAO con todas las consultas.
    abstract fun horusDao(): HorusDao

    companion object {
        // @Volatile: asegura que todos los hilos vean SIEMPRE el valor más reciente.
        @Volatile
        private var INSTANCIA: AppDatabase? = null

        /**
         * PATRÓN SINGLETON: garantiza UNA SOLA instancia de la base de datos en toda la app.
         * ¿Por qué? Abrir varias conexiones a SQLite es caro y puede corromper datos.
         *
         * Se lee así: "si INSTANCIA ya existe devuélvela; si no, créala de forma sincronizada".
         */
        fun getBaseDatos(context: Context): AppDatabase {
            return INSTANCIA ?: synchronized(this) {   // synchronized: solo un hilo entra a crearla
                val instancia = Room.databaseBuilder(
                    context.applicationContext,        // applicationContext evita fugas de memoria
                    AppDatabase::class.java,
                    "base_horus_health"                // <- NOMBRE DEL ARCHIVO FÍSICO de la BD en el teléfono
                )
                    .addCallback(DatabaseCallback())   // Callback que precarga los médicos (ver abajo)
                    .build()

                INSTANCIA = instancia
                instancia
            }
        }
    }

    /**
     * CLASE DE RELACIÓN: une una Cita con SU médico en un solo objeto.
     * Se usa en la pantalla "Mis citas", donde hay que mostrar datos de ambas tablas.
     *
     *  - @Embedded: incrusta todas las columnas de la tabla citas.
     *  - @Relation: le dice a Room "busca en medicos el que tenga
     *               licencia == medicoLicencia de esta cita".
     * Es el equivalente a un JOIN, pero devolviendo objetos ya armados.
     */
    data class CitaConMedico(
        @androidx.room.Embedded val cita: Cita,
        @androidx.room.Relation(
            parentColumn = "medicoLicencia",  // columna de la cita
            entityColumn = "licencia"         // columna del médico con la que enlaza
        )
        val medico: MedicoEntity
    )

    /**
     * PRECARGA DE DATOS (seed).
     * onCreate() se ejecuta UNA SOLA VEZ: la primerísima vez que se crea el archivo de la BD.
     * (Si desinstalas la app y la reinstalas, se vuelve a ejecutar.)
     *
     * Copia la lista fija de médicos de MedicData.kt a la tabla "medicos",
     * para que la app arranque ya con médicos disponibles.
     */
    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Corrutina en hilo IO: insertar en la BD no puede bloquear la app.
            CoroutineScope(Dispatchers.IO).launch {
                val dao = INSTANCIA?.horusDao()

                // Recorremos la lista "quemada" y la convertimos a entidades de Room.
                RepositorioMedicos.listaMaestra.forEach { medicoAntiguo ->
                    val nuevoMedicoRoom = MedicoEntity(
                        licencia = medicoAntiguo.licencia,
                        nombre = medicoAntiguo.nombre,
                        especialidad = medicoAntiguo.especialidad,
                        subespecialidad = medicoAntiguo.subespecialidad,
                        clinica = medicoAntiguo.clinica,
                        direccion = medicoAntiguo.direccion,
                        pisoYHabitacion = medicoAntiguo.pisoYHabitacion,
                        genero = medicoAntiguo.genero,
                        iniciales = medicoAntiguo.iniciales,
                        horaInicio = medicoAntiguo.horaInicio,
                        horaFin = medicoAntiguo.horaFin
                    )
                    dao?.addMedico(nuevoMedicoRoom)
                }
            }
        }
    }
}

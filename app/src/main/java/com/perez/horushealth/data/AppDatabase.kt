package com.perez.horushealth.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tu_paquete.horushealth.RepositorioMedicos // Importa tu lista antigua
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Usuario::class, MedicoEntity::class, Cita::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun horusDao(): HorusDao

    companion object {
        @Volatile
        private var INSTANCIA: AppDatabase? = null

        fun getBaseDatos(context: Context): AppDatabase {
            return INSTANCIA ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "base_horus_health"
                )
                    .addCallback(DatabaseCallback()) // 🔥 Le agregamos el Callback
                    .build()

                INSTANCIA = instancia
                instancia
            }
        }
    }

    // 1. Clase que une la Cita con el Médico
    data class CitaConMedico(
        @androidx.room.Embedded val cita: Cita,
        @androidx.room.Relation(
            parentColumn = "medicoLicencia",
            entityColumn = "licencia"
        )
        val medico: MedicoEntity
    )

    // 🔥 Esta clase llena los médicos de forma automática al crear la base de datos
    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Usamos una Corrutina para no bloquear la app mientras guarda los médicos
            CoroutineScope(Dispatchers.IO).launch {
                val dao = INSTANCIA?.horusDao()

                // Recorremos tu lista antigua y la guardamos en la tabla de Room
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
package com.perez.horushealth.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey


// Equivalente a Jugador: Tabla de Usuarios (Pacientes)
@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey
    val cedula: String,
    val nombre: String,
    val correo: String,
    val telefono: String,
    val fechaNacimiento: String,
    val contrasena: String,
    val paisCodigo: String,
    val biometriaActivada: Boolean
)

// Equivalente a Jugador: Tabla de Médicos
@Entity(tableName = "medicos")
data class MedicoEntity(
    @PrimaryKey
    val licencia: String,
    val nombre: String,
    val especialidad: String,
    val subespecialidad: String,
    val clinica: String,
    val direccion: String,
    val pisoYHabitacion: String,
    val genero: String,
    val iniciales: String,
    val horaInicio: Int,
    val horaFin: Int
)

// Equivalente a Jugador: Tabla de Citas (Conectada con las otras dos)
@Entity(
    tableName = "citas",
    foreignKeys = [
        ForeignKey(entity = Usuario::class, parentColumns = ["cedula"], childColumns = ["pacienteCedula"]),
        ForeignKey(entity = MedicoEntity::class, parentColumns = ["licencia"], childColumns = ["medicoLicencia"])
    ]
)
data class Cita(
    @PrimaryKey(autoGenerate = true)
    val idCita: Int = 0,
    val pacienteCedula: String,
    val medicoLicencia: String,
    val fecha: String,
    val hora: String,
    val estado: String
)
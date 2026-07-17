package com.perez.horushealth.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

/*
 * ============================================================================
 *  ENTIDADES = LAS TABLAS DE LA BASE DE DATOS (Room)
 * ============================================================================
 *  Cada "data class" marcada con @Entity se convierte en una TABLA de SQLite.
 *  Cada propiedad (val) se convierte en una COLUMNA.
 *  Room genera el SQL "CREATE TABLE" automáticamente a partir de esto.
 * ============================================================================
 */

// ------------------------- TABLA 1: USUARIOS (Pacientes) -------------------------
@Entity(tableName = "usuarios")   // El nombre real de la tabla en SQLite será "usuarios"
data class Usuario(
    @PrimaryKey                   // Clave primaria: identifica de forma única a cada usuario.
    val cedula: String,           // Usamos la cédula (no un número autogenerado) porque ya es única por persona.

    val nombre: String,
    val correo: String,           // Se usa para iniciar sesión (ver HorusDao.getUsuario)
    val telefono: String,
    val fechaNacimiento: String,  // Se guarda como texto porque SQLite no tiene tipo "fecha"
    val contrasena: String,       // NOTA: en una app real esto debería guardarse HASHEADO, no en texto plano.
    val paisCodigo: String,       // Ej: "+593" (viene del selector de país)
    val biometriaActivada: Boolean // true = el usuario puede entrar con huella (ver LoginActivity)
)

// ------------------------- TABLA 2: MÉDICOS -------------------------
@Entity(tableName = "medicos")
data class MedicoEntity(
    @PrimaryKey
    val licencia: String,         // Clave primaria: el número de licencia médica.

    val nombre: String,
    val especialidad: String,     // Por esta columna se filtra en el Paso 1 (Medicina General, Cardiología...)
    val subespecialidad: String,
    val clinica: String,
    val direccion: String,
    val pisoYHabitacion: String,
    val genero: String,           // Se usa para decidir si mostrar "Dr." o "Dra."
    val iniciales: String,

    // Jornada laboral en formato 24h. GestorHorarios genera los turnos entre estas dos horas.
    val horaInicio: Int,          // Ej: 8  -> 08:00
    val horaFin: Int              // Ej: 17 -> 17:00
)

// ------------------------- TABLA 3: CITAS (tabla relacional) -------------------------
@Entity(
    tableName = "citas",
    // FOREIGN KEYS: conectan esta tabla con las otras dos.
    // Garantizan que no se pueda crear una cita de un paciente o un médico que no existen.
    foreignKeys = [
        // pacienteCedula (aquí) apunta a -> cedula (en la tabla usuarios)
        ForeignKey(entity = Usuario::class, parentColumns = ["cedula"], childColumns = ["pacienteCedula"]),
        // medicoLicencia (aquí) apunta a -> licencia (en la tabla medicos)
        ForeignKey(entity = MedicoEntity::class, parentColumns = ["licencia"], childColumns = ["medicoLicencia"])
    ]
)
data class Cita(
    @PrimaryKey(autoGenerate = true)  // Aquí SÍ autogeneramos el id: 1, 2, 3... porque una cita no tiene un id natural.
    val idCita: Int = 0,              // Se pone 0 y Room lo reemplaza por el número real al insertar.

    val pacienteCedula: String,       // ¿De quién es la cita?  (FK -> usuarios)
    val medicoLicencia: String,       // ¿Con qué médico?        (FK -> medicos)
    val fecha: String,                // Se guarda como texto ISO: "2026-07-16"
    val hora: String,                 // Se guarda como texto: "10:00"
    val estado: String                // "Activa" / "Programada" / "Cancelada" / "Realizada"
)

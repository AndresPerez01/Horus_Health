package com.perez.horushealth.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.perez.horushealth.data.AppDatabase.CitaConMedico

@Dao
interface HorusDao {

    // --- CONSULTAS DE USUARIOS ---
    @Insert
    suspend fun addUsuario(usuario: Usuario)

    @Query("SELECT * FROM usuarios WHERE correo = :email LIMIT 1")
    suspend fun getUsuario(email: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE cedula = :cedula LIMIT 1")
    suspend fun getUsuarioPorCedula(cedula: String): Usuario?

    @androidx.room.Update
    suspend fun actualizarUsuario(usuario: Usuario)

    // --- CONSULTAS DE MÉDICOS ---
    @Insert
    suspend fun addMedico(medico: MedicoEntity)

    @Query("SELECT * FROM medicos WHERE especialidad = :especialidad")
    suspend fun getMedicosPorEspecialidad(especialidad: String): List<MedicoEntity>

    // --- CONSULTAS DE CITAS ---
    @Insert
    suspend fun addCita(cita: Cita)

    @Query("SELECT * FROM citas WHERE pacienteCedula = :cedula ORDER BY idCita DESC")
    suspend fun getCitasPorPaciente(cedula: String): List<Cita>

    // 2. Agrega estos métodos dentro de tu interface HorusDao
    @androidx.room.Transaction
    @androidx.room.Query("SELECT * FROM citas WHERE pacienteCedula = :cedula ORDER BY fecha DESC, hora DESC")
    suspend fun getCitasDePaciente(cedula: String): List<CitaConMedico>

    @androidx.room.Update
    suspend fun actualizarCita(cita: Cita)
}
package com.perez.horushealth.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.perez.horushealth.data.AppDatabase.CitaConMedico

/*
 * ============================================================================
 *  DAO = Data Access Object  ("el objeto que accede a los datos")
 * ============================================================================
 *  Aquí se define TODO el SQL de la app. Es una INTERFACE: nosotros solo
 *  declaramos QUÉ queremos; Room (con kapt, el procesador de anotaciones)
 *  genera automáticamente la clase que lo implementa al compilar.
 *
 *  Todos los métodos son "suspend" -> son funciones de CORRUTINA.
 *  Room lo exige para obligarnos a consultar FUERA del hilo principal
 *  (si no, la app se congelaría). Por eso siempre se llaman dentro de
 *  lifecycleScope.launch(Dispatchers.IO).
 *
 *  Los ":nombre" dentro del SQL son PARÁMETROS que se enlazan con los
 *  argumentos de la función (evita inyección SQL).
 * ============================================================================
 */
@Dao
interface HorusDao {

    // ======================= CONSULTAS DE USUARIOS =======================

    @Insert                                        // Room genera el INSERT solo
    suspend fun addUsuario(usuario: Usuario)       // Se usa al REGISTRARSE

    // Busca por correo -> se usa en el LOGIN. Devuelve null si no existe.
    @Query("SELECT * FROM usuarios WHERE correo = :email LIMIT 1")
    suspend fun getUsuario(email: String): Usuario?

    // Busca por cédula -> se usa cuando ya hay sesión (SessionManager guarda la cédula).
    @Query("SELECT * FROM usuarios WHERE cedula = :cedula LIMIT 1")
    suspend fun getUsuarioPorCedula(cedula: String): Usuario?

    // @Update busca el registro por su CLAVE PRIMARIA (cedula) y reemplaza el resto.
    // Se usa para: editar datos, cambiar contraseña y activar/desactivar la huella.
    @androidx.room.Update
    suspend fun actualizarUsuario(usuario: Usuario)

    // ======================= CONSULTAS DE MÉDICOS =======================

    @Insert
    suspend fun addMedico(medico: MedicoEntity)    // Solo lo usa la precarga inicial (AppDatabase)

    // Filtra los médicos de una especialidad -> Paso 2 del agendamiento.
    @Query("SELECT * FROM medicos WHERE especialidad = :especialidad")
    suspend fun getMedicosPorEspecialidad(especialidad: String): List<MedicoEntity>

    // ======================= CONSULTAS DE CITAS =======================

    @Insert
    suspend fun addCita(cita: Cita)                // Guardar la cita (Step4 manual / Step5 automático)

    // CLAVE PARA EVITAR DOBLE RESERVA:
    // devuelve solo la columna "hora" de las citas ya tomadas de ese médico ese día.
    // Excluye las canceladas, porque esa hora vuelve a quedar libre.
    @Query("SELECT hora FROM citas WHERE medicoLicencia = :idMedico AND fecha = :fechaElegida AND estado != 'Cancelada'")
    suspend fun getHorasOcupadasPorMedico(idMedico: String, fechaElegida: String): List<String>

    // Citas "planas" de un paciente (sin datos del médico). Actualmente no se usa en pantalla.
    @Query("SELECT * FROM citas WHERE pacienteCedula = :cedula ORDER BY idCita DESC")
    suspend fun getCitasPorPaciente(cedula: String): List<Cita>

    // LA CONSULTA QUE USA "MIS CITAS":
    // @Transaction es obligatorio porque Room hace 2 consultas por debajo
    // (una para las citas y otra para sus médicos) y las une en CitaConMedico.
    // La transacción garantiza que ambas vean los mismos datos.
    @androidx.room.Transaction
    @androidx.room.Query("SELECT * FROM citas WHERE pacienteCedula = :cedula ORDER BY fecha DESC, hora DESC")
    suspend fun getCitasDePaciente(cedula: String): List<CitaConMedico>

    // Se usa para CANCELAR: no borramos la cita, solo cambiamos su estado a "Cancelada".
    @androidx.room.Update
    suspend fun actualizarCita(cita: Cita)
}

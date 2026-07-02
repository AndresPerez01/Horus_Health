package com.perez.horushealth

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class UserProfile(
    val name: String,
    val email: String,
    val phone: String,
    val birthDate: String,
    val password: String
)

data class Doctor(
    val name: String,
    val specialty: String,
    val schedule: String,
    val rating: Double
)

data class Appointment(
    val id: String,
    val specialty: String,
    val doctorName: String,
    val dateLabel: String,
    val time: String,
    val status: String
)

object LocalStorage {
    private const val PREFS_NAME = "horus_health_local_storage"
    private const val KEY_USERS = "users"
    private const val KEY_APPOINTMENTS = "appointments"
    private const val KEY_SESSION_EMAIL = "session_email"

    private val doctorsBySpecialty = mapOf(
        "Medicina General" to listOf(
            Doctor("Dra. Ana Lopez", "Medicina General", "Lun - Vie · 9:00-16:00", 4.9),
            Doctor("Dr. Juan Perez", "Medicina General", "Lun - Vie · 8:00-17:00", 4.2),
            Doctor("Dr. Carlos Ruiz", "Medicina General", "Lun - Jue · 10:00-18:00", 4.5)
        ),
        "Cardiologia" to listOf(
            Doctor("Dra. Marta Sierra", "Cardiologia", "Mar - Vie · 8:00-15:00", 4.8),
            Doctor("Dr. Andres Leon", "Cardiologia", "Lun - Mie · 10:00-17:00", 4.6)
        ),
        "Pediatria" to listOf(
            Doctor("Dra. Luisa Gomez", "Pediatria", "Lun - Sab · 7:00-14:00", 4.9),
            Doctor("Dr. Felipe Torres", "Pediatria", "Mar - Vie · 9:00-16:00", 4.7)
        ),
        "Traumatologia" to listOf(
            Doctor("Dr. Camilo Rojas", "Traumatologia", "Lun - Vie · 8:00-13:00", 4.4),
            Doctor("Dra. Paula Mejia", "Traumatologia", "Mie - Sab · 12:00-18:00", 4.8)
        ),
        "Oftalmologia" to listOf(
            Doctor("Dra. Natalia Vega", "Oftalmologia", "Lun - Jue · 9:00-16:00", 4.7),
            Doctor("Dr. Sergio Mora", "Oftalmologia", "Mar - Vie · 8:00-14:00", 4.5)
        )
    )

    fun ensureSeedData(context: Context) {
        val users = loadUsers(context)
        val cleanedUsers = users.filterNot {
            it.email.equals("juana.b.pinzon@gmail.com", ignoreCase = true) && it.password == "Juana123"
        }
        if (cleanedUsers.size != users.size) {
            saveUsers(context, cleanedUsers)
        }

        val appointments = loadAppointments(context)
        val cleanedAppointments = appointments.filterNot { it.id.startsWith("demo-") }
        if (cleanedAppointments.size != appointments.size) {
            saveAppointments(context, cleanedAppointments)
        }
    }

    fun login(context: Context, email: String, password: String): UserProfile? {
        ensureSeedData(context)
        val user = loadUsers(context).firstOrNull {
            it.email.equals(email, ignoreCase = true) && it.password == password
        } ?: return null

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SESSION_EMAIL, user.email)
            .apply()

        return user
    }

    fun logout(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_SESSION_EMAIL)
            .apply()
    }

    fun getSessionUser(context: Context): UserProfile? {
        ensureSeedData(context)
        val email = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SESSION_EMAIL, null)
            ?: return null
        return loadUsers(context).firstOrNull { it.email.equals(email, ignoreCase = true) }
    }

    fun registerUser(context: Context, user: UserProfile): Result<Unit> {
        ensureSeedData(context)
        val users = loadUsers(context).toMutableList()
        if (users.any { it.email.equals(user.email, ignoreCase = true) }) {
            return Result.failure(IllegalArgumentException("Ya existe una cuenta con ese correo"))
        }

        users.add(user)
        saveUsers(context, users)
        return Result.success(Unit)
    }

    fun getDoctorsForSpecialty(specialty: String): List<Doctor> {
        return doctorsBySpecialty[specialty].orEmpty()
    }

    fun getBestDoctorForSpecialty(specialty: String): Doctor? {
        return getDoctorsForSpecialty(specialty).maxByOrNull { it.rating }
    }

    fun loadAppointments(context: Context): List<Appointment> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_APPOINTMENTS, null).orEmpty()
        if (raw.isBlank()) return emptyList()

        val array = JSONArray(raw)
        return buildList {
            for (index in 0 until array.length()) {
                add(array.getJSONObject(index).toAppointment())
            }
        }
    }

    fun saveAppointment(context: Context, appointment: Appointment) {
        val appointments = loadAppointments(context).toMutableList()
        appointments.add(0, appointment)
        saveAppointments(context, appointments)
    }

    fun cancelAppointment(context: Context, appointmentId: String) {
        val appointments = loadAppointments(context).map { appointment ->
            if (appointment.id == appointmentId) appointment.copy(status = "cancelled") else appointment
        }
        saveAppointments(context, appointments)
    }

    fun deleteAppointment(context: Context, appointmentId: String) {
        val appointments = loadAppointments(context).filterNot { it.id == appointmentId }
        saveAppointments(context, appointments)
    }

    private fun saveAppointments(context: Context, appointments: List<Appointment>) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_APPOINTMENTS, JSONArray(appointments.map { it.toJson() }).toString())
            .apply()
    }

    private fun saveUsers(context: Context, users: List<UserProfile>) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_USERS, JSONArray(users.map { it.toJson() }).toString())
            .apply()
    }

    private fun loadUsers(context: Context): List<UserProfile> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_USERS, null).orEmpty()
        if (raw.isBlank()) return emptyList()

        val array = JSONArray(raw)
        return buildList {
            for (index in 0 until array.length()) {
                add(array.getJSONObject(index).toUser())
            }
        }
    }

    private fun UserProfile.toJson(): JSONObject = JSONObject()
        .put("name", name)
        .put("email", email)
        .put("phone", phone)
        .put("birthDate", birthDate)
        .put("password", password)

    private fun Appointment.toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("specialty", specialty)
        .put("doctorName", doctorName)
        .put("dateLabel", dateLabel)
        .put("time", time)
        .put("status", status)

    private fun JSONObject.toUser(): UserProfile = UserProfile(
        name = getString("name"),
        email = getString("email"),
        phone = getString("phone"),
        birthDate = getString("birthDate"),
        password = getString("password")
    )

    private fun JSONObject.toAppointment(): Appointment = Appointment(
        id = getString("id"),
        specialty = getString("specialty"),
        doctorName = getString("doctorName"),
        dateLabel = getString("dateLabel"),
        time = getString("time"),
        status = getString("status")
    )
}

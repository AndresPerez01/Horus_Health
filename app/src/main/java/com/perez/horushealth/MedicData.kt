package com.tu_paquete.horushealth
// OJO (dato para la defensa): este archivo quedó con el package de ejemplo
// "com.tu_paquete.horushealth" en vez de "com.perez.horushealth".
// Por eso AppDatabase.kt tiene que importarlo con ese nombre raro.

/*
 * ============================================================================
 *  SEMILLA DE MÉDICOS (datos de precarga)
 * ============================================================================
 *  IMPORTANTE: esto NO es la base de datos, es una LISTA FIJA en el código.
 *
 *  ¿Para qué sirve? La app necesita arrancar con médicos ya cargados (nadie
 *  los registra desde la app). La primera vez que se crea la base de datos,
 *  el DatabaseCallback de AppDatabase.kt recorre esta lista y la copia a la
 *  tabla "medicos" de Room.
 *
 *  Es decir:  MedicData.kt (lista fija)  --se copia una vez-->  tabla medicos (Room)
 *  A partir de ahí, la app SIEMPRE lee de Room, nunca de esta lista.
 * ============================================================================
 */

// Clase "de paso": representa un médico ANTES de entrar a Room.
// Es casi igual a MedicoEntity, pero sin anotaciones (no es una tabla).
data class Medico(
    val id: String,
    val nombre: String,
    val especialidad: String,
    val subespecialidad: String,
    val licencia: String,
    val clinica: String,
    val direccion: String,
    val pisoYHabitacion: String,
    val genero: String,
    val iniciales: String,
    val horaInicio: Int, // Formato 24h: 8 (para 8:00 AM)
    val horaFin: Int     // Formato 24h: 17 (para 17:00 / 5:00 PM)
)

/**
 * Singleton con la lista maestra de médicos.
 * Solo lo usa AppDatabase.DatabaseCallback (la precarga inicial).
 */
object RepositorioMedicos {
    val listaMaestra = listOf(

        // ---------------- MEDICINA GENERAL (3 Médicos) ----------------
        Medico(
            id = "mg-001",
            nombre = "Dra. Sofía Mendieta Vega",
            especialidad = "Medicina General",
            subespecialidad = "Medicina Familiar y Preventiva",
            licencia = "MSP-MG-12348",
            clinica = "Centro Médico Metropolitano",
            direccion = "Av. Mariana de Jesús y Nicolás Arteta, Quito",
            pisoYHabitacion = "Piso 3 • Consultorio 305",
            genero = "F",
            iniciales = "SM",
            horaInicio = 8,
            horaFin = 16
        ),
        Medico(
            id = "mg-002",
            nombre = "Dr. Juan Pérez",
            especialidad = "Medicina General",
            subespecialidad = "Medicina General y Urgencias",
            licencia = "MSP-MG-09182",
            clinica = "Clínica Pichincha",
            direccion = "Veintimilla E3-30 y Páez, Quito",
            pisoYHabitacion = "Planta Baja • Habitación 02",
            genero = "M",
            iniciales = "JP",
            horaInicio = 10,
            horaFin = 18
        ),
        Medico(
            id = "mg-003",
            nombre = "Dra. Andrea Viteri",
            especialidad = "Medicina General",
            subespecialidad = "Salud Pública",
            licencia = "MSP-MG-44122",
            clinica = "Hospital de los Valles",
            direccion = "Av. Interoceánica km 12.5, Cumbayá",
            pisoYHabitacion = "Piso 1 • Consultorio 112",
            genero = "F",
            iniciales = "AV",
            horaInicio = 7,
            horaFin = 13
        ),

        // ---------------- CARDIOLOGÍA (2 Médicos) ----------------
        Medico(
            id = "car-001",
            nombre = "Dr. Carlos Ruiz",
            especialidad = "Cardiología",
            subespecialidad = "Cardiología Clínica e Hipertensión",
            licencia = "MSP-CA-55441",
            clinica = "Hospital Metropolitano",
            direccion = "Av. Mariana de Jesús, Quito",
            pisoYHabitacion = "Piso 4 • Torre Médica 2, Cons. 401",
            genero = "M",
            iniciales = "CR",
            horaInicio = 9,
            horaFin = 15
        ),
        Medico(
            id = "car-002",
            nombre = "Dra. Elena Torres",
            especialidad = "Cardiología",
            subespecialidad = "Ecocardiografía",
            licencia = "MSP-CA-11234",
            clinica = "Novaclinica Santa Cecilia",
            direccion = "Av. Veintimilla y 10 de Agosto, Quito",
            pisoYHabitacion = "Piso 2 • Consultorio 22",
            genero = "F",
            iniciales = "ET",
            horaInicio = 11,
            horaFin = 19
        ),

        // ---------------- GERIATRÍA (2 Médicos) ----------------
        Medico(
            id = "ger-001",
            nombre = "Dr. Roberto Aguilar",
            especialidad = "Geriatría",
            subespecialidad = "Neurogeriatría y Demencias",
            licencia = "MSP-GE-99821",
            clinica = "Centro Geriátrico San Juan",
            direccion = "Calle San Juan N14-22, Quito",
            pisoYHabitacion = "Planta Baja • Consultorio 1A",
            genero = "M",
            iniciales = "RA",
            horaInicio = 8,
            horaFin = 14
        ),
        Medico(
            id = "ger-002",
            nombre = "Dra. Carmen Silva",
            especialidad = "Geriatría",
            subespecialidad = "Cuidados Paliativos",
            licencia = "MSP-GE-77412",
            clinica = "Hospital Santa Inés",
            direccion = "Av. República del Salvador, Quito",
            pisoYHabitacion = "Piso 3 • Consultorio 314",
            genero = "F",
            iniciales = "CS",
            horaInicio = 14,
            horaFin = 20
        ),

        // ---------------- OFTALMOLOGÍA (2 Médicos) ----------------
        Medico(
            id = "oft-001",
            nombre = "Dr. Luis Mendoza",
            especialidad = "Oftalmología",
            subespecialidad = "Glaucoma y Cataratas",
            licencia = "MSP-OF-33211",
            clinica = "Clínica Santa Lucía",
            direccion = "Suiza N33-145 y Eloy Alfaro, Quito",
            pisoYHabitacion = "Piso 2 • Consultorio 208",
            genero = "M",
            iniciales = "LM",
            horaInicio = 9,
            horaFin = 17
        ),
        Medico(
            id = "oft-002",
            nombre = "Dra. Patricia León",
            especialidad = "Oftalmología",
            subespecialidad = "Oftalmología Pediátrica",
            licencia = "MSP-OF-65874",
            clinica = "Centro Médico Axxis",
            direccion = "Av. 10 de Agosto y Naciones Unidas, Quito",
            pisoYHabitacion = "Piso 5 • Consultorio 505",
            genero = "F",
            iniciales = "PL",
            horaInicio = 8,
            horaFin = 16
        ),

        // ---------------- DERMATOLOGÍA (2 Médicos) ----------------
        Medico(
            id = "der-001",
            nombre = "Dra. Verónica Paz",
            especialidad = "Dermatología",
            subespecialidad = "Dermatología Estética y Láser",
            licencia = "MSP-DE-22100",
            clinica = "DermaSalud",
            direccion = "Av. Shyris y Suecia, Edificio Renazzo, Quito",
            pisoYHabitacion = "Piso 8 • Oficina 82",
            genero = "F",
            iniciales = "VP",
            horaInicio = 10,
            horaFin = 19
        ),
        Medico(
            id = "der-002",
            nombre = "Dr. Diego Herrera",
            especialidad = "Dermatología",
            subespecialidad = "Oncología Cutánea",
            licencia = "MSP-DE-11985",
            clinica = "Hospital Vozandes",
            direccion = "Villalengua Oe2-37 y 10 de Agosto, Quito",
            pisoYHabitacion = "Piso 1 • Consultorio 15",
            genero = "M",
            iniciales = "DH",
            horaInicio = 7,
            horaFin = 14
        ),

        // ---------------- TRAUMATOLOGÍA (2 Médicos) ----------------
        Medico(
            id = "tra-001",
            nombre = "Dr. Fernando Castro",
            especialidad = "Traumatología",
            subespecialidad = "Cirugía Artroscópica y Deportiva",
            licencia = "MSP-TR-88541",
            clinica = "Hospital Metropolitano",
            direccion = "Av. Mariana de Jesús, Quito",
            pisoYHabitacion = "Piso 1 • Área de Traumatología",
            genero = "M",
            iniciales = "FC",
            horaInicio = 8,
            horaFin = 18
        ),
        Medico(
            id = "tra-002",
            nombre = "Dra. Gabriela Robles",
            especialidad = "Traumatología",
            subespecialidad = "Traumatología Infantil",
            licencia = "MSP-TR-44758",
            clinica = "Clínica Pasteur",
            direccion = "Av. Eloy Alfaro y Alemania, Quito",
            pisoYHabitacion = "Piso 3 • Consultorio 310",
            genero = "F",
            iniciales = "GR",
            horaInicio = 13,
            horaFin = 20
        )
    )
}
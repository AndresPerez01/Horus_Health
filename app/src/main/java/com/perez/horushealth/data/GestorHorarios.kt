package com.perez.horushealth.utils
// OJO (dato para la defensa): el archivo está físicamente en la carpeta /data,
// pero su package dice "utils". Funciona igual, pero no coinciden carpeta y paquete.

import com.perez.horushealth.data.MedicoEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/*
 * ============================================================================
 *  GENERADOR DE TURNOS  (la "lógica de negocio" del agendamiento)
 * ============================================================================
 *  "object" = Singleton: se usa como GestorHorarios.obtenerHorariosDisponibles(...)
 *
 *  Su trabajo: dado un médico y una fecha, calcular a qué horas se puede atender.
 *  Lo usa Step2Activity (asignación automática).
 *  Step3Activity hace un cálculo parecido pero además descarta las horas ya ocupadas.
 * ============================================================================
 */
object GestorHorarios {

    /**
     * Devuelve la lista de horas libres de un médico en una fecha.
     *
     * REGLAS DE NEGOCIO que aplica:
     *   1. Solo genera turnos dentro de la jornada del médico (horaInicio..horaFin).
     *   2. Los turnos son de 1 hora en punto (08:00, 09:00, ...).
     *   3. Si la cita es para HOY, exige mínimo 2 horas de anticipación.
     *   4. No permite fechas pasadas.
     */
    fun obtenerHorariosDisponibles(medico: MedicoEntity, fechaSeleccionada: LocalDate): List<LocalTime> {
        val turnosDisponibles = mutableListOf<LocalTime>()

        val ahora = LocalDateTime.now()
        val limiteHoraMinima = ahora.plusHours(2)  // REGLA: no se agenda con menos de 2h de anticipación

        // "until" recorre desde horaInicio hasta horaFin-1 (el último turno termina a horaFin)
        for (hora in medico.horaInicio until medico.horaFin) {
            val horaDelTurno = LocalTime.of(hora, 0)                              // Ej: 09:00
            val fechaHoraDelTurno = LocalDateTime.of(fechaSeleccionada, horaDelTurno) // Ej: 2026-07-16 09:00

            if (fechaSeleccionada == LocalDate.now()) {
                // CASO HOY: solo turnos que estén más allá del límite de 2 horas
                if (fechaHoraDelTurno.isAfter(limiteHoraMinima)) {
                    turnosDisponibles.add(horaDelTurno)
                }
            } else if (fechaSeleccionada.isAfter(LocalDate.now())) {
                // CASO FUTURO: todos los turnos de la jornada valen
                turnosDisponibles.add(horaDelTurno)
            }
            // CASO PASADO: no entra a ningún if -> no se agrega nada (no se agenda hacia atrás)
        }
        return turnosDisponibles
    }
}

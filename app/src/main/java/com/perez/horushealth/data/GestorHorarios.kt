package com.perez.horushealth.utils

import com.tu_paquete.horushealth.Medico // Importa tu clase Medico
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

object GestorHorarios {

    fun obtenerHorariosDisponibles(medico: Medico, fechaSeleccionada: LocalDate): List<LocalTime> {
        val turnosDisponibles = mutableListOf<LocalTime>()
        val ahora = LocalDateTime.now()
        // Aquí aplicamos la regla estricta: mínimo 2 horas de anticipación
        val limiteHoraMinima = ahora.plusHours(2)

        // Recorremos desde que el médico entra hasta que sale
        for (hora in medico.horaInicio until medico.horaFin) {
            val horaDelTurno = LocalTime.of(hora, 0) // Turnos exactos (ej. 8:00, 9:00)
            val fechaHoraDelTurno = LocalDateTime.of(fechaSeleccionada, horaDelTurno)

            // Validamos si la cita es para HOY
            if (fechaSeleccionada == LocalDate.now()) {
                // Solo agrega el turno si es DESPUÉS del límite de 2 horas
                if (fechaHoraDelTurno.isAfter(limiteHoraMinima)) {
                    turnosDisponibles.add(horaDelTurno)
                }
            }
            // Si la cita es para un DÍA FUTURO, todos los turnos están disponibles
            else if (fechaSeleccionada.isAfter(LocalDate.now())) {
                turnosDisponibles.add(horaDelTurno)
            }
        }

        return turnosDisponibles
    }
}
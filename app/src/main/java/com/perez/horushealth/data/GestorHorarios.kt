package com.perez.horushealth.utils

import com.perez.horushealth.data.MedicoEntity // 🔥 Usamos la entidad de Room
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

object GestorHorarios {

    fun obtenerHorariosDisponibles(medico: MedicoEntity, fechaSeleccionada: LocalDate): List<LocalTime> {
        val turnosDisponibles = mutableListOf<LocalTime>()
        val ahora = LocalDateTime.now()
        val limiteHoraMinima = ahora.plusHours(2)

        for (hora in medico.horaInicio until medico.horaFin) {
            val horaDelTurno = LocalTime.of(hora, 0)
            val fechaHoraDelTurno = LocalDateTime.of(fechaSeleccionada, horaDelTurno)

            if (fechaSeleccionada == LocalDate.now()) {
                if (fechaHoraDelTurno.isAfter(limiteHoraMinima)) {
                    turnosDisponibles.add(horaDelTurno)
                }
            } else if (fechaSeleccionada.isAfter(LocalDate.now())) {
                turnosDisponibles.add(horaDelTurno)
            }
        }
        return turnosDisponibles
    }
}
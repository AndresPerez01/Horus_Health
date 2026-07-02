package com.perez.horushealth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.perez.horushealth.ui.theme.HorusHealthTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

private enum class MainScreen {
    HOME,
    APPOINTMENTS,
    PROFILE,
    SPECIALTY,
    DOCTOR_CHOICE,
    DOCTOR_LIST,
    AUTO_ASSIGN,
    DATE_TIME,
    SUMMARY,
    CONFIRMATION
}

private data class DayOption(
    val dateLabel: String,
    val shortLabel: String
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocalStorage.ensureSeedData(this)

        val sessionUser = LocalStorage.getSessionUser(this)
        if (sessionUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContent {
            HorusHealthTheme(darkTheme = false, dynamicColor = false) {
                AppointmentsApp(
                    currentUser = sessionUser,
                    onLogout = {
                        LocalStorage.logout(this)
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    },
                    loadAppointments = { LocalStorage.loadAppointments(this) },
                    saveAppointment = { LocalStorage.saveAppointment(this, it) },
                    cancelAppointment = { LocalStorage.cancelAppointment(this, it) },
                    deleteAppointment = { LocalStorage.deleteAppointment(this, it) }
                )
            }
        }
    }
}

@Composable
private fun AppointmentsApp(
    currentUser: UserProfile,
    onLogout: () -> Unit,
    loadAppointments: () -> List<Appointment>,
    saveAppointment: (Appointment) -> Unit,
    cancelAppointment: (String) -> Unit,
    deleteAppointment: (String) -> Unit
) {
    val appointments = remember {
        mutableStateListOf<Appointment>().apply {
            addAll(loadAppointments())
        }
    }
    fun refreshAppointments() {
        appointments.clear()
        appointments.addAll(loadAppointments())
    }

    var currentScreen by remember { mutableStateOf(MainScreen.HOME) }
    var selectedSpecialty by remember { mutableStateOf("Medicina General") }
    var selectedDoctor by remember { mutableStateOf<Doctor?>(null) }
    var selectedDate by remember { mutableStateOf<DayOption?>(null) }
    var selectedTime by remember { mutableStateOf("09:00") }
    var confirmedAppointment by remember { mutableStateOf<Appointment?>(null) }

    val availableDates = remember { generateNextDates() }
    val timeSlots = remember { listOf("08:00", "09:00", "10:00", "11:00", "14:00", "15:00") }
    val rootScreens = setOf(MainScreen.HOME, MainScreen.APPOINTMENTS, MainScreen.PROFILE)

    Scaffold(
        topBar = {
            AppTopBar(
                screen = currentScreen,
                userName = currentUser.name,
                canGoBack = currentScreen !in rootScreens,
                onBack = {
                    currentScreen = when (currentScreen) {
                        MainScreen.SPECIALTY -> MainScreen.HOME
                        MainScreen.DOCTOR_CHOICE -> MainScreen.SPECIALTY
                        MainScreen.DOCTOR_LIST, MainScreen.AUTO_ASSIGN -> MainScreen.DOCTOR_CHOICE
                        MainScreen.DATE_TIME -> {
                            if (selectedDoctor == null) MainScreen.AUTO_ASSIGN else MainScreen.DOCTOR_LIST
                        }
                        MainScreen.SUMMARY -> MainScreen.DATE_TIME
                        MainScreen.CONFIRMATION -> MainScreen.HOME
                        else -> MainScreen.HOME
                    }
                }
            )
        },
        bottomBar = {
            if (currentScreen in rootScreens) {
                NavigationBar {
                    listOf(
                        Triple(MainScreen.HOME, "Inicio", Icons.Filled.Home),
                        Triple(MainScreen.APPOINTMENTS, "Citas", Icons.Filled.CalendarMonth),
                        Triple(MainScreen.PROFILE, "Perfil", Icons.Filled.Person)
                    ).forEach { (destination, label, icon) ->
                        NavigationBarItem(
                            selected = currentScreen == destination,
                            onClick = { currentScreen = destination },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF0B2458),
                                selectedTextColor = Color(0xFF0B2458),
                                unselectedIconColor = Color(0xFF344054),
                                unselectedTextColor = Color(0xFF344054),
                                indicatorColor = Color(0xFFD9E5FF)
                            ),
                            icon = { Icon(imageVector = icon, contentDescription = label) },
                            label = { Text(label, fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFFF7F9FC)
    ) { innerPadding ->
        when (currentScreen) {
            MainScreen.HOME -> HomeScreen(
                userName = currentUser.name,
                onScheduleClick = {
                    selectedDoctor = null
                    selectedDate = availableDates.firstOrNull()
                    selectedTime = "09:00"
                    currentScreen = MainScreen.SPECIALTY
                },
                onAppointmentsClick = { currentScreen = MainScreen.APPOINTMENTS },
                onProfileClick = { currentScreen = MainScreen.PROFILE },
                modifier = Modifier.padding(innerPadding)
            )

            MainScreen.APPOINTMENTS -> AppointmentsScreen(
                appointments = appointments,
                onCancelAppointment = {
                    cancelAppointment(it)
                    refreshAppointments()
                },
                onDeleteHistoryAppointment = {
                    deleteAppointment(it)
                    refreshAppointments()
                },
                onGoHome = { currentScreen = MainScreen.HOME },
                modifier = Modifier.padding(innerPadding)
            )

            MainScreen.PROFILE -> ProfileScreen(
                currentUser = currentUser,
                onLogout = onLogout,
                modifier = Modifier.padding(innerPadding)
            )

            MainScreen.SPECIALTY -> SpecialtyScreen(
                selectedSpecialty = selectedSpecialty,
                onSelectSpecialty = { selectedSpecialty = it },
                onContinue = {
                    selectedDoctor = null
                    currentScreen = MainScreen.DOCTOR_CHOICE
                },
                modifier = Modifier.padding(innerPadding)
            )

            MainScreen.DOCTOR_CHOICE -> DoctorChoiceScreen(
                selectedSpecialty = selectedSpecialty,
                onChooseDoctor = { currentScreen = MainScreen.DOCTOR_LIST },
                onAutoAssign = {
                    selectedDoctor = LocalStorage.getBestDoctorForSpecialty(selectedSpecialty)
                    currentScreen = MainScreen.AUTO_ASSIGN
                },
                modifier = Modifier.padding(innerPadding)
            )

            MainScreen.DOCTOR_LIST -> DoctorListScreen(
                specialty = selectedSpecialty,
                doctors = LocalStorage.getDoctorsForSpecialty(selectedSpecialty),
                selectedDoctor = selectedDoctor,
                onSelectDoctor = {
                    selectedDoctor = it
                    selectedDate = availableDates.firstOrNull()
                    currentScreen = MainScreen.DATE_TIME
                },
                modifier = Modifier.padding(innerPadding)
            )

            MainScreen.AUTO_ASSIGN -> AutoAssignedScreen(
                specialty = selectedSpecialty,
                doctor = selectedDoctor,
                onContinue = {
                    selectedDate = availableDates.firstOrNull()
                    currentScreen = MainScreen.DATE_TIME
                },
                modifier = Modifier.padding(innerPadding)
            )

            MainScreen.DATE_TIME -> DateTimeScreen(
                availableDates = availableDates,
                selectedDate = selectedDate,
                selectedTime = selectedTime,
                timeSlots = timeSlots,
                onSelectDate = { selectedDate = it },
                onSelectTime = { selectedTime = it },
                onContinue = { currentScreen = MainScreen.SUMMARY },
                modifier = Modifier.padding(innerPadding)
            )

            MainScreen.SUMMARY -> SummaryScreen(
                currentUser = currentUser,
                specialty = selectedSpecialty,
                doctorName = selectedDoctor?.name ?: "Por asignar",
                dateLabel = selectedDate?.dateLabel.orEmpty(),
                time = selectedTime,
                onEdit = { currentScreen = MainScreen.DATE_TIME },
                onConfirm = {
                    val appointment = Appointment(
                        id = UUID.randomUUID().toString(),
                        specialty = selectedSpecialty,
                        doctorName = selectedDoctor?.name ?: "Por asignar",
                        dateLabel = selectedDate?.dateLabel.orEmpty(),
                        time = selectedTime,
                        status = "upcoming"
                    )
                    saveAppointment(appointment)
                    confirmedAppointment = appointment
                    refreshAppointments()
                    currentScreen = MainScreen.CONFIRMATION
                },
                modifier = Modifier.padding(innerPadding)
            )

            MainScreen.CONFIRMATION -> ConfirmationScreen(
                appointment = confirmedAppointment,
                onGoHome = { currentScreen = MainScreen.HOME },
                onSeeAppointments = { currentScreen = MainScreen.APPOINTMENTS },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun AppTopBar(
    screen: MainScreen,
    userName: String,
    canGoBack: Boolean,
    onBack: () -> Unit
) {
    Surface(color = Color(0xFF0B2458), tonalElevation = 0.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (canGoBack) {
                TextButton(onClick = onBack) {
                    Text("< Volver", color = Color.White)
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0x1FFFFFFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.firstOrNull()?.uppercase() ?: "U",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                horizontalAlignment = if (canGoBack) Alignment.CenterHorizontally else Alignment.Start
            ) {
                Text(
                    text = screenTitle(screen),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text = screenSubtitle(screen, userName),
                    color = Color(0xFFD7E3FF),
                    fontSize = 12.sp
                )
            }

            if (canGoBack) {
                Spacer(modifier = Modifier.width(40.dp))
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0x1FFFFFFF))
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(
    userName: String,
    onScheduleClick: () -> Unit,
    onAppointmentsClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B4FD8)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Hola, ${userName.substringBefore(" ")}",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Necesitas una cita?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                    Text(
                        text = "Agenda rapido y sin complicaciones siguiendo un paso a paso claro.",
                        color = Color(0xFFE8EEFF),
                        modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
                    )
                    Button(onClick = onScheduleClick) {
                        Text("+ Agendar cita")
                    }
                }
            }
        }

        item {
            SectionTitle("Accesos rapidos")
        }

        item {
            QuickActionCard(
                title = "Mis citas",
                subtitle = "Ver, confirmar y cancelar citas",
                onClick = onAppointmentsClick
            )
        }

        item {
            QuickActionCard(
                title = "Mi perfil",
                subtitle = "Consultar datos personales y cerrar sesion",
                onClick = onProfileClick
            )
        }
    }
}

@Composable
private fun AppointmentsScreen(
    appointments: List<Appointment>,
    onCancelAppointment: (String) -> Unit,
    onDeleteHistoryAppointment: (String) -> Unit,
    onGoHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val upcoming = appointments.filter { it.status == "upcoming" }
    val history = appointments.filter { it.status != "upcoming" }
    var appointmentToCancel by remember { mutableStateOf<Appointment?>(null) }
    var appointmentToDelete by remember { mutableStateOf<Appointment?>(null) }

    if (appointmentToCancel != null) {
        AlertDialog(
            onDismissRequest = { appointmentToCancel = null },
            title = { Text("Confirmar cancelación") },
            text = {
                Text(
                    "Vas a cancelar la cita de ${appointmentToCancel?.specialty} con ${appointmentToCancel?.doctorName}. Esta acción la moverá al historial."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        appointmentToCancel?.let { onCancelAppointment(it.id) }
                        appointmentToCancel = null
                    }
                ) {
                    Text("Sí, cancelar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { appointmentToCancel = null }) {
                    Text("No, conservar")
                }
            }
        )
    }

    if (appointmentToDelete != null) {
        AlertDialog(
            onDismissRequest = { appointmentToDelete = null },
            title = { Text("Confirmar borrado") },
            text = {
                Text(
                    "Vas a eliminar este registro del historial. Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        appointmentToDelete?.let { onDeleteHistoryAppointment(it.id) }
                        appointmentToDelete = null
                    }
                ) {
                    Text("Sí, borrar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { appointmentToDelete = null }) {
                    Text("No, conservar")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionTitle("Proximas") }

        if (upcoming.isEmpty()) {
            item {
                InfoCard(
                    title = "No tienes citas proximas",
                    body = "Cuando agendes una nueva cita, aparecera aqui."
                )
            }
        } else {
            items(upcoming) { appointment ->
                AppointmentCard(
                    appointment = appointment,
                    actionLabel = "Cancelar cita",
                    onAction = { appointmentToCancel = appointment }
                )
            }
        }

        item { SectionTitle("Historial") }

        if (history.isEmpty()) {
            item {
                InfoCard(
                    title = "Todavia no hay historial",
                    body = "Las citas realizadas o canceladas se veran en esta seccion."
                )
            }
        } else {
            items(history) { appointment ->
                AppointmentCard(
                    appointment = appointment,
                    actionLabel = "Borrar del historial",
                    onAction = { appointmentToDelete = appointment }
                )
            }
        }

        item {
            OutlinedButton(
                onClick = onGoHome,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver al inicio")
            }
        }
    }
}

@Composable
private fun ProfileScreen(
    currentUser: UserProfile,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            InfoCard(
                title = currentUser.name,
                body = "Este perfil se usa para completar automaticamente el resumen de la cita."
            )
        }
        item { ProfileRow("Correo", currentUser.email) }
        item { ProfileRow("Telefono", currentUser.phone) }
        item { ProfileRow("Fecha de nacimiento", currentUser.birthDate) }
        item {
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar sesion")
            }
        }
    }
}

@Composable
private fun SpecialtyScreen(
    selectedSpecialty: String,
    onSelectSpecialty: (String) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val specialties = listOf(
        "Medicina General" to "Consulta general",
        "Cardiologia" to "Corazon y circulacion",
        "Pediatria" to "Ninos hasta 15 anos",
        "Traumatologia" to "Huesos y articulaciones",
        "Oftalmologia" to "Vision y ojos"
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { StepHeader("Paso 1 de 4", "Que especialidad necesitas?") }
        items(specialties) { (title, subtitle) ->
            SelectionCard(
                title = title,
                subtitle = subtitle,
                selected = selectedSpecialty == title,
                onClick = { onSelectSpecialty(title) }
            )
        }
        item {
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continuar")
            }
        }
    }
}

@Composable
private fun DoctorChoiceScreen(
    selectedSpecialty: String,
    onChooseDoctor: () -> Unit,
    onAutoAssign: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { StepHeader("Paso 2 de 4", "Como prefieres continuar?") }
        item {
            InfoCard(
                title = selectedSpecialty,
                body = "Puedes escoger un profesional manualmente o dejar que el sistema elija el mas disponible."
            )
        }
        item {
            SelectionCard(
                title = "Elegir mi medico",
                subtitle = "Ver disponibilidad y perfil",
                selected = true,
                onClick = onChooseDoctor
            )
        }
        item {
            SelectionCard(
                title = "Asignar automaticamente",
                subtitle = "Es la ruta mas rapida para agendar",
                selected = false,
                onClick = onAutoAssign
            )
        }
    }
}

@Composable
private fun DoctorListScreen(
    specialty: String,
    doctors: List<Doctor>,
    selectedDoctor: Doctor?,
    onSelectDoctor: (Doctor) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { StepHeader("Paso 2 de 4", "Medicos disponibles para $specialty") }
        items(doctors) { doctor ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedDoctor == doctor) Color(0xFFEEF2FF) else Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(doctor.name, fontWeight = FontWeight.Bold)
                    Text(doctor.schedule, color = Color(0xFF667085), modifier = Modifier.padding(top = 4.dp))
                    Text("Calificacion: ${doctor.rating}", color = Color(0xFF667085), modifier = Modifier.padding(top = 4.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onSelectDoctor(doctor) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Elegir")
                    }
                }
            }
        }
    }
}

@Composable
private fun AutoAssignedScreen(
    specialty: String,
    doctor: Doctor?,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { StepHeader("Paso 2 de 4", "Medico asignado") }
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B4FD8))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Asignacion automatica lista", color = Color.White, fontWeight = FontWeight.Bold)
                    Text(
                        text = doctor?.name ?: "Sin medico disponible",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = "$specialty · calificacion ${doctor?.rating ?: "--"}",
                        color = Color(0xFFE8EEFF),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
        item {
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                enabled = doctor != null
            ) {
                Text("Confirmar y elegir fecha")
            }
        }
    }
}

@Composable
private fun DateTimeScreen(
    availableDates: List<DayOption>,
    selectedDate: DayOption?,
    selectedTime: String,
    timeSlots: List<String>,
    onSelectDate: (DayOption) -> Unit,
    onSelectTime: (String) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { StepHeader("Paso 3 de 4", "Elige fecha y hora") }
        item { SectionTitle("Fechas disponibles") }
        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableDates.forEach { option ->
                    ChipButton(
                        text = option.shortLabel,
                        selected = selectedDate == option,
                        onClick = { onSelectDate(option) }
                    )
                }
            }
        }
        item {
            InfoCard(
                title = selectedDate?.dateLabel ?: "Selecciona una fecha",
                body = "Horarios disponibles para continuar con la reserva."
            )
        }
        item { SectionTitle("Horarios") }
        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                timeSlots.forEach { slot ->
                    ChipButton(
                        text = slot,
                        selected = selectedTime == slot,
                        onClick = { onSelectTime(slot) },
                        enabled = slot != "11:00"
                    )
                }
            }
        }
        item {
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedDate != null
            ) {
                Text("Continuar")
            }
        }
    }
}

@Composable
private fun SummaryScreen(
    currentUser: UserProfile,
    specialty: String,
    doctorName: String,
    dateLabel: String,
    time: String,
    onEdit: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { StepHeader("Paso 4 de 4", "Revisa tu cita") }
        item {
            SummaryCard(
                title = "Datos de la cita",
                rows = listOf(
                    "Especialidad" to specialty,
                    "Medico" to doctorName,
                    "Fecha" to dateLabel,
                    "Hora" to time
                )
            )
        }
        item {
            SummaryCard(
                title = "Tus datos",
                rows = listOf(
                    "Nombre" to currentUser.name,
                    "Correo" to currentUser.email,
                    "Telefono" to currentUser.phone
                )
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Editar")
                }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Confirmar cita")
                }
            }
        }
    }
}

@Composable
private fun ConfirmationScreen(
    appointment: Appointment?,
    onGoHome: () -> Unit,
    onSeeAppointments: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFFDFF6E5)),
            contentAlignment = Alignment.Center
        ) {
            Text("OK", color = Color(0xFF157347), fontWeight = FontWeight.Bold)
        }
        Text(
            text = "Cita agendada",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Tu cita quedo registrada localmente en la aplicacion.",
            textAlign = TextAlign.Center,
            color = Color(0xFF667085)
        )
        if (appointment != null) {
            SummaryCard(
                title = "Resumen final",
                rows = listOf(
                    "Medico" to appointment.doctorName,
                    "Fecha" to appointment.dateLabel,
                    "Hora" to appointment.time
                )
            )
        }
        Button(
            onClick = onSeeAppointments,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver mis citas")
        }
        OutlinedButton(
            onClick = onGoHome,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al inicio")
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = Color(0xFF1B4FD8),
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp
    )
}

@Composable
private fun StepHeader(step: String, title: String) {
    Column {
        Text(step, color = Color(0xFF667085), fontSize = 12.sp)
        Text(title, fontWeight = FontWeight.Bold, fontSize = 22.sp)
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color(0xFF667085), modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun SelectionCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFFEEF2FF) else Color.White
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color(0xFF667085), modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun AppointmentCard(
    appointment: Appointment,
    actionLabel: String?,
    onAction: (() -> Unit)?
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(appointment.specialty, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(appointment.doctorName, color = Color(0xFF344054), modifier = Modifier.padding(top = 6.dp))
            Text(
                "${appointment.dateLabel} · ${appointment.time}",
                color = Color(0xFF667085),
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                statusLabel(appointment.status),
                color = when (appointment.status) {
                    "upcoming" -> Color(0xFF1B4FD8)
                    "done" -> Color(0xFF667085)
                    else -> Color(0xFFB42318)
                },
                modifier = Modifier.padding(top = 8.dp)
            )
            if (actionLabel != null && onAction != null) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = onAction, modifier = Modifier.fillMaxWidth()) {
                    Text(actionLabel)
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    rows: List<Pair<String, String>>
) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF667085))
            Spacer(modifier = Modifier.height(8.dp))
            rows.forEachIndexed { index, (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(label, color = Color(0xFF667085))
                    Text(
                        value,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (index != rows.lastIndex) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    body: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(body, color = Color(0xFF667085), modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun ChipButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Surface(
        color = when {
            !enabled -> Color(0xFFE4E7EC)
            selected -> Color(0xFF1B4FD8)
            else -> Color.White
        },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick),
        shadowElevation = 1.dp
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else if (enabled) Color(0xFF344054) else Color(0xFF98A2B3),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, color = Color(0xFF667085), fontSize = 12.sp)
            Text(value, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

private fun screenTitle(screen: MainScreen): String = when (screen) {
    MainScreen.HOME -> "Citas Medicas"
    MainScreen.APPOINTMENTS -> "Mis Citas"
    MainScreen.PROFILE -> "Mi Perfil"
    MainScreen.SPECIALTY -> "Tipo de atencion"
    MainScreen.DOCTOR_CHOICE -> "Elegir medico"
    MainScreen.DOCTOR_LIST -> "Medicos disponibles"
    MainScreen.AUTO_ASSIGN -> "Medico asignado"
    MainScreen.DATE_TIME -> "Fecha y hora"
    MainScreen.SUMMARY -> "Revisa tu cita"
    MainScreen.CONFIRMATION -> "Listo"
}

private fun screenSubtitle(screen: MainScreen, userName: String): String = when (screen) {
    MainScreen.HOME -> "Hola, ${userName.substringBefore(" ")}"
    MainScreen.APPOINTMENTS -> "Gestiona tus citas activas e historial"
    MainScreen.PROFILE -> "Consulta tus datos y cierra sesion"
    MainScreen.SPECIALTY -> "Paso 1 de 4"
    MainScreen.DOCTOR_CHOICE -> "Paso 2 de 4"
    MainScreen.DOCTOR_LIST -> "Paso 2 de 4"
    MainScreen.AUTO_ASSIGN -> "Paso 2 de 4"
    MainScreen.DATE_TIME -> "Paso 3 de 4"
    MainScreen.SUMMARY -> "Paso 4 de 4"
    MainScreen.CONFIRMATION -> "Confirmacion final de la reserva"
}

private fun statusLabel(status: String): String = when (status) {
    "upcoming" -> "Proxima"
    "done" -> "Realizada"
    "cancelled" -> "Cancelada"
    else -> status
}

private fun generateNextDates(): List<DayOption> {
    val spanishLocale = Locale.forLanguageTag("es-ES")
    val dayFormat = SimpleDateFormat("dd MMM", spanishLocale)
    val fullFormat = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", spanishLocale)
    val calendar = Calendar.getInstance()
    val options = mutableListOf<DayOption>()

    while (options.size < 8) {
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        if (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {
            options.add(
                DayOption(
                    dateLabel = fullFormat.format(calendar.time),
                    shortLabel = dayFormat.format(calendar.time)
                )
            )
        }
        calendar.add(Calendar.DAY_OF_MONTH, 1)
    }

    return options
}
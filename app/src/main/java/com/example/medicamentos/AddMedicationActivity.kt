package com.example.medicamentos

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.medicamentos.data.Treatment
import com.example.medicamentos.ui.theme.MedicamentosTheme
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medicamentos.data.MedicamentosApplication
import com.example.medicamentos.data.TreatmentViewModel
import com.example.medicamentos.data.TreatmentViewModelFactory

class AddMedicationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            val app = application as MedicamentosApplication
            val viewModel: TreatmentViewModel = viewModel(
                factory = TreatmentViewModelFactory(app.database.treatmentDao(), app)
            )

            MedicamentosTheme {
                AddMedicationScreen(
                    onNavigateBack = { finish() },
                    viewModel = viewModel
                )
            }
        }
    }
}

// ----- COMPONENTES REUTILIZÁVEIS -----

@Composable
fun NumberSelector(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange = 1..100
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            OutlinedIconButton(
                onClick = { if (value > range.first) onValueChange(value - 1) },
                modifier = Modifier.size(50.dp),
                shape = CircleShape
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Diminuir $label")
            }
            Text(
                text = "$value",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f).wrapContentWidth(Alignment.CenterHorizontally)
            )
            OutlinedIconButton(
                onClick = { if (value < range.last) onValueChange(value + 1) },
                modifier = Modifier.size(50.dp),
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Aumentar $label")
            }
        }
    }
}

// ----- TELA PRINCIPAL DE CADASTRO -----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen(onNavigateBack: () -> Unit, viewModel: TreatmentViewModel) {
    val context = LocalContext.current

    var medicationName by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var durationDays by remember { mutableStateOf(7) }
    var frequencyPerDay by remember { mutableStateOf(3) }
    var intervalHours by remember { mutableStateOf(8) }

    val currentTime = remember { Calendar.getInstance() }

    var showTimePicker by remember { mutableStateOf(false) }

    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = true
    )

    val isFormValid = medicationName.isNotBlank() && dosage.isNotBlank()


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Novo Medicamento", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar") } },
                actions = {
                    TextButton(
                        onClick = {
                            val newTreatment = Treatment(
                                medicationName = medicationName,
                                dosage = dosage,
                                startDate = Date(),
                                durationInDays = durationDays,
                                frequencyPerDay = frequencyPerDay,
                                startHour = timePickerState.hour,
                                startMinute = timePickerState.minute,
                                intervalHours = intervalHours
                            )
                            viewModel.insertTreatment(newTreatment)

                            Toast.makeText(context, "$medicationName salvo!", Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        },
                        enabled = isFormValid
                    ) {
                        Text("Salvar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { innerPadding ->

        if (showTimePicker) {
            TimePickerDialog(onDismissRequest = { showTimePicker = false }) {
                TimeInput(state = timePickerState)
            }
        }

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Nome e Dosagem", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = medicationName,
                        onValueChange = { medicationName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nome do Medicamento") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Dosagem") },
                        placeholder = { Text("Ex: 1 comprimido ou 30 gotas") },
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Frequência e Horários", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    NumberSelector(label = "Duração (em dias)", value = durationDays, onValueChange = { durationDays = it })
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                    NumberSelector(label = "Vezes ao dia", value = frequencyPerDay, onValueChange = { frequencyPerDay = it }, range = 1..12)
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                    Text("Horário da Primeira Dose", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTimePicker = true }
                            .padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = String.format(Locale.getDefault(), "%02d:%02d", timePickerState.hour, timePickerState.minute),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                    NumberSelector(label = "Intervalo (em horas)", value = intervalHours, onValueChange = { intervalHours = it }, range = 1..24)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            if (frequencyPerDay > 0 && intervalHours > 0) {
                CalculatedTimesPreview(
                    startHour = timePickerState.hour,
                    startMinute = timePickerState.minute,
                    frequency = frequencyPerDay,
                    interval = intervalHours
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun CalculatedTimesPreview(startHour: Int, startMinute: Int, frequency: Int, interval: Int) {
    val times = remember(startHour, startMinute, frequency, interval) {
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        (0 until frequency).map { i ->
            // Cria uma nova instância do calendário para cada cálculo para não acumular horas
            val doseCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, startHour)
                set(Calendar.MINUTE, startMinute)
                add(Calendar.HOUR_OF_DAY, i * interval)
            }
            sdf.format(doseCalendar.time)
        }
    }

    Column {
        Text("Seus horários serão:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(times.joinToString(" - "), style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(showBackground = true)
@Composable
fun AddMedicationPreview() {

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    title: String = "Selecione o horário",
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier.wrapContentSize(),
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                Text(text = title, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 20.dp))
                content()
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismissRequest) {
                        Text("OK")
                    }
                }
            }
        }
    }
}
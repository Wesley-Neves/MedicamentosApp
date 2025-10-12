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

        val treatmentId = intent.getIntExtra("TREATMENT_ID", -1)

        val voiceExtras = Bundle().apply {
            putString("VOICE_MEDICATION_NAME", intent.getStringExtra("VOICE_MEDICATION_NAME"))
            putString("VOICE_DOSAGE", intent.getStringExtra("VOICE_DOSAGE"))
            putInt("VOICE_DURATION", intent.getIntExtra("VOICE_DURATION", -1))
            putInt("VOICE_FREQUENCY", intent.getIntExtra("VOICE_FREQUENCY", -1))
        }

        setContent {

            val app = application as MedicamentosApplication
            val viewModel: TreatmentViewModel = viewModel(
                factory = TreatmentViewModelFactory(app.database.treatmentDao(), app)
            )

            MedicamentosTheme {
                AddMedicationScreen(
                    treatmentId = treatmentId,
                    viewModel = viewModel,
                    onNavigateBack = { finish() },
                    voiceExtras = voiceExtras
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



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen(
    treatmentId: Int,
    viewModel: TreatmentViewModel,
    onNavigateBack: () -> Unit,
    voiceExtras: Bundle?
) {
    val context = LocalContext.current

    val isEditMode = treatmentId != -1

    val treatmentToEdit by viewModel.treatmentToEdit.collectAsState()

    var medicationName by remember { mutableStateOf(voiceExtras?.getString("VOICE_MEDICATION_NAME") ?: "") }
    var dosage by remember { mutableStateOf(voiceExtras?.getString("VOICE_DOSAGE") ?: "") }
    var durationDays by remember {
        // Pega o valor do bundle. Se o bundle for nulo ou a chave não existir, o valor será -1.
        val durationFromVoice = voiceExtras?.getInt("VOICE_DURATION", -1) ?: -1
        // Se o valor for válido (diferente de -1), usa ele. Senão, usa o padrão 7.
        mutableStateOf(if (durationFromVoice != -1) durationFromVoice else 7)
    }
    var frequencyPerDay by remember {
        // Mesma lógica para a frequência
        val frequencyFromVoice = voiceExtras?.getInt("VOICE_FREQUENCY", -1) ?: -1
        mutableStateOf(if (frequencyFromVoice != -1) frequencyFromVoice else 3)
    }
    var intervalHours by remember { mutableStateOf(8) }

    val currentTime = remember { Calendar.getInstance() }

    var showTimePicker by remember { mutableStateOf(false) }

    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = true
    )

    LaunchedEffect(key1 = Unit) {
        if (isEditMode) {
            viewModel.loadTreatmentById(treatmentId)
        }
    }

    // Efeito para atualizar os campos quando o tratamento for carregado
    LaunchedEffect(key1 = treatmentToEdit) {
        treatmentToEdit?.let { treatment ->
            medicationName = treatment.medicationName
            dosage = treatment.dosage
            durationDays = treatment.durationInDays
            frequencyPerDay = treatment.frequencyPerDay
            intervalHours = treatment.intervalHours
            timePickerState.hour = treatment.startHour
            timePickerState.minute = treatment.startMinute
        }
    }

    // Limpa o viewModel ao sair da tela
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearEditingTreatment()
        }
    }

    val isFormValid = medicationName.isNotBlank() && dosage.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Editar Medicamento" else "Novo Medicamento", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Voltar") } },
                actions = {
                    TextButton(
                        onClick = {
                            if (isEditMode) {
                                // Lógica de ATUALIZAR
                                val updatedTreatment = treatmentToEdit!!.copy(
                                    medicationName = medicationName,
                                    dosage = dosage,
                                    durationInDays = durationDays,
                                    frequencyPerDay = frequencyPerDay,
                                    startHour = timePickerState.hour,
                                    startMinute = timePickerState.minute,
                                    intervalHours = intervalHours
                                )
                                viewModel.updateTreatmentAndRescheduleDoses(updatedTreatment)
                                Toast.makeText(context, "$medicationName atualizado!", Toast.LENGTH_SHORT).show()
                            } else {
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
                                Toast.makeText(
                                    context,
                                    "$medicationName salvo!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
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
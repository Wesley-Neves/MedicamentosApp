package com.example.medicamentos

import android.Manifest
import android.R.attr.elevation
import android.R.attr.shape
import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.provider.Settings
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.CheckboxDefaults.colors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medicamentos.data.*
import com.example.medicamentos.ui.theme.MedicamentosTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import androidx.activity.result.ActivityResultLauncher
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

class HomeActivity : ComponentActivity() {

    private lateinit var addMedicationLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addMedicationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val app = application as MedicamentosApplication
            val viewModel: TreatmentViewModel = viewModel(
                factory = TreatmentViewModelFactory(app.database.treatmentDao(), app)
            )

            // CORREÇÃO: Sincroniza quando o usuário muda, não apenas uma vez
            val currentUser = Firebase.auth.currentUser?.uid
            var lastSyncedUser by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(currentUser) {
                if (currentUser != null && currentUser != lastSyncedUser) {
                    Log.d("HomeActivity", "Sincronizando para novo usuário: $currentUser")
                    viewModel.syncDataFromFirestore()
                    lastSyncedUser = currentUser
                }
            }
            LaunchedEffect(Unit) {
                viewModel.startRealtimeSync() // Inicia sincronização em tempo real
            }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    if (!alarmManager.canScheduleExactAlarms()) {
                        // Envia o usuário para a tela de configurações para habilitar a permissão
                        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).also { intent ->
                            startActivity(intent)
                        }
                    }
                }
            }

            MedicamentosTheme {
                HomeScreen(
                    viewModel = viewModel,
                    addMedicationLauncher = addMedicationLauncher)
            }
        }
    }
}

// ----- COMPONENTES DA UI -----

@Composable
fun MedicationCard(
    medication: MedicationDose,
    onMedicationTaken: (MedicationDose) -> Unit,
    onMedicationPostponed: (MedicationDose) -> Unit,
    onDeleteDose: (MedicationDose) -> Unit
) {

    var buttonsEnabled by remember { mutableStateOf(false) }

    // 2. Efeito que roda continuamente para verificar a hora
    LaunchedEffect(key1 = medication.id) {
        while (true) {
            try {
                // Pega a hora agendada da dose
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                val scheduledTime = Calendar.getInstance().apply {
                    val timeParts = medication.time.split(":")
                    set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                    set(Calendar.MINUTE, timeParts[1].toInt())
                    set(Calendar.SECOND, 0)
                }

                // Pega a hora atual
                val currentTime = Calendar.getInstance()

                // Calcula a diferença em minutos
                val diffInMillis = scheduledTime.timeInMillis - currentTime.timeInMillis
                val diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)

                // 3. Define a regra: habilitar 10 minutos antes e até 4 horas depois
                buttonsEnabled = diffInMinutes <= 10 && diffInMinutes > -240 // (de +10 min até -240 min)

            } catch (e: Exception) {
                // Em caso de erro de parsing de data, mantém os botões desabilitados
                buttonsEnabled = false
            }

            // 4. Pausa por um minuto antes de verificar novamente
            delay(60000) // 60000 milissegundos = 1 minuto
        }
    }

    val cardColor = if (medication.status == MedicationStatus.PENDING) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer
    val contentColor = if (medication.status == MedicationStatus.PENDING) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer
    val iconColor = if (medication.status == MedicationStatus.PENDING) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer

    var showOptionsDialog by remember { mutableStateOf(false) }

    if (showOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showOptionsDialog = false },
            title = { Text(medication.medicationName) },
            text = { Text("Deseja excluir esta dose específica?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteDose(medication)
                        showOptionsDialog = false
                    }
                ) { Text("Excluir", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showOptionsDialog = false }) { Text("Cancelar") }
            }
        )
    }


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                // Só mostra o diálogo se o medicamento estiver pendente
                if (medication.status == MedicationStatus.PENDING) {
                    showOptionsDialog = true
                }
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (medication.status == MedicationStatus.PENDING) 1.dp else 0.dp)
    ) {
        Column(modifier = Modifier.padding(all = 20.dp).fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Medication, contentDescription = "Ícone de medicamento", modifier = Modifier.size(44.dp), tint = iconColor)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = medication.medicationName, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = contentColor)
                        Text(text = medication.dosage, fontSize = 16.sp, color = contentColor.copy(alpha = 0.8f))
                    }
                }
                if (medication.status == MedicationStatus.TAKEN) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Medicamento tomado", modifier = Modifier.size(50.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }
            if (medication.status == MedicationStatus.PENDING) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { onMedicationPostponed(medication) },
                        enabled = buttonsEnabled && medication.postponeCount < 2
                    ) {
                        Icon(Icons.Default.Alarm, contentDescription = "Adiar", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Adiar 15 m", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onMedicationTaken(medication) }, shape = RoundedCornerShape(16.dp),
                        enabled = buttonsEnabled
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Tomar", modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tomar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Função para processar o comando de voz recebido
private fun processVoiceCommand(
    command: String,
    doses: List<MedicationDose>,
    onMedicationTaken: (MedicationDose) -> Unit,
    onMedicationPostponed: (MedicationDose) -> Unit,
    context: android.content.Context,
    addMedicationLauncher: ActivityResultLauncher<Intent>
) {
    val lowerCaseCommand = command.lowercase(Locale.getDefault())
    var commandProcessed = false

    for (medication in doses.filter { it.status == MedicationStatus.PENDING }) {
        if (lowerCaseCommand.contains(medication.medicationName.lowercase(Locale.getDefault()))) {
            when {
                lowerCaseCommand.contains("confirmar") || lowerCaseCommand.contains("já tomei") || lowerCaseCommand.contains("tomar") -> {
                    onMedicationTaken(medication)
                    commandProcessed = true
                    break
                }
                lowerCaseCommand.contains("adiar") || lowerCaseCommand.contains("adiar") -> {
                    onMedicationPostponed(medication)
                    commandProcessed = true
                    break
                }
            }
        }
    }

    if (commandProcessed) return

    // --- NOVA LÓGICA DE "ADICIONAR" ---
    if (lowerCaseCommand.startsWith("adicionar") || lowerCaseCommand.startsWith("cadastrar")) {
        // Remove a palavra-chave inicial para analisar o resto do comando
        val details = lowerCaseCommand.removePrefix("adicionar ").removePrefix("cadastrar ")

        // Expressões regulares para extrair as informações
        val dosageRegex = "(\\d+)\\s*(mg|ml|gotas|comprimidos?)".toRegex()
        val durationRegex = "(\\d+)\\s*dias?".toRegex()
        val frequencyRegex = "(\\d+)\\s*vezes?".toRegex()

        val dosageMatch = dosageRegex.find(details)
        val durationMatch = durationRegex.find(details)
        val frequencyMatch = frequencyRegex.find(details)

        // Extrai os valores encontrados
        val dosage = dosageMatch?.value ?: ""
        val duration = durationMatch?.groupValues?.get(1)?.toIntOrNull() ?: 7 // Padrão de 7 dias
        val frequency = frequencyMatch?.groupValues?.get(1)?.toIntOrNull() ?: 3 // Padrão de 3 vezes

        // O que sobrar, consideramos como o nome do medicamento
        val medicationName = details
            .replace(dosageRegex, "")
            .replace(durationRegex, "")
            .replace(frequencyRegex, "")
            .replace(" por ", "")
            .replace(" ao dia", "")
            .trim()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

        // Cria o Intent para a AddMedicationActivity
        val intent = Intent(context, AddMedicationActivity::class.java).apply {
            // ✨ Envia os dados extraídos como extras ✨
            putExtra("VOICE_MEDICATION_NAME", medicationName)
            putExtra("VOICE_DOSAGE", dosage)
            putExtra("VOICE_DURATION", duration)
            putExtra("VOICE_FREQUENCY", frequency)
        }

        // Inicia a activity para confirmação do usuário
        addMedicationLauncher.launch(intent)

        commandProcessed = true
    }

    if (!commandProcessed) {
        Toast.makeText(context, "Comando não reconhecido. Tente: 'Tomar [medicamento]' ou 'Adiar [medicamento]'", Toast.LENGTH_LONG).show()
    }
}

// ----- TELA PRINCIPAL -----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: TreatmentViewModel, addMedicationLauncher: ActivityResultLauncher<Intent>) {
    val context = LocalContext.current
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayStr = sdf.format(Date())

    // A UI lê a lista de doses do dia diretamente do banco de dados (via ViewModel).
    val medicationDoses by viewModel.getDosesForDate(todayStr).collectAsState(initial = emptyList())

    val onMedicationTaken: (MedicationDose) -> Unit = { doseToUpdate ->
        viewModel.updateDose(
            doseToUpdate.copy(
                status = MedicationStatus.TAKEN,
                takenTimestamp = System.currentTimeMillis()
            )
        )
        Toast.makeText(context, "${doseToUpdate.medicationName} confirmado!", Toast.LENGTH_SHORT).show()
    }
    val onMedicationPostponed: (MedicationDose) -> Unit = { doseToUpdate ->

        if (doseToUpdate.postponeCount < 2) {
            Log.d(
                "AdiarDebug",
                "ANTES - ID: ${doseToUpdate.id}, Horário: ${doseToUpdate.time}, Medicamento: ${doseToUpdate.medicationName}"
            )

            val calendar = Calendar.getInstance()
            val timeSdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            calendar.time = timeSdf.parse(doseToUpdate.time) ?: Date()
            calendar.add(Calendar.MINUTE, 15)
            val newTime = timeSdf.format(calendar.time)

            Log.d(
                "AdiarDebug",
                "DEPOIS - ID: ${doseToUpdate.id}, Novo Horário: $newTime, Medicamento: ${doseToUpdate.medicationName}"
            )

            val updatedDose = doseToUpdate.copy(
                time = newTime,
                status = MedicationStatus.PENDING,
                postponeCount = doseToUpdate.postponeCount + 1
            )

            viewModel.updateDose(updatedDose)
            Toast.makeText(
                context,
                "${doseToUpdate.medicationName} adiado para $newTime!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(context, "Limite de adiamentos atingido para esta dose.", Toast.LENGTH_SHORT).show()
        }
    }

    val speechResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText: ArrayList<String>? = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!spokenText.isNullOrEmpty()) {
                processVoiceCommand(
                    command = spokenText[0],
                    doses = medicationDoses,
                    onMedicationTaken = onMedicationTaken,
                    onMedicationPostponed = onMedicationPostponed,
                    context = context,
                    addMedicationLauncher = addMedicationLauncher
                )

            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Fale o comando...")
            }
            speechResultLauncher.launch(intent)
        } else {
            Toast.makeText(context, "Permissão de áudio necessária.", Toast.LENGTH_SHORT).show()
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
                Toast.makeText(context, "Permissão de notificação negada.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Scaffold(
        bottomBar = {
            AppBottomNavigationBar(
                currentScreen = "Hoje",
                onNavigateToHome = {},
                onNavigateToSchedule = { context.startActivity(Intent(context, ScheduleActivity::class.java)) },
                onNavigateToProfile = { context.startActivity(Intent(context, ProfileActivity::class.java)) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { context.startActivity(Intent(context, AddMedicationActivity::class.java)) },
                shape = CircleShape,
                modifier = Modifier.size(80.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Medicamento", modifier = Modifier.size(40.dp))
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 24.dp, top = 24.dp, end = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Hoje", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                    val dateFormat = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("pt", "BR"))
                    Text(text = dateFormat.format(Date()), style = MaterialTheme.typography.titleLarge, color = Color.Gray)
                }
                FilledTonalIconButton(
                    onClick = {
                        if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO)) {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Fale o comando...")
                            }
                            speechResultLauncher.launch(intent)
                        } else {
                            permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Mic, contentDescription = "Comando de Voz", modifier = Modifier.size(36.dp))
                }
            }

            val groupedMedications = medicationDoses.sortedBy { it.time }.groupBy { it.time }

            if (medicationDoses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum medicamento para hoje.\nAdicione um no botão '+'!",
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    groupedMedications.forEach { (time, medsInTime) ->
                        item {
                            Text(
                                text = time,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }
                        items(medsInTime) { dose ->
                            MedicationCard(
                                medication = dose,
                                onMedicationTaken = onMedicationTaken,
                                onMedicationPostponed = onMedicationPostponed,
                                onDeleteDose = { viewModel.deleteDose(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MedicamentosTheme {

    }
}
package com.example.medicamentos

import android.Manifest
import android.app.Activity
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

            MedicamentosTheme {
                HomeScreen(viewModel = viewModel)
            }
        }
    }
}

// ----- COMPONENTES DA UI -----

@Composable
fun MedicationCard(
    medication: MedicationDose,
    onMedicationTaken: (MedicationDose) -> Unit,
    onMedicationPostponed: (MedicationDose) -> Unit
) {
    val cardColor = if (medication.status == MedicationStatus.PENDING) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer
    val contentColor = if (medication.status == MedicationStatus.PENDING) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer
    val iconColor = if (medication.status == MedicationStatus.PENDING) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
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
                    TextButton(onClick = { onMedicationPostponed(medication) }) {
                        Icon(Icons.Default.Alarm, contentDescription = "Adiar", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Adiar 15 m", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onMedicationTaken(medication) }, shape = RoundedCornerShape(16.dp)) {
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
    context: android.content.Context
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

    if (!commandProcessed) {
        Toast.makeText(context, "Comando não reconhecido. Tente: 'Tomar [medicamento]' ou 'Adiar [medicamento]'", Toast.LENGTH_LONG).show()
    }
}

// ----- TELA PRINCIPAL -----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: TreatmentViewModel) {
    val context = LocalContext.current
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayStr = sdf.format(Date())

    // A UI lê a lista de doses do dia diretamente do banco de dados (via ViewModel).
    val medicationDoses by viewModel.getDosesForDate(todayStr).collectAsState(initial = emptyList())

    val onMedicationTaken: (MedicationDose) -> Unit = { doseToUpdate ->
        viewModel.updateDose(doseToUpdate.copy(status = MedicationStatus.TAKEN))
        Toast.makeText(context, "${doseToUpdate.medicationName} confirmado!", Toast.LENGTH_SHORT).show()
    }
    val onMedicationPostponed: (MedicationDose) -> Unit = { doseToUpdate ->
        Log.d("AdiarDebug", "ANTES - ID: ${doseToUpdate.id}, Horário: ${doseToUpdate.time}, Medicamento: ${doseToUpdate.medicationName}")

        val calendar = Calendar.getInstance()
        val timeSdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        calendar.time = timeSdf.parse(doseToUpdate.time) ?: Date()
        calendar.add(Calendar.MINUTE, 15)
        val newTime = timeSdf.format(calendar.time)

        Log.d("AdiarDebug", "DEPOIS - ID: ${doseToUpdate.id}, Novo Horário: $newTime, Medicamento: ${doseToUpdate.medicationName}")

        val updatedDose = doseToUpdate.copy(
            time = newTime,
            status = MedicationStatus.PENDING
        )

        viewModel.updateDose(updatedDose)
        Toast.makeText(context, "${doseToUpdate.medicationName} adiado para $newTime!", Toast.LENGTH_SHORT).show()
    }

    val speechResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText: ArrayList<String>? = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!spokenText.isNullOrEmpty()) {
                processVoiceCommand(spokenText[0], medicationDoses, onMedicationTaken, onMedicationPostponed, context)
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
                                onMedicationPostponed = onMedicationPostponed
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
        HomeScreen(viewModel = viewModel())
    }
}
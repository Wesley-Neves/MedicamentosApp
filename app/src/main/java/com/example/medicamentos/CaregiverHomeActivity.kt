package com.example.medicamentos

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medicamentos.data.CaregiverViewModel
import com.example.medicamentos.data.CaregiverViewModelFactory
import com.example.medicamentos.data.MedicationDose
import com.example.medicamentos.data.MedicationStatus
import com.example.medicamentos.ui.theme.MedicamentosTheme

class CaregiverHomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(this) {
            // Desabilita o botão "voltar" do sistema nesta tela
        }

        val patientName = intent.getStringExtra("PATIENT_NAME") ?: "Paciente"
        val patientUid = intent.getStringExtra("PATIENT_UID")

        setContent {
            MedicamentosTheme {
                val viewModel: CaregiverViewModel = viewModel(factory = CaregiverViewModelFactory())

                LaunchedEffect(patientUid) {
                    if (patientUid != null) {
                        viewModel.fetchPatientData(patientUid, patientName)
                    }
                }

                CaregiverHomeScreen(viewModel = viewModel, patientUid = patientUid, patientName = patientName)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverHomeScreen(
    viewModel: CaregiverViewModel,
    patientUid: String?,
    patientName: String
) {
    val context = LocalContext.current
    val todayDoses by viewModel.doses.collectAsState()
    val isLoading by viewModel.isLoading

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = patientName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Modo Cuidador",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val intent = Intent(context, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sair do Modo Cuidador"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Button(
                onClick = {
                    val intent = Intent(context, CaregiverScheduleActivity::class.java)
                    intent.putExtra("PATIENT_NAME", patientName)
                    intent.putExtra("PATIENT_UID", patientUid)
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ListAlt,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                Text("Ver Cronograma Completo")
            }

            when {
                isLoading && todayDoses.isEmpty() -> LoadingState()
                todayDoses.isEmpty() -> EmptyState()
                else -> MedicationList(todayDoses)
            }
        }
    }
}

@Composable
private fun MedicationList(doses: List<MedicationDose>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Text(
                "Medicamentos de Hoje",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        val groupedMedications = doses.sortedBy { it.time }.groupBy { it.time }

        groupedMedications.forEach { (time, medsInTime) ->
            item {
                Column {
                    Text(
                        text = time,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                    HorizontalDivider()
                }
            }
            items(medsInTime) { medicationDose ->
                CaregiverMedicationCard(medication = medicationDose)
            }
        }
    }
}

@Composable
fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CircularProgressIndicator()
            Text(
                "Carregando medicamentos...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsOff,
                contentDescription = "Nenhum medicamento",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Nenhum medicamento para hoje",
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "O paciente não tem doses agendadas para hoje.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


// ✨ AQUI ESTÁ A MUDANÇA NA COR DO CARD ✨
@Composable
fun CaregiverMedicationCard(
    medication: MedicationDose,
    modifier: Modifier = Modifier
) {
    val isPending = medication.status == MedicationStatus.PENDING

    val statusColor = if (isPending) Color(0xFFF59E0B) else Color(0xFF16A34A)
    val statusText = if (isPending) "Pendente" else "Tomado"
    val statusIcon = if (isPending) Icons.Default.HourglassTop else Icons.Default.CheckCircle

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            // MUDANÇA: Usando a mesma cor de fundo da outra tela
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        ListItem(
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            ),
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Medication,
                    contentDescription = "Ícone de medicamento",
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            headlineContent = {
                Text(
                    text = medication.medicationName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            supportingContent = {
                Text(
                    text = medication.dosage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingContent = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = statusText,
                        tint = statusColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = statusText,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun CaregiverMedicationCardPreview() {
    MedicamentosTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CaregiverMedicationCard(
                medication = MedicationDose(
                    medicationName = "Paracetamol",
                    dosage = "500mg",
                    status = MedicationStatus.PENDING,
                    time = "08:00",
                    date = "2025-09-30"
                )
            )
            CaregiverMedicationCard(
                medication = MedicationDose(
                    medicationName = "Vitamina C",
                    dosage = "1000mg",
                    status = MedicationStatus.TAKEN,
                    time = "12:00",
                    date = "2025-09-30"
                )
            )
        }
    }
}
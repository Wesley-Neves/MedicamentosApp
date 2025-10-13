package com.example.medicamentos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medicamentos.data.CaregiverViewModel
import com.example.medicamentos.data.CaregiverViewModelFactory
import com.example.medicamentos.ui.theme.MedicamentosTheme

class CaregiverScheduleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val patientName = intent.getStringExtra("PATIENT_NAME") ?: "Paciente"
        val patientUid = intent.getStringExtra("PATIENT_UID")

        setContent {
            MedicamentosTheme {
                val viewModel: CaregiverViewModel = viewModel(factory = CaregiverViewModelFactory())

                CaregiverScheduleScreen(
                    patientName = patientName,
                    patientUid = patientUid,
                    viewModel = viewModel,
                    onNavigateBack = { finish() }
                )
            }
        }
    }
}

// ----- TELA DE CRONOGRAMA DO CUIDADOR (COM DESIGN APRIMORADO) -----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverScheduleScreen(
    patientName: String,
    patientUid: String?,
    viewModel: CaregiverViewModel,
    onNavigateBack: () -> Unit
) {
    LaunchedEffect(patientUid) {
        if (patientUid != null) {
            viewModel.fetchPatientData(patientUid, patientName)
        }
    }

    val treatments by viewModel.treatments.collectAsState()
    val isLoading by viewModel.isLoading

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Cronograma Completo",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Progresso de $patientName",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isLoading -> {
                    // Estado de Carregamento
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                treatments.isEmpty() -> {
                    // Estado Vazio
                    EmptyState(patientName)
                }
                else -> {
                    // Lista de Tratamentos
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(treatments) { treatment ->
                            TreatmentCard(treatment = treatment)
                        }
                    }
                }
            }
        }
    }
}

// ----- COMPONENTES DA TELA -----


@Composable
fun DateInfo(label: String, date: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Icons.Default.CalendarMonth,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = date,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EmptyState(patientName: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Nenhum tratamento encontrado para $patientName.",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}


// ----- PREVIEWS -----

@Preview(showBackground = true)
@Composable
fun CaregiverScheduleScreenPreview() {
    MedicamentosTheme {
        // Mock ViewModel para o preview
        val mockViewModel: CaregiverViewModel = viewModel(factory = CaregiverViewModelFactory())
        // Forçar estado de lista vazia para o preview
        // mockViewModel.treatments.value = emptyList()

        CaregiverScheduleScreen(
            patientName = "Maria da Silva",
            patientUid = "123",
            viewModel = mockViewModel,
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TreatmentCardPreview() {
    MedicamentosTheme {
    }
}
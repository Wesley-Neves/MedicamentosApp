package com.example.medicamentos

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medicamentos.data.MedicamentosApplication
import com.example.medicamentos.data.Treatment
import com.example.medicamentos.data.TreatmentViewModel
import com.example.medicamentos.data.TreatmentViewModelFactory
import com.example.medicamentos.ui.theme.MedicamentosTheme

class ScheduleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val app = application as MedicamentosApplication
            val viewModel: TreatmentViewModel = viewModel(
                factory = TreatmentViewModelFactory(app.database.treatmentDao(), app)
            )
            MedicamentosTheme {
                // A chamada correta é para o "Route", que gerencia o estado
                ScheduleRoute(viewModel = viewModel)
            }
        }
    }
}

// ----- CONTAINER INTELIGENTE (CONECTA O VIEWMODEL À UI) -----
@Composable
fun ScheduleRoute(viewModel: TreatmentViewModel) {
    val context = LocalContext.current
    val treatments by viewModel.allTreatments.collectAsState(initial = emptyList())

    ScheduleScreen(
        treatments = treatments,
        onDeleteTreatment = { viewModel.deleteTreatment(it) },
        onNavigateToHome = {
            val intent = Intent(context, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            context.startActivity(intent)
        },
        onNavigateToProfile = {
            context.startActivity(Intent(context, ProfileActivity::class.java))
        }
    )
}

// ----- TELA "BURRA" (APENAS EXIBE OS DADOS) -----
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    treatments: List<Treatment>,
    onDeleteTreatment: (Treatment) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    // 1. Divide a lista em duas: em andamento e concluídos
    val (ongoingTreatments, completedTreatments) = treatments.partition {
        it.daysCompleted < it.durationInDays
    }

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = TopAppBarDefaults.windowInsets,
                title = {
                    Column(modifier = Modifier.statusBarsPadding()) {
                        Text("Cronograma", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                        Text("O progresso dos seus tratamentos", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                    }
                }
            )
        },
        bottomBar = {
            AppBottomNavigationBar(
                currentScreen = "Cronograma",
                onNavigateToHome = onNavigateToHome,
                onNavigateToSchedule = { /* Já estamos aqui */ },
                onNavigateToProfile = onNavigateToProfile
            )
        }
    ) { innerPadding ->
        if (treatments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aguardando um novo tratamento...\n\nAdicione seu primeiro medicamento na tela 'Hoje'.",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
            ) {
                // 2. Seção "Em Andamento"
                if (ongoingTreatments.isNotEmpty()) {
                    item {
                        Text(
                            "Em Andamento",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(ongoingTreatments) { treatment ->
                        TreatmentCard(
                            treatment = treatment,
                            onDelete = { onDeleteTreatment(it) }
                        )
                    }
                }

                // 3. Seção "Concluídos"
                if (completedTreatments.isNotEmpty()) {
                    item {
                        Text(
                            "Concluídos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                        )
                    }
                    items(completedTreatments) { treatment ->
                        TreatmentCard(
                            treatment = treatment,
                            onDelete = { onDeleteTreatment(it) }
                        )
                    }
                }
            }
        }
    }
}

// ----- PREVIEW FUNCIONAL -----
@Preview(showBack

        ground = true)
@Composable
fun ScheduleScreenPreview() {
    val mockTreatments = listOf(
        Treatment(id = 1, medicationName = "Dipirona", durationInDays = 3, daysCompleted = 0),
        Treatment(id = 2, medicationName = "Amoxicilina", durationInDays = 7, daysCompleted = 7),
        Treatment(id = 3, medicationName = "Vitamina C", durationInDays = 30, daysCompleted = 15)
    )

    MedicamentosTheme {
        ScheduleScreen(
            treatments = mockTreatments,
            onDeleteTreatment = {},
            onNavigateToHome = {},
            onNavigateToProfile = {}
        )
    }
}
package com.example.medicamentos

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.medicamentos.ui.theme.MedicamentosTheme
import java.util.Date
import java.util.concurrent.TimeUnit
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.medicamentos.data.MedicamentosApplication
import com.example.medicamentos.data.TreatmentViewModel
import com.example.medicamentos.data.TreatmentViewModelFactory

class ScheduleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            val app = application as MedicamentosApplication
            val viewModel: TreatmentViewModel = viewModel(
                factory = TreatmentViewModelFactory(app.database.treatmentDao(), app)
            )

            MedicamentosTheme {
                ScheduleScreen(viewModel = viewModel)
            }
        }
    }
}
// ----- TELA PRINCIPAL DO CRONOGRAMA -----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen( viewModel: TreatmentViewModel) {
    val context = LocalContext.current

   val treatments by viewModel.allTreatments.collectAsState(initial = emptyList())


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.padding(vertical = 16.dp)) {
                        Text("Cronograma", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                        Text("O progresso dos seus tratamentos", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                    }
                }
            )
        },
        bottomBar = {
            // Usa o componente compartilhado, informando que a tela atual é "Cronograma"
            AppBottomNavigationBar(
                currentScreen = "Cronograma",
                onNavigateToHome = {
                    // Previne criar múltiplas instâncias da mesma tela
                    val intent = Intent(context, HomeActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    context.startActivity(intent)
                },
                onNavigateToSchedule = { /* Já estamos aqui */ },
                onNavigateToProfile = {
                    context.startActivity(Intent(context, ProfileActivity::class.java))
                }
            )
        }
    ) { innerPadding ->
        // Lógica para exibir a lista ou a mensagem de estado vazio
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
                items(treatments) { treatment ->
                    TreatmentCard(treatment = treatment)
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ScheduleScreenPreview() {
    MedicamentosTheme {

    }
}
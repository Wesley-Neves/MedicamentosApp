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

class ScheduleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MedicamentosTheme {
                ScheduleScreen()
            }
        }
    }
}

// ----- COMPONENTES DA UI -----

@Composable
fun TreatmentCard(treatment: Treatment) {
    // Lógica para calcular o progresso
    val progress = (treatment.daysCompleted.toFloat() / treatment.durationInDays).coerceIn(0f, 1f)
    val daysRemaining = (treatment.durationInDays - treatment.daysCompleted).coerceAtLeast(0)
    val isCompleted = daysRemaining == 0

    val cardColor = when {
        isCompleted -> Color(0xFFE8F5E9) // Verde para concluído
        else -> MaterialTheme.colorScheme.secondaryContainer // Azul claro para em andamento
    }
    val progressColor = if (isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
    val borderColor = if (isCompleted) Color(0xFF66BB6A) else Color.Transparent
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = treatment.medicationName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface // Cor de texto padrão para boa legibilidade
                )
                if (isCompleted) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Tratamento Finalizado",
                        tint = progressColor
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Tratamento de ${treatment.durationInDays} dias",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            val statusText = if (isCompleted) "Tratamento finalizado!" else "Faltam $daysRemaining dias"
            Text(statusText, style = MaterialTheme.typography.bodyMedium, color = progressColor, fontWeight = FontWeight.Bold)
        }
    }
}

// ----- TELA PRINCIPAL DO CRONOGRAMA -----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen() {
    val context = LocalContext.current

    // Os tratamentos vêm da fonte de dados central (DataManager)
    val treatments = DataManager.treatmentList

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
                onNavigateToProfile = { /* Lógica para ir ao Perfil */ }
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
        ScheduleScreen()
    }
}
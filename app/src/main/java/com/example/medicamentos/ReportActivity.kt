package com.example.medicamentos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.HighlightOff

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medicamentos.data.*
import com.example.medicamentos.ui.theme.MedicamentosTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

// 1. A Activity
class ReportActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val app = application as MedicamentosApplication
            // Usamos a factory para criar o ViewModel com o DAO
            val viewModel: ReportViewModel = viewModel(
                factory = ReportViewModelFactory(app.database.treatmentDao())
            )
            MedicamentosTheme {
                ReportScreen(
                    viewModel = viewModel,
                    onNavigateBack = { finish() }
                )
            }
        }
    }
}

data class ReportStats(
    val compliancePercentage: Double = 100.0,
    val takenCount: Int = 0,
    val missedCount: Int = 0
)

// 2. O ViewModel (bem simples)
class ReportViewModel(dao: TreatmentDao) : ViewModel() {
    private val doseHistory: Flow<List<MedicationDose>> = dao.getPastDosesHistory()

    val reportStats: Flow<ReportStats> = doseHistory.map { history ->
        if (history.isEmpty()) return@map ReportStats()

        val takenCount = history.count { it.status == MedicationStatus.TAKEN }
        val missedCount = history.count { it.status == MedicationStatus.MISSED }
        val totalPast = takenCount + missedCount

        val compliance = if (totalPast > 0) {
            (takenCount.toDouble() / totalPast.toDouble()) * 100
        } else {
            100.0 // Se não há doses passadas, a adesão é 100%
        }

        ReportStats(
            compliancePercentage = compliance,
            takenCount = takenCount,
            missedCount = missedCount
        )
    }

    // Expõe o histórico para a lista
    val historyItems = doseHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}

// 3. A Factory para o ViewModel
class ReportViewModelFactory(private val dao: TreatmentDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReportViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// 4. A UI da Tela
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(viewModel: ReportViewModel, onNavigateBack: () -> Unit) {
    val historyItems by viewModel.historyItems.collectAsState()
    val stats by viewModel.reportStats.collectAsState(initial = ReportStats())

    Scaffold(
        topBar = { TopAppBar(title = { Text("Relatório de Adesão") }, navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Voltar") } }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            ReportStatsCard(stats = stats)
            Spacer(modifier = Modifier.height(24.dp))

            Text("Histórico de Doses", style = MaterialTheme.typography.titleLarge, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))

            if (historyItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhuma dose foi confirmada ou esquecida ainda.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(historyItems) { dose ->
                        DoseHistoryItem(dose = dose)
                    }
                }
            }
        }
    }
}

@Composable
fun ReportStatsCard(stats: ReportStats) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Taxa de Adesão Geral", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${String.format("%.0f", stats.compliancePercentage)}%",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Tomadas", style = MaterialTheme.typography.labelLarge)
                    Text(stats.takenCount.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Esquecidas", style = MaterialTheme.typography.labelLarge)
                    Text(stats.missedCount.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DoseHistoryItem(dose: MedicationDose) {
    val statusColor = when (dose.status) {
        MedicationStatus.TAKEN -> MaterialTheme.colorScheme.primary
        MedicationStatus.MISSED -> MaterialTheme.colorScheme.error
        else -> Color.Gray
    }
    val statusIcon = when (dose.status) {
        MedicationStatus.TAKEN -> Icons.Default.CheckCircle
        MedicationStatus.MISSED -> Icons.Default.HighlightOff
        else -> Icons.Default.HelpOutline // Ícone para um estado inesperado
    }

    // --- LÓGICA PARA CALCULAR O ATRASO ---
    var atrasoInfo: String? = null
    if (dose.status == MedicationStatus.TAKEN && dose.takenTimestamp != null) {
        try {
            // Converte a data e hora agendada para um timestamp
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val scheduledTime = sdf.parse("${dose.date} ${dose.time}")?.time ?: 0

            // Calcula a diferença em minutos
            val diffInMillis = dose.takenTimestamp!! - scheduledTime
            val diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)

            if (diffInMinutes > 5) { // Consideramos um atraso se for maior que 5 minutos
                atrasoInfo = "Tomado com ${diffInMinutes}min de atraso"
            }
        } catch (e: Exception) {
            // Ignora erros de parsing de data
        }
    }

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(dose.medicationName, fontWeight = FontWeight.Bold)
                Text("Agendado: ${dose.date} às ${dose.time}", style = MaterialTheme.typography.bodySmall)

                // Mostra a informação de atraso se ela existir
                atrasoInfo?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFA000) // Uma cor de alerta (âmbar)
                    )
                }
            }
            Text(dose.status.name, color = statusColor, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
    }
}

// 5. O item da lista
@Composable
fun TakenDoseItem(dose: MedicationDose) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Dose Tomada",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(dose.medicationName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(dose.dosage, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "${dose.date} às ${dose.time}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
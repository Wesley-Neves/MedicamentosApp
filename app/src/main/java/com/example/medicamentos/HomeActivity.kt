package com.example.medicamentos

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.example.medicamentos.ui.theme.MedicamentosTheme
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MedicamentosTheme {
                HomeScreen()
            }
        }
    }
}

// ----- COMPONENTES DA UI -----

@Composable
fun MedicationCard(
    medication: Medication,
    onMedicationTaken: (Medication) -> Unit,
    onMedicationPostponed: (Medication) -> Unit
) {
    val cardColor = if (medication.status == MedicationStatus.PENDING) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer
    val contentColor = if (medication.status == MedicationStatus.PENDING) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer
    val iconColor = if (medication.status == MedicationStatus.PENDING) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (medication.status == MedicationStatus.PENDING) 1.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(all = 20.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Medication,
                        contentDescription = "Ícone de medicamento",
                        modifier = Modifier.size(44.dp),
                        tint = iconColor
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = medication.name, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = contentColor)
                        Text(text = medication.dosage, fontSize = 16.sp, color = contentColor.copy(alpha = 0.8f))
                    }
                }
                if (medication.status == MedicationStatus.TAKEN) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Medicamento tomado",
                        modifier = Modifier.size(50.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
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
                    Button(
                        onClick = { onMedicationTaken(medication) },
                        shape = RoundedCornerShape(16.dp)
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

// ----- TELA PRINCIPAL -----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val medications = DataManager.medicationDoseList
    val context = LocalContext.current

    val onMedicationTaken: (Medication) -> Unit = { medToUpdate ->
        val index = medications.indexOfFirst { it.id == medToUpdate.id }
        if (index != -1) {
            medications[index] = medToUpdate.copy(status = MedicationStatus.TAKEN)
        }

        val dosesForThisTreatment = medications.filter { it.treatmentId == medToUpdate.treatmentId }
        val allDosesTaken = dosesForThisTreatment.all { it.status == MedicationStatus.TAKEN }

        if (allDosesTaken) {
            val treatmentToUpdate = DataManager.treatmentList.find { it.id == medToUpdate.treatmentId }
            treatmentToUpdate?.let {
                // Esta é uma lógica de segurança para não contar o mesmo dia duas vezes.
                val expectedDaysCompleted = TimeUnit.MILLISECONDS.toDays(Date().time - it.startDate.time).toInt()
                if(it.daysCompleted <= expectedDaysCompleted){
                    it.daysCompleted++
                    Toast.makeText(context, "Parabéns! Doses de ${it.medicationName} por hoje concluídas!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val onMedicationPostponed: (Medication) -> Unit = { medToUpdate ->
        val index = medications.indexOfFirst { it.id == medToUpdate.id }
        if (index != -1) {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val calendar = Calendar.getInstance()
            calendar.time = sdf.parse(medToUpdate.time) ?: Date()
            calendar.add(Calendar.MINUTE, 15)
            val newTime = sdf.format(calendar.time)
            medications[index] = medToUpdate.copy(time = newTime)
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.padding(start = 24.dp, top = 48.dp, bottom = 16.dp)) {
                Text(text = "Hoje", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                val sdf = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("pt", "BR"))
                Text(text = sdf.format(Date()), style = MaterialTheme.typography.titleLarge, color = Color.Gray)
            }
        },
        bottomBar = {
            AppBottomNavigationBar(
                currentScreen = "Hoje",
                onNavigateToHome = { /* Já estamos aqui */ },
                onNavigateToSchedule = {
                    context.startActivity(Intent(context, ScheduleActivity::class.java))
                },
                onNavigateToProfile = {
                /* Lógica para ir ao Perfil */
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val intent = Intent(context, AddMedicationActivity::class.java)
                    context.startActivity(intent)
                },
                shape = CircleShape,
                modifier = Modifier.size(80.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Medicamento", modifier = Modifier.size(40.dp))
            }
        }
    ) { innerPadding ->
        val groupedMedications = medications.sortedBy { it.time }.groupBy { it.time }

        if (medications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum medicamento para hoje.\nAdicione um no botão '+'!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                groupedMedications.forEach { (time, medsInTime) ->
                    item {
                        Text(
                            text = time,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                        )
                    }
                    items(medsInTime) { medication ->
                        MedicationCard(
                            medication = medication,
                            onMedicationTaken = onMedicationTaken,
                            onMedicationPostponed = onMedicationPostponed
                        )
                    }
                }
            }
        }
    }
}


// ----- PREVIEW -----
@Preview(showBackground = true, device = "id:pixel_6")
@Composable
fun DefaultPreview() {
    MedicamentosTheme {
        HomeScreen()
    }
}
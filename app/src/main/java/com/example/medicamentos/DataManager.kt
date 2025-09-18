package com.example.medicamentos

import androidx.compose.runtime.mutableStateListOf
import java.util.Calendar
import java.util.Date

/**
 * Um objeto Singleton para guardar os dados em memória.
 * No futuro, isso será substituído por um banco de dados real (Room).
 */
object DataManager {

    // Lista para a tela de Cronograma
    val treatmentList = mutableStateListOf<Treatment>()

    // Lista para a tela Hoje
    val medicationDoseList = mutableStateListOf<Medication>()

    // Bloco de inicialização com dados de exemplo consistentes
    init {
        // --- DADOS DE EXEMPLO ---

        // IDs para conectar os dados
        val tratamentoLoratadinaId = 1
        val tratamentoOmeprazolId = 2

        // 1. Criar os tratamentos (para a tela Cronograma)
        val calendar = Calendar.getInstance()
        val loratadinaTreatment = Treatment(
            id = tratamentoLoratadinaId,
            medicationName = "Loratadina",
            startDate = calendar.apply { add(Calendar.DAY_OF_YEAR, -2) }.time, // Começou há 2 dias
            durationInDays = 7,
            daysCompleted = 2 // Já completou 2 dias
        )
        val omeprazolTreatment = Treatment(
            id = tratamentoOmeprazolId,
            medicationName = "Omeprazol",
            startDate = calendar.apply { add(Calendar.DAY_OF_YEAR, -10) }.time, // Começou há 10 dias
            durationInDays = 30,
            daysCompleted = 10 // Já completou 10 dias
        )
        treatmentList.addAll(listOf(loratadinaTreatment, omeprazolTreatment))


        // 2. Criar as doses de HOJE (para a tela Hoje), ligando-as aos tratamentos
        val loratadinaDose = Medication(
            id = 101,
            treatmentId = tratamentoLoratadinaId, // <-- Conecta com o tratamento
            name = "Loratadina",
            dosage = "1 comprimido",
            time = "09:00",
            status = MedicationStatus.PENDING
        )
        val omeprazolDose = Medication(
            id = 102,
            treatmentId = tratamentoOmeprazolId, // <-- Conecta com o tratamento
            name = "Omeprazol",
            dosage = "1 comprimido",
            time = "12:15",
            status = MedicationStatus.TAKEN
        )
        medicationDoseList.addAll(listOf(loratadinaDose, omeprazolDose).sortedBy { it.time })
    }
}
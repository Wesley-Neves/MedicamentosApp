package com.example.medicamentos

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Enum para o status de uma DOSE diária
@Parcelize
enum class MedicationStatus : Parcelable {
    PENDING,
    TAKEN
}

// Representa uma DOSE individual de um medicamento em um horário específico
@Parcelize
data class Medication(
    val id: Int,
    val treatmentId: Int,
    val name: String,
    val dosage: String,
    val time: String,
    var status: MedicationStatus
) : Parcelable
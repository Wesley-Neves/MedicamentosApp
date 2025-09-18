package com.example.medicamentos

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

// Enum para o status de um TRATAMENTO completo
enum class TreatmentStatus {
    ONGOING,
    COMPLETED
}

// Representa o TRATAMENTO completo ao longo de vários dias.
@Parcelize
data class Treatment(
    val id: Int,
    val medicationName: String,
    val startDate: Date,
    val durationInDays: Int,
    var daysCompleted: Int = 0
) : Parcelable
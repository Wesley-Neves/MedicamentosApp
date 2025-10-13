package com.example.medicamentos.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.medicamentos.data.Converters
import java.util.Date

@Entity(tableName = "treatments")
@TypeConverters(Converters::class)
data class Treatment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val medicationName: String = "", // <-- Valor padrão adicionado
    val dosage: String = "",         // <-- Valor padrão adicionado
    val startDate: Date = Date(),    // <-- Valor padrão adicionado
    val durationInDays: Int = 0,     // <-- Valor padrão adicionado
    val frequencyPerDay: Int = 0,    // <-- Valor padrão adicionado
    val startHour: Int = 0,          // <-- Valor padrão adicionado
    val startMinute: Int = 0,        // <-- Valor padrão adicionado
    val intervalHours: Int = 0,      // <-- Valor padrão adicionado
    var daysCompleted: Int = 0
)
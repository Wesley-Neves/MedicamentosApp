package com.example.medicamentos.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "daily_doses")
data class MedicationDose(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val treatmentId: Int = 0,
    val medicationName: String = "",
    val dosage: String = "",
    val time: String = "",
    val date: String = "",
    var status: MedicationStatus = MedicationStatus.PENDING
) : Parcelable

@Parcelize
enum class MedicationStatus : Parcelable {
    PENDING, TAKEN
}
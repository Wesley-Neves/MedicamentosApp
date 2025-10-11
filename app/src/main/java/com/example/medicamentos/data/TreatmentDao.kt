package com.example.medicamentos.data

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TreatmentDao {

    // --- Funções de Tratamento ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTreatment(treatment: Treatment): Long

    @Query("SELECT * FROM treatments ORDER BY startDate DESC")
    fun getAllTreatments(): Flow<List<Treatment>>

    @Update
    suspend fun updateTreatment(treatment: Treatment)

    @Query("SELECT * FROM treatments WHERE id = :id")
    suspend fun getTreatmentById(id: Int): Treatment?

    // --- Funções de Doses Diárias ---
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDose(dose: MedicationDose): Long

    @Update
    suspend fun updateDose(dose: MedicationDose)

    @Query("SELECT * FROM daily_doses WHERE date = :date ORDER BY time ASC")
    fun getDosesForDate(date: String): Flow<List<MedicationDose>>

    @Query("SELECT COUNT(id) FROM daily_doses WHERE date = :date")
    suspend fun countDosesForDate(date: String): Int

    @Query("SELECT * FROM daily_doses WHERE treatmentId = :treatmentId AND date = :date")
    suspend fun getDosesForTreatmentOnDate(treatmentId: Int, date: String): List<MedicationDose>

    @Query("SELECT * FROM daily_doses WHERE id = :doseId")
    suspend fun getDoseById(doseId: Int): MedicationDose?

    // --- Métodos para Limpar Dados ---
    @Query("DELETE FROM treatments")
    suspend fun clearAllTreatments()

    @Query("DELETE FROM daily_doses")
    suspend fun clearAllDoses()


    @Transaction
    suspend fun clearAllData() {
        clearAllDoses()
        clearAllTreatments()
        Log.d("DAO", "Todos os dados locais foram limpos")
    }

    @Query("DELETE FROM treatments WHERE id = :treatmentId")
    suspend fun deleteTreatmentById(treatmentId: Int)

    @Query("DELETE FROM daily_doses WHERE treatmentId = :treatmentId")
    suspend fun deleteDosesByTreatmentId(treatmentId: Int)

    // Transação para garantir que ambos sejam deletados com segurança
    @Transaction
    suspend fun deleteTreatmentAndDoses(treatmentId: Int) {
        deleteDosesByTreatmentId(treatmentId)
        deleteTreatmentById(treatmentId)
        Log.d("DAO", "Tratamento $treatmentId e suas doses foram deletados.")
    }

    @Query("DELETE FROM daily_doses WHERE id = :doseId")
    suspend fun deleteDoseById(doseId: Int)

    @Query("SELECT * FROM daily_doses WHERE status IN ('TAKEN', 'MISSED') ORDER BY date DESC, time DESC")
    fun getPastDosesHistory(): Flow<List<MedicationDose>>

    @Query("SELECT * FROM daily_doses WHERE date = :date AND status = 'PENDING'")
    suspend fun getPendingDosesForDate(date: String): List<MedicationDose>
}
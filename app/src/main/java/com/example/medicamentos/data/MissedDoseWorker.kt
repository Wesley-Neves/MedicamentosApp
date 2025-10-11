package com.example.medicamentos.data

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MissedDoseWorker(
    appContext: Context,
    workerParams: WorkerParameters
): CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("MissedDoseWorker", "Iniciando verificação de doses esquecidas...")
        try {
            val dao = (applicationContext as MedicamentosApplication).database.treatmentDao()
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val yesterday = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -1)
            }.time
            val yesterdayStr = sdf.format(yesterday)

            // Busca todas as doses de ontem que ainda estão pendentes
            val missedDoses = dao.getPendingDosesForDate(yesterdayStr)

            if (missedDoses.isNotEmpty()) {
                Log.d("MissedDoseWorker", "Encontradas ${missedDoses.size} doses esquecidas de ontem.")
                // Atualiza o status de cada uma para MISSED
                missedDoses.forEach { dose ->
                    dao.updateDose(dose.copy(status = MedicationStatus.MISSED))
                }
            } else {
                Log.d("MissedDoseWorker", "Nenhuma dose esquecida encontrada.")
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e("MissedDoseWorker", "Erro ao verificar doses esquecidas.", e)
            return Result.failure()
        }
    }
}
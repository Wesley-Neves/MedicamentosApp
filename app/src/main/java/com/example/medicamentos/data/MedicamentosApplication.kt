package com.example.medicamentos.data

import androidx.work.Constraints
import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Classe Application customizada para inicializar o banco de dados.
 */
class MedicamentosApplication : Application() {
    // Usando 'lazy' para que o banco de dados só seja criado quando for acessado pela primeira vez.
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        // Inicia o agendamento do worker
        scheduleMissedDoseWorker()
    }

    private fun scheduleMissedDoseWorker() {
        // Define que a tarefa não pode rodar se a bateria estiver muito baixa
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        // Cria a requisição para rodar a cada 24 horas
        val repeatingRequest = PeriodicWorkRequestBuilder<MissedDoseWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()

        // Agenda a tarefa com o sistema, garantindo que não haverá duplicatas
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "missedDoseCheck",
            ExistingPeriodicWorkPolicy.KEEP, // Mantém o agendamento existente se já houver um
            repeatingRequest
        )
    }
}
package com.example.medicamentos.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.medicamentos.R

class ReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    companion object {
        const val KEY_TREATMENT_ID = "treatment_id"
        const val KEY_MEDICATION_NAME = "medication_name"
        const val KEY_MEDICATION_DOSAGE = "medication_dosage"
    }

    override fun doWork(): Result {
        val treatmentId = inputData.getInt(KEY_TREATMENT_ID, 0)
        val medicationName = inputData.getString(KEY_MEDICATION_NAME) ?: "Medicamento"
        val medicationDosage = inputData.getString(KEY_MEDICATION_DOSAGE) ?: "Hora de tomar"

        sendNotification(treatmentId, medicationName, medicationDosage)

        return Result.success()
    }

    private fun sendNotification(treatmentId: Int, name: String, dosage: String) {
        val channelId = "medication_reminders_channel"
        val notificationId = treatmentId

        // CORREÇÃO 1: Cria o canal de notificação APENAS se estiver no Android 8.0 (API 26) ou superior.
        // Em versões mais antigas, canais não existem e não são necessários.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Lembretes de Medicamentos",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações para lembrar de tomar os medicamentos"
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_medication) // Ícone que aparece na barra de status
            .setContentTitle("Hora de tomar: $name") // Título da notificação
            .setContentText("Dosagem: $dosage") // Corpo da notificação
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // A notificação some ao ser tocada

        // Exibe a notificação
        with(NotificationManagerCompat.from(applicationContext)) {
            if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            notify(notificationId, builder.build())
        }
    }
}
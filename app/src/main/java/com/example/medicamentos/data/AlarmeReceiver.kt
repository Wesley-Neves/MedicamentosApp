package com.example.medicamentos.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.medicamentos.MainActivity
import com.example.medicamentos.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val doseId = intent.getIntExtra("DOSE_ID", 0)
        Log.d("AlarmReceiver", "Alarme recebido para DOSE_ID: $doseId")

        if (doseId == 0) {
            Log.e("AlarmReceiver", "Alarme ignorado: DOSE_ID inválido")
            return
        }

        // ✨ A CORREÇÃO FINAL ESTÁ AQUI ✨
        // Informa ao sistema que vamos fazer um trabalho assíncrono
        val pendingResult = goAsync()
        val coroutineScope = CoroutineScope(Dispatchers.IO)

        coroutineScope.launch {
            try {
                val dao = (context.applicationContext as MedicamentosApplication).database.treatmentDao()
                val dose = dao.getDoseById(doseId)

                if (dose != null) {
                    Log.d("AlarmReceiver", "Dose encontrada no banco: ${dose.medicationName}")
                    triggerAlarmFeedback(context)
                    sendNotification(context, dose) // Passa o objeto dose inteiro
                } else {
                    Log.e("AlarmReceiver", "Dose com ID $doseId não encontrada no banco.")
                }
            } finally {
                // Informa ao sistema que terminamos o trabalho em segundo plano
                pendingResult.finish()
                Log.d("AlarmReceiver", "Trabalho assíncrono finalizado.")
            }
        }
    }

    private fun triggerAlarmFeedback(context: Context) {
        Log.d("AlarmReceiver", "Tentando ativar som e vibração...")
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(1000)
                }
                Log.d("AlarmReceiver", "Vibração ativada.")
            }

            RingtonePlayerService.start(context)

        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Falha ao ativar som/vibração.", e)
        }
    }

    // Função simplificada para receber o objeto Dose
    private fun sendNotification(context: Context, dose: MedicationDose) {
        Log.d("AlarmReceiver", "--- INICIANDO sendNotification para ${dose.medicationName} ---")
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "medication_alarm_channel"
        val notificationId = dose.id

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Lembretes de Medicamentos (Alarmes)",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alarmes críticos para tomar medicamentos"
                    val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    val audioAttributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                    setSound(soundUri, audioAttributes)
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                }
                notificationManager.createNotificationChannel(channel)
            }
            Log.d("AlarmReceiver", "Passo 1: Canal de notificação OK.")

            val mainIntent = Intent(context, MainActivity::class.java)
            val pendingMainIntent = PendingIntent.getActivity(context, notificationId, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            Log.d("AlarmReceiver", "Passo 2: PendingIntent principal OK.")

            val confirmIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = "ACTION_CONFIRM"
                putExtra("NOTIFICATION_ID", notificationId)
                putExtra("DOSE_ID", dose.id)
            }
            val pendingConfirmIntent = PendingIntent.getBroadcast(context, notificationId * 10 + 1, confirmIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            Log.d("AlarmReceiver", "Passo 3: PendingIntent de confirmação OK.")

            val postponeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = "ACTION_POSTPONE"
                putExtra("NOTIFICATION_ID", notificationId)
                putExtra("DOSE_ID", dose.id)
            }
            val pendingPostponeIntent = PendingIntent.getBroadcast(context, notificationId * 10 + 2, postponeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            Log.d("AlarmReceiver", "Passo 4: PendingIntent de adiar OK.")

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_medication)
                .setContentTitle("Hora de tomar: ${dose.medicationName}")
                .setContentText("Dosagem: ${dose.dosage}")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setContentIntent(pendingMainIntent)
                .addAction(R.drawable.ic_check, "Confirmar", pendingConfirmIntent)
                if (dose.postponeCount < 2) {
                    builder.addAction(R.drawable.ic_snooze, "Adiar 15 min", pendingPostponeIntent)
                }
            Log.d("AlarmReceiver", "Passo 5: Builder da notificação construído com sucesso.")

            notificationManager.notify(notificationId, builder.build())
            Log.d("AlarmReceiver", "Passo 6: Notificação ENVIADA com sucesso!")

        } catch (e: Exception) {
            Log.e("AlarmReceiver", "CRASH ao tentar criar ou enviar notificação", e)
        }
    }
}
package com.example.medicamentos.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.*

object AlarmScheduler {

    fun schedule(context: Context, dose: MedicationDose) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Log.e("AlarmScheduler", "Permissão para agendar alarmes exatos não concedida.")
            return
        }

        // ✨ CORREÇÃO: Usamos um Intent com uma Action única para evitar conflitos
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.medicamentos.MEDICATION_ALARM" // Action para identificar o Intent
            putExtra("DOSE_ID", dose.id) // O único dado que realmente precisamos
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            dose.id, // Request code único por dose
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Calcular a hora exata do alarme
        val calendar = Calendar.getInstance()
        val timeParts = dose.time.split(":")
        val dateParts = dose.date.split("-")

        calendar.set(Calendar.YEAR, dateParts[0].toInt())
        calendar.set(Calendar.MONTH, dateParts[1].toInt() - 1) // Mês é 0-based
        calendar.set(Calendar.DAY_OF_MONTH, dateParts[2].toInt())
        calendar.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
        calendar.set(Calendar.MINUTE, timeParts[1].toInt())
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // Agendar o alarme
        if (calendar.timeInMillis > System.currentTimeMillis()) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                Log.d("AlarmScheduler", "Alarme agendado para ${dose.medicationName} às ${dose.time}")
            } catch (e: SecurityException) {
                Log.e("AlarmScheduler", "Permissão SCHEDULE_EXACT_ALARM não concedida.")
            }
        }
    }

    fun cancel(context: Context, dose: MedicationDose) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.medicamentos.MEDICATION_ALARM"
        }

        // É crucial recriar o PendingIntent exatamente da mesma forma para que o sistema o encontre e cancele
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            dose.id, // O mesmo request code único
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        Log.d("AlarmScheduler", "Alarme cancelado para a dose ${dose.id}")
    }
}
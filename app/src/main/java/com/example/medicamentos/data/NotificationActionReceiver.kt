package com.example.medicamentos.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.example.medicamentos.data.RingtonePlayerService
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        RingtonePlayerService.stop()

        val notificationId = intent.getIntExtra("NOTIFICATION_ID", 0)
        val doseId = intent.getIntExtra("DOSE_ID", 0)

        if (doseId == 0) return

        val pendingResult = goAsync()
        val coroutineScope = CoroutineScope(Dispatchers.IO)

        coroutineScope.launch {
            try {
                // ✨ Instâncias do Firebase e do DAO
                val dao = (context.applicationContext as MedicamentosApplication).database.treatmentDao()
                val db = Firebase.firestore
                val auth = Firebase.auth
                val userId = auth.currentUser?.uid ?: run {
                    Log.e("NotificationAction", "Usuário não logado, não é possível sincronizar.")
                    return@launch
                }

                when (intent.action) {
                    "ACTION_CONFIRM" -> {
                        val dose = dao.getDoseById(doseId)
                        if (dose != null) {
                            // 1. Atualiza localmente
                            val updatedDose = dose.copy(status = MedicationStatus.TAKEN)
                            dao.updateDose(updatedDose)

                            // 2. Atualiza no Firestore
                            val doseDocId = "${dose.treatmentId}_${dose.date}_${dose.time}_${dose.medicationName.hashCode()}"
                            db.collection("users").document(userId).collection("doses").document(doseDocId)
                                .update("status", MedicationStatus.TAKEN)
                                .addOnSuccessListener { Log.d("NotificationAction", "Dose ID $doseId confirmada no Firestore.") }
                                .addOnFailureListener { e -> Log.e("NotificationAction", "Erro ao confirmar dose no Firestore", e) }
                        }
                    }
                    "ACTION_POSTPONE" -> {
                        val dose = dao.getDoseById(doseId)
                        if (dose != null) {
                            if (dose.postponeCount < 2) {
                                // Calcula o novo horário (+15 minutos)
                                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                                val calendar = Calendar.getInstance().apply {
                                    time = sdf.parse(dose.time) ?: Date()
                                    add(Calendar.MINUTE, 15)
                                }
                                val newTime = sdf.format(calendar.time)

                                // 1. Atualiza localmente
                                val updatedDose = dose.copy(
                                    time = newTime,
                                    postponeCount = dose.postponeCount + 1
                                )
                                dao.updateDose(updatedDose)

                                // 2. Atualiza no Firestore (deletando o antigo e criando o novo)
                                val oldDoseDocId =
                                    "${dose.treatmentId}_${dose.date}_${dose.time}_${dose.medicationName.hashCode()}"
                                val newDoseDocId =
                                    "${updatedDose.treatmentId}_${updatedDose.date}_${updatedDose.time}_${updatedDose.medicationName.hashCode()}"

                                val oldDocRef =
                                    db.collection("users").document(userId).collection("doses")
                                        .document(oldDoseDocId)
                                val newDocRef =
                                    db.collection("users").document(userId).collection("doses")
                                        .document(newDoseDocId)

                                db.runBatch { batch ->
                                    batch.delete(oldDocRef)
                                    batch.set(newDocRef, updatedDose)
                                }
                                    .addOnSuccessListener {
                                        Log.d(
                                            "NotificationAction",
                                            "Dose ID $doseId adiada no Firestore."
                                        )
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e(
                                            "NotificationAction",
                                            "Erro ao adiar dose no Firestore",
                                            e
                                        )
                                    }


                                // Re-agenda o alarme para o novo horário
                                AlarmScheduler.schedule(context, updatedDose)
                            } else {
                                Log.d("NotificationAction", "Limite de adiamentos atingido para a dose $doseId. nenhuma ação tomada.")
                            }
                        }
                    }
                }

                // Fecha a notificação após a ação
                NotificationManagerCompat.from(context).cancel(notificationId)

            } finally {
                pendingResult.finish()
                Log.d("NotificationAction", "Ação da notificação finalizada.")
            }
        }
    }
}
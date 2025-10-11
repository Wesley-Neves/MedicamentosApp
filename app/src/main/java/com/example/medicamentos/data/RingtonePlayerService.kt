package com.example.medicamentos.data

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.util.Log

// Usamos um 'object' para garantir que haverá apenas uma instância deste controlador
object RingtonePlayerService {

    private var ringtone: Ringtone? = null

    fun start(context: Context) {
        // Garante que qualquer alarme anterior seja parado antes de iniciar um novo
        stop()
        try {
            val alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ringtone = RingtoneManager.getRingtone(context, alarmSoundUri)
            ringtone?.play()
            Log.d("RingtoneService", "Alarme iniciado.")
        } catch (e: Exception) {
            Log.e("RingtoneService", "Erro ao iniciar o alarme.", e)
            e.printStackTrace()
        }
    }

    fun stop() {
        try {
            if (ringtone?.isPlaying == true) {
                ringtone?.stop()
                Log.d("RingtoneService", "Alarme parado com sucesso.")
            }
            // Limpa a referência para liberar memória
            ringtone = null
        } catch (e: Exception) {
            Log.e("RingtoneService", "Erro ao parar o alarme.", e)
            e.printStackTrace()
        }
    }
}
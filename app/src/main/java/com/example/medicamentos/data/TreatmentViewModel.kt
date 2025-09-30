package com.example.medicamentos.data

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class TreatmentViewModel(private val dao: TreatmentDao, private val application: Application) : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    val allTreatments: Flow<List<Treatment>> = dao.getAllTreatments()
    fun getDosesForDate(date: String): Flow<List<MedicationDose>> = dao.getDosesForDate(date)

    /**
     * Insere um novo tratamento e gera TODAS as suas doses futuras.
     */
    fun insertTreatment(treatment: Treatment) = viewModelScope.launch {
        val userId = auth.currentUser?.uid ?: return@launch

        // 1. Salva no Room e obtém o ID gerado automaticamente
        val generatedId = dao.insertTreatment(treatment)
        val treatmentWithId = treatment.copy(id = generatedId.toInt())

        // 2. Salva o tratamento completo (com ID) no Firestore
        db.collection("users").document(userId)
            .collection("treatments").document(generatedId.toString())
            .set(treatmentWithId)
            .addOnSuccessListener {
                Log.d("Firestore", "Tratamento ${treatmentWithId.medicationName} salvo na nuvem!")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Erro ao salvar tratamento na nuvem", e)
            }

        // 3. Gera todas as doses para a duração do tratamento e agenda os alarmes
        generateAndSaveDoses(listOf(treatmentWithId), userId)
        scheduleRemindersForTreatment(treatmentWithId)
    }

    /**
     * Atualiza uma dose no Room e no Firestore
     */
    fun updateDose(updatedDose: MedicationDose) = viewModelScope.launch {
        val userId = auth.currentUser?.uid ?: return@launch

        // CORREÇÃO: Primeiro busca a dose ORIGINAL no Room para saber o horário anterior
        val originalDoses = dao.getDosesForTreatmentOnDate(updatedDose.treatmentId, updatedDose.date)
        val originalDose = originalDoses.find { it.id == updatedDose.id }

        if (originalDose != null) {
            // 1. Atualiza no Room
            dao.updateDose(updatedDose)

            // 2. CORREÇÃO: Calcula o ID do documento baseado no horário ORIGINAL
            val originalDocId = "${originalDose.treatmentId}_${originalDose.date}_${originalDose.time}_${originalDose.medicationName.hashCode()}"

            // 3. Se o horário mudou, precisamos deletar o documento antigo e criar um novo
            if (originalDose.time != updatedDose.time) {
                // Horário mudou - deleta o antigo e cria novo
                val newDocId = "${updatedDose.treatmentId}_${updatedDose.date}_${updatedDose.time}_${updatedDose.medicationName.hashCode()}"

                // Deleta o documento antigo
                db.collection("users").document(userId).collection("doses")
                    .document(originalDocId)
                    .delete()
                    .addOnSuccessListener {
                        Log.d("Firestore_Sync", "Documento antigo deletado: $originalDocId")

                        // Cria o novo documento
                        db.collection("users").document(userId).collection("doses")
                            .document(newDocId)
                            .set(updatedDose)
                            .addOnSuccessListener {
                                Log.d("Firestore_Sync", "SUCESSO: Dose atualizada com novo horário: ${updatedDose.medicationName} (${originalDose.time} → ${updatedDose.time})")
                            }
                    }
            } else {
                // Horário não mudou - apenas atualiza o documento existente
                db.collection("users").document(userId).collection("doses")
                    .document(originalDocId)
                    .set(updatedDose)
                    .addOnSuccessListener {
                        Log.d("Firestore_Sync", "SUCESSO: Dose atualizada: ${updatedDose.medicationName} (${updatedDose.time})")
                    }
            }
        } else {
            // Dose não encontrada localmente - insere como nova
            dao.updateDose(updatedDose)
            val newDocId = "${updatedDose.treatmentId}_${updatedDose.date}_${updatedDose.time}_${updatedDose.medicationName.hashCode()}"
            db.collection("users").document(userId).collection("doses")
                .document(newDocId)
                .set(updatedDose)
                .addOnSuccessListener {
                    Log.d("Firestore_Sync", "Nova dose criada: ${updatedDose.medicationName} (${updatedDose.time})")
                }
        }

        checkIfTreatmentDayIsComplete(updatedDose)
    }

    /**
     * Verifica se um dia de tratamento foi concluído e atualiza o progresso.
     */
    private fun checkIfTreatmentDayIsComplete(dose: MedicationDose) = viewModelScope.launch {
        val dosesForDate = dao.getDosesForTreatmentOnDate(dose.treatmentId, dose.date)
        if (dosesForDate.isNotEmpty() && dosesForDate.all { it.status == MedicationStatus.TAKEN }) {
            val treatment = dao.getTreatmentById(dose.treatmentId)
            treatment?.let { treatmentObj ->
                val updatedTreatment = treatmentObj.copy(daysCompleted = treatmentObj.daysCompleted + 1)
                dao.updateTreatment(updatedTreatment)

                val userId = auth.currentUser?.uid ?: return@launch
                db.collection("users").document(userId)
                    .collection("treatments").document(treatmentObj.id.toString())
                    .set(updatedTreatment)
                    .addOnSuccessListener {
                        Log.d("TreatmentUpdate", "Dias completos atualizados para ${treatmentObj.medicationName}")
                    }
                    .addOnFailureListener { e ->
                        Log.e("TreatmentUpdate", "Erro ao atualizar dias completos no Firestore", e)
                    }
            }
        }
    }

    /**
     * Sincronização inteligente que evita duplicação
     */
    fun syncDataFromFirestore() = viewModelScope.launch {
        val userId = auth.currentUser?.uid ?: return@launch
        Log.d("SyncDebug", "Iniciando sincronização segura para: $userId")

        try {
            db.collection("users").document(userId).collection("treatments").get()
                .addOnSuccessListener { documents ->
                    val cloudTreatments = documents.toObjects(Treatment::class.java)
                    Log.d("SyncDebug", "Encontrados ${cloudTreatments.size} tratamentos no Firestore")

                    if (cloudTreatments.isNotEmpty()) {
                        viewModelScope.launch {
                            // 1. PRIMEIRO sincroniza as doses (para preservar status)
                            syncDosesFromFirestore(userId)

                            // 2. DEPOIS processa os tratamentos
                            val localTreatments = allTreatments.first()

                            val newTreatments = cloudTreatments.filter { cloudTreatment ->
                                localTreatments.none { it.id == cloudTreatment.id }
                            }

                            Log.d("SyncDebug", "${newTreatments.size} tratamentos novos")

                            // 3. Gera doses APENAS para tratamentos realmente novos
                            if (newTreatments.isNotEmpty()) {
                                newTreatments.forEach { treatment ->
                                    dao.insertTreatment(treatment)
                                    Log.d("SyncDebug", "Tratamento novo: ${treatment.medicationName}")

                                    // Pequeno delay para garantir que a sincronização de doses terminou
                                    kotlinx.coroutines.delay(500)
                                    generateAndSaveDoses(listOf(treatment), userId)
                                }
                            }

                            Log.d("SyncDebug", "Sincronização segura completa")
                        }
                    } else {
                        Log.d("SyncDebug", "Nenhum tratamento encontrado")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("SyncDebug", "Erro ao buscar tratamentos", e)
                }
        } catch (e: Exception) {
            Log.e("SyncDebug", "Erro na sincronização", e)
        }
    }

    /**
     * CORREÇÃO: Sincroniza doses do Firestore SEM duplicar
     */
    private fun syncDosesFromFirestore(userId: String) = viewModelScope.launch {
        db.collection("users").document(userId).collection("doses").get()
            .addOnSuccessListener { documents ->
                val cloudDoses = documents.toObjects(MedicationDose::class.java)
                Log.d("SyncDebug", "Encontradas ${cloudDoses.size} doses na nuvem para sincronizar")

                if (cloudDoses.isNotEmpty()) {
                    viewModelScope.launch {
                        // CORREÇÃO: Busca TODAS as doses locais de uma vez para comparação eficiente
                        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        val localDosesToday = dao.getDosesForDate(today).first()

                        // Cria um mapa para busca rápida: chave = "treatmentId_date_time_medicationName"
                        val localDosesMap = localDosesToday.associateBy { dose ->
                            "${dose.treatmentId}_${dose.date}_${dose.time}_${dose.medicationName}"
                        }

                        val dosesToUpdate = mutableListOf<MedicationDose>()
                        val dosesToInsert = mutableListOf<MedicationDose>()

                        cloudDoses.forEach { cloudDose ->
                            val doseKey = "${cloudDose.treatmentId}_${cloudDose.date}_${cloudDose.time}_${cloudDose.medicationName}"

                            val localDose = localDosesMap[doseKey]

                            if (localDose != null) {
                                // Dose existe localmente - atualiza APENAS se o status for diferente
                                if (localDose.status != cloudDose.status) {
                                    dosesToUpdate.add(cloudDose)
                                    Log.d("SyncDebug", "Status será atualizado: ${cloudDose.medicationName} - ${cloudDose.status}")
                                }
                            } else {
                                // Dose não existe localmente - insere
                                dosesToInsert.add(cloudDose)
                                Log.d("SyncDebug", "Dose será inserida do cloud: ${cloudDose.medicationName}")
                            }
                        }

                        // Executa as operações em lote
                        if (dosesToUpdate.isNotEmpty()) {
                            dosesToUpdate.forEach { dose ->
                                dao.updateDose(dose)
                            }
                            Log.d("SyncDebug", "${dosesToUpdate.size} doses atualizadas")
                        }

                        if (dosesToInsert.isNotEmpty()) {
                            dao.insertDoses(dosesToInsert)
                            Log.d("SyncDebug", "${dosesToInsert.size} doses inseridas")
                        }

                        Log.d("SyncDebug", "Sincronização de doses concluída: ${dosesToUpdate.size} atualizadas, ${dosesToInsert.size} inseridas")
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("SyncDebug", "Erro ao buscar doses da nuvem", e)
            }
    }

    /**
     * Gera e salva doses APENAS se não existirem
     */
    private fun generateAndSaveDoses(treatments: List<Treatment>, userId: String) = viewModelScope.launch {
        Log.d("DoseDebug", "Gerando doses para ${treatments.size} tratamentos NOVOS")

        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dosesToInsert = mutableListOf<MedicationDose>()
        val dosesToSaveInFirestore = mutableListOf<MedicationDose>()

        treatments.forEach { treatment ->
            val medName = treatment.medicationName.ifEmpty { "Medicamento sem nome" }
            Log.d("DoseDebug", "Verificando doses para: $medName")

            for (day in 0 until treatment.durationInDays) {
                (0 until treatment.frequencyPerDay).forEach { i ->
                    val doseCalendar = Calendar.getInstance().apply {
                        time = treatment.startDate
                        add(Calendar.DAY_OF_YEAR, day)
                        set(Calendar.HOUR_OF_DAY, treatment.startHour)
                        set(Calendar.MINUTE, treatment.startMinute)
                        set(Calendar.SECOND, 0)
                        add(Calendar.HOUR_OF_DAY, i * treatment.intervalHours)
                    }

                    val doseDate = sdfDate.format(doseCalendar.time)
                    val doseTime = sdfTime.format(doseCalendar.time)

                    val newDose = MedicationDose(
                        treatmentId = treatment.id,
                        medicationName = treatment.medicationName,
                        dosage = treatment.dosage,
                        time = doseTime,
                        date = doseDate,
                        status = MedicationStatus.PENDING
                    )

                    // CORREÇÃO: Verifica se a dose já existe consultando o DAO
                    val existingDoses = dao.getDosesForTreatmentOnDate(treatment.id, doseDate)
                    val doseAlreadyExists = existingDoses.any { it.time == doseTime && it.medicationName == treatment.medicationName }

                    if (!doseAlreadyExists) {
                        dosesToInsert.add(newDose)
                        dosesToSaveInFirestore.add(newDose)
                        Log.d("DoseDebug", "Dose nova: ${treatment.medicationName} - $doseTime - $doseDate")
                    } else {
                        Log.d("DoseDebug", "Dose já existe: ${treatment.medicationName} - $doseTime - $doseDate")
                    }
                }
            }
        }

        Log.d("DoseDebug", "Doses a inserir no Room: ${dosesToInsert.size}")
        Log.d("DoseDebug", "Doses a salvar no Firestore: ${dosesToSaveInFirestore.size}")

        // Insere no Room APENAS doses que não existem
        if (dosesToInsert.isNotEmpty()) {
            dao.insertDoses(dosesToInsert)
            Log.d("DoseDebug", "${dosesToInsert.size} doses NOVAS salvas no Room")
        }

        // Salva no Firestore APENAS doses que não existem
        if (dosesToSaveInFirestore.isNotEmpty()) {
            val batch = db.batch()
            dosesToSaveInFirestore.forEach { dose ->
                val doseDocId = "${dose.treatmentId}_${dose.date}_${dose.time}_${dose.medicationName.hashCode()}"
                val doseRef = db.collection("users").document(userId).collection("doses").document(doseDocId)
                batch.set(doseRef, dose)
            }
            batch.commit()
                .addOnSuccessListener {
                    Log.d("DoseDebug", "${dosesToSaveInFirestore.size} doses NOVAS salvas na nuvem")
                }
                .addOnFailureListener { e ->
                    Log.e("DoseDebug", "Erro ao salvar doses NOVAS na nuvem", e)
                }
        } else {
            Log.d("DoseDebug", "Nenhuma dose nova para salvar no Firestore")
        }
    }

    // Função agora obsoleta.
    fun generateDosesForTodayIfNeeded() = viewModelScope.launch { }

    private fun scheduleRemindersForTreatment(treatment: Treatment) {
        val workManager = WorkManager.getInstance(application)
        val data = Data.Builder()
            .putInt(ReminderWorker.KEY_TREATMENT_ID, treatment.id)
            .putString(ReminderWorker.KEY_MEDICATION_NAME, treatment.medicationName)
            .putString(ReminderWorker.KEY_MEDICATION_DOSAGE, treatment.dosage)
            .build()

        val now = Calendar.getInstance()

        (0 until treatment.frequencyPerDay).forEach { i ->
            val nextDose = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, treatment.startHour)
                set(Calendar.MINUTE, treatment.startMinute)
                set(Calendar.SECOND, 0)
                add(Calendar.HOUR_OF_DAY, i * treatment.intervalHours)
                if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
            }

            val initialDelay = nextDose.timeInMillis - now.timeInMillis

            val reminderRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("reminder_for_treatment_${treatment.id}")
                .build()

            workManager.enqueue(reminderRequest)
        }
    }

    /**
     * Limpa todos os dados locais (útil para logout)
     */
    fun clearLocalData() = viewModelScope.launch {
        try {
            dao.clearAllData()
            Log.d("TreatmentViewModel", "Todos os dados locais foram limpos")
        } catch (e: Exception) {
            Log.e("TreatmentViewModel", "Erro ao limpar dados locais", e)
        }
    }

    /**
     * CORREÇÃO: Escuta mudanças em tempo real do Firestore para sincronização entre dispositivos
     */
    fun startRealtimeSync() = viewModelScope.launch {
        val userId = auth.currentUser?.uid ?: return@launch

        // Escuta mudanças nas doses
        db.collection("users").document(userId).collection("doses")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("RealtimeSync", "Erro ao escutar mudanças nas doses", error)
                    return@addSnapshotListener
                }

                snapshot?.documentChanges?.forEach { change ->
                    when (change.type) {
                        com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                            val updatedDose = change.document.toObject(MedicationDose::class.java)
                            viewModelScope.launch {
                                dao.updateDose(updatedDose)
                                Log.d("RealtimeSync", "Dose atualizada em tempo real: ${updatedDose.medicationName}")
                            }
                        }
                        else -> {}
                    }
                }
            }
    }
}

class TreatmentViewModelFactory(
    private val dao: TreatmentDao,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TreatmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TreatmentViewModel(dao, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
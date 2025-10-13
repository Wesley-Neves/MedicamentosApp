package com.example.medicamentos.data

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TreatmentViewModel(private val dao: TreatmentDao, private val application: Application) : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private val _treatmentToEdit = MutableStateFlow<Treatment?>(null)
    val treatmentToEdit = _treatmentToEdit.asStateFlow()

    val allTreatments: Flow<List<Treatment>> = dao.getAllTreatments()
    fun getDosesForDate(date: String): Flow<List<MedicationDose>> = dao.getDosesForDate(date)

    fun loadTreatmentById(id: Int) {
        viewModelScope.launch {
            _treatmentToEdit.value = dao.getTreatmentById(id)
        }
    }

    fun clearEditingTreatment() {
        _treatmentToEdit.value = null
    }

    fun updateTreatmentAndRescheduleDoses(updatedTreatment: Treatment) = viewModelScope.launch {
        val userId = auth.currentUser?.uid ?: return@launch

        // 1. Busca todas as doses antigas para cancelar os alarmes
        val oldDoses = dao.getDosesForTreatmentOnDate(updatedTreatment.id, "%") // '%' é um curinga para qualquer data
        oldDoses.forEach { dose ->
            AlarmScheduler.cancel(application.applicationContext, dose)
        }

        // 2. Deleta todas as doses antigas do Room
        dao.deleteDosesByTreatmentId(updatedTreatment.id)

        // 3. Atualiza o tratamento no Room
        dao.updateTreatment(updatedTreatment)

        // 4. Sincroniza as deleções e a atualização com o Firestore
        // (Esta parte é complexa, vamos simplificar deletando e recriando)
        deleteTreatmentFromFirestore(updatedTreatment.id, userId) // Função auxiliar
        db.collection("users").document(userId)
            .collection("treatments").document(updatedTreatment.id.toString())
            .set(updatedTreatment)
            .addOnSuccessListener { Log.d("Firestore", "Tratamento ${updatedTreatment.id} re-sincronizado.") }

        // 5. Gera e salva as novas doses e agenda os novos alarmes
        // Reutilizamos a função que já existe!
        generateAndSaveDoses(listOf(updatedTreatment), userId)
    }

    // Função auxiliar para deletar doses antigas do Firestore
    private fun deleteTreatmentFromFirestore(treatmentId: Int, userId: String) {
        db.collection("users").document(userId)
            .collection("doses").whereEqualTo("treatmentId", treatmentId)
            .get()
            .addOnSuccessListener { documents ->
                val batch = db.batch()
                for (document in documents) {
                    batch.delete(document.reference)
                }
                batch.commit().addOnSuccessListener { Log.d("Firestore", "Doses antigas do tratamento $treatmentId deletadas.") }
            }
    }

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
    }

    fun deleteTreatment(treatment: Treatment) = viewModelScope.launch {
        val userId = auth.currentUser?.uid ?: return@launch

        // 1. Deleta do Room (usando a transação)
        dao.deleteTreatmentAndDoses(treatment.id)

        // 2. Deleta do Firestore
        // Primeiro, deleta o documento do tratamento
        db.collection("users").document(userId)
            .collection("treatments").document(treatment.id.toString())
            .delete()
            .addOnSuccessListener { Log.d("Firestore", "Tratamento ${treatment.id} deletado da nuvem.") }
            .addOnFailureListener { e -> Log.w("Firestore", "Erro ao deletar tratamento da nuvem", e) }

        // Depois, deleta todas as doses associadas na nuvem
        // (Isso é mais complexo, a forma mais simples é buscar e deletar em lote)
        db.collection("users").document(userId)
            .collection("doses").whereEqualTo("treatmentId", treatment.id)
            .get()
            .addOnSuccessListener { documents ->
                val batch = db.batch()
                for (document in documents) {
                    batch.delete(document.reference)
                }
                batch.commit()
                    .addOnSuccessListener { Log.d("Firestore", "Doses do tratamento ${treatment.id} deletadas da nuvem.") }
            }
    }

    fun deleteDose(dose: MedicationDose) = viewModelScope.launch {
        val userId = auth.currentUser?.uid ?: return@launch

        // 1. Deleta do Room
        dao.deleteDoseById(dose.id)

        // 2. Deleta do Firestore
        val doseDocId = "${dose.treatmentId}_${dose.date}_${dose.time}_${dose.medicationName.hashCode()}"
        db.collection("users").document(userId)
            .collection("doses").document(doseDocId)
            .delete()
            .addOnSuccessListener { Log.d("Firestore", "Dose ${dose.id} deletada da nuvem.") }
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

        if (updatedDose.status == MedicationStatus.TAKEN) {
            // Usamos o 'application' context que o ViewModel já possui.
            AlarmScheduler.cancel(application.applicationContext, updatedDose)
            Log.d("AlarmCancellation", "Alarme para a dose ${updatedDose.id} cancelado após confirmação no app.")
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
                                    delay(500)
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
                            dosesToInsert.forEach { dose ->
                                dao.insertDose(dose)
                            }
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
        Log.d("DoseDebug", "Gerando doses para ${treatments.size} tratamentos")

        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dosesToSaveInFirestore = mutableListOf<MedicationDose>()

        treatments.forEach { treatment ->
            val medName = treatment.medicationName.ifEmpty { "Medicamento sem nome" }
            Log.d("DoseDebug", "Processando tratamento: $medName")

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

                    // Verifica se a dose já existe ANTES de qualquer coisa
                    val existingDoses = dao.getDosesForTreatmentOnDate(treatment.id, doseDate)
                    val doseAlreadyExists = existingDoses.any { it.time == doseTime && it.medicationName == treatment.medicationName }

                    if (!doseAlreadyExists) {
                        // Se não existe, cria o objeto (com id=0)
                        val newDose = MedicationDose(
                            treatmentId = treatment.id,
                            medicationName = treatment.medicationName,
                            dosage = treatment.dosage,
                            time = doseTime,
                            date = doseDate,
                            status = MedicationStatus.PENDING
                        )

                        // Insere no banco e PEGA O ID REAL de volta
                        val generatedId = dao.insertDose(newDose)

                        // Cria uma cópia da dose, agora com o ID correto
                        val doseWithId = newDose.copy(id = generatedId.toInt())

                        // Agenda o alarme USANDO O OBJETO COM O ID CORRETO
                        Log.d("AlarmScheduling", "Agendando alarme para dose com ID REAL: ${doseWithId.id}")
                        AlarmScheduler.schedule(application.applicationContext, doseWithId)

                        // Adiciona na lista para salvar no Firestore depois
                        dosesToSaveInFirestore.add(doseWithId)
                    }
                }
            }
        }

        // A lógica de salvar no Firestore continua a mesma, mas agora usa
        // a lista de doses que foram realmente criadas e agendadas.
        if (dosesToSaveInFirestore.isNotEmpty()) {
            val batch = db.batch()
            dosesToSaveInFirestore.forEach { dose ->
                val doseDocId = "${dose.treatmentId}_${dose.date}_${dose.time}_${dose.medicationName.hashCode()}"
                val doseRef = db.collection("users").document(userId).collection("doses").document(doseDocId)
                batch.set(doseRef, dose)
            }
            batch.commit()
                .addOnSuccessListener {
                    Log.d("DoseDebug", "${dosesToSaveInFirestore.size} doses salvas na nuvem")
                }
                .addOnFailureListener { e ->
                    Log.e("DoseDebug", "Erro ao salvar doses na nuvem", e)
                }
        }
    }

    // Função agora obsoleta.
    fun generateDosesForTodayIfNeeded() = viewModelScope.launch { }

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
                        DocumentChange.Type.MODIFIED -> {
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
package com.example.medicamentos.data

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CaregiverViewModel : ViewModel() {

    private val db = Firebase.firestore
    private var treatmentsListener: ListenerRegistration? = null
    private var dosesListener: ListenerRegistration? = null

    private val _treatments = MutableStateFlow<List<Treatment>>(emptyList())
    val treatments: StateFlow<List<Treatment>> = _treatments

    private val _doses = MutableStateFlow<List<MedicationDose>>(emptyList())
    val doses: StateFlow<List<MedicationDose>> = _doses

    val isLoading = mutableStateOf(false)
    val patientName = mutableStateOf("Paciente")

    fun fetchPatientData(patientUid: String, name: String) {
        patientName.value = name
        isLoading.value = true
        Log.d("CaregiverViewModel_Debug", "INICIANDO BUSCA de dados para o paciente UID: $patientUid")

        // Limpa ouvintes antigos para evitar buscas duplicadas
        treatmentsListener?.remove()
        dosesListener?.remove()

        // Ouve em tempo real os TRATAMENTOS
        treatmentsListener = db.collection("users").document(patientUid)
            .collection("treatments")
            .addSnapshotListener { snapshot, error ->
                isLoading.value = false // Controla o loading principal
                if (error != null) {
                    Log.e("CaregiverViewModel_Debug", "FALHA ao buscar tratamentos: ", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val patientTreatments = snapshot.toObjects(Treatment::class.java)
                    _treatments.value = patientTreatments
                    Log.d("CaregiverViewModel_Debug", "SUCESSO: ${patientTreatments.size} tratamentos encontrados.")
                } else {
                    Log.d("CaregiverViewModel_Debug", "Snapshot de tratamentos nulo.")
                }
            }

        // Ouve em tempo real as DOSES do dia
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(Date())
        Log.d("CaregiverViewModel_Debug", "Buscando doses para a data: $todayStr")

        dosesListener = db.collection("users").document(patientUid)
            .collection("doses")
            .whereEqualTo("date", todayStr)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("CaregiverViewModel_Debug", "FALHA ao buscar doses do dia: ", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val patientDoses = snapshot.toObjects(MedicationDose::class.java)
                    _doses.value = patientDoses
                    Log.d("CaregiverViewModel_Debug", "SUCESSO: ${patientDoses.size} doses encontradas para hoje.")
                } else {
                    Log.d("CaregiverViewModel_Debug", "Snapshot de doses nulo.")
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        treatmentsListener?.remove()
        dosesListener?.remove()
        Log.d("CaregiverViewModel_Debug", "ViewModel limpo, ouvintes removidos.")
    }
}

class CaregiverViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CaregiverViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CaregiverViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
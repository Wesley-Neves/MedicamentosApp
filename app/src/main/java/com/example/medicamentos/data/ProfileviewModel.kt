package com.example.medicamentos.data

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProfileViewModel : ViewModel() {

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    // StateFlow para expor os dados do perfil para a UI. Começa como nulo.
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    // Função que busca os dados no Firestore
    fun fetchUserProfile() {
        // Pega o ID do usuário atualmente logado
        val userId = auth.currentUser?.uid

        if (userId != null) {
            // Vai na coleção "users" e pega o documento com o ID do usuário
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Converte o documento do Firestore para nosso objeto UserProfile
                        val profile = document.toObject(UserProfile::class.java)
                        _userProfile.value = profile
                    } else {
                        Log.d("Firestore", "Nenhum documento de perfil encontrado")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("Firestore", "Erro ao buscar perfil: ", exception)
                }
        }
    }
}


// Factory simples para criar o ViewModel
class ProfileViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
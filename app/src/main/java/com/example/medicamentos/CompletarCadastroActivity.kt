package com.example.medicamentos

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.medicamentos.ui.theme.MedicamentosTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class CompletarCadastroActivity : ComponentActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Firebase.firestore

        // Pega os dados enviados pela tela anterior
        val userId = intent.getStringExtra("USER_UID")
        val userName = intent.getStringExtra("USER_NAME") ?: "Nome não encontrado"
        val userEmail = intent.getStringExtra("USER_EMAIL") ?: "Email não encontrado"

        if (userId == null) {
            Toast.makeText(this, "Erro: ID de usuário não encontrado.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setContent {
            MedicamentosTheme {
                CompletarCadastroScreen(
                    db = db,
                    userId = userId,
                    prefilledName = userName,
                    prefilledEmail = userEmail
                )
            }
        }
    }
}

@Composable
fun CompletarCadastroScreen(
    db: FirebaseFirestore,
    userId: String,
    prefilledName: String,
    prefilledEmail: String
) {
    val context = LocalContext.current
    val activity = LocalContext.current

    // Estados para os novos campos
    var telefone by remember { mutableStateOf("") }
    var dataNascimento by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var selectedToggle by remember { mutableStateOf(ToggleState.CADASTRO) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // Estrutura de Coluna para Cabeçalho Fixo
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(50.dp))
            AnimatedToggle(
                selectedState = selectedToggle,
                onStateChange = { }
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Coluna interna rolável para o formulário
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Campo Nome (Desabilitado)
                    FormField(
                        label = "Nome Completo",
                        value = prefilledName,
                        onValueChange = {}, // Não faz nada, pois é desabilitado
                        enabled = false // Desabilita o campo
                    )
                    // Campo E-mail (Desabilitado)
                    FormField(
                        label = "E-mail",
                        value = prefilledEmail,
                        onValueChange = {},
                        enabled = false
                    )
                    // Campo Telefone (Habilitado)
                    FormField(
                        label = "Telefone",
                        value = telefone,
                        onValueChange = { telefone = it },
                        placeholder = "(XX) XXXXX-XXXX",
                        keyboardType = KeyboardType.Phone
                    )
                    // Campo Data de Nascimento (Habilitado)
                    FormField(
                        label = "Data de Nascimento",
                        value = dataNascimento,
                        onValueChange = { dataNascimento = it },
                        placeholder = "DD/MM/AAAA",
                        keyboardType = KeyboardType.Text
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (telefone.isBlank() || dataNascimento.isBlank()) {
                                Toast.makeText(context, "Por favor, preencha os campos restantes.", Toast.LENGTH_SHORT).show()
                            } else {
                               isLoading = true

                                // --- LÓGICA DE SALVAR NO FIRESTORE ---

                                // 1. Cria um mapa com os dados do perfil do usuário
                                val userProfile = hashMapOf(
                                    "uid" to userId,
                                    "name" to prefilledName,
                                    "email" to prefilledEmail,
                                    "phone" to telefone,
                                    "dob" to dataNascimento
                                )

                                // 2. Salva os dados no Firestore, na coleção "users", usando o uid como ID do documento
                                db.collection("users").document(userId)
                                    .set(userProfile)
                                    .addOnSuccessListener {
                                        isLoading = false
                                        Toast.makeText(context, "Cadastro finalizado com sucesso!", Toast.LENGTH_SHORT).show()

                                        // 3. Navega para a Home e limpa o histórico de telas de login/cadastro
                                        val intent = Intent(context, HomeActivity::class.java).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        }
                                        context.startActivity(intent)
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false

                                        Log.e("FIRESTORE_ERROR", "Erro ao salvar perfil: ", e)
                                        Toast.makeText(context, "Erro ao salvar perfil: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                            }

                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(
                                "Prosseguir",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun CompletarCadastroScreenPreview() {

}
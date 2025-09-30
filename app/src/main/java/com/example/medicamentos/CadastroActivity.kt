package com.example.medicamentos

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.example.medicamentos.ui.theme.MedicamentosTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.compose.ui.unit.dp
import com.example.medicamentos.data.UserProfile
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore

class CadastroActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        setContent {
            MedicamentosTheme {
                CadastroScreen(auth = auth)
            }
        }
    }
}

@Composable
fun CadastroScreen(auth: FirebaseAuth) {
    val context = LocalContext.current
    val activity = LocalActivity.current

    var nome by remember { mutableStateOf("") }
    var dataNascimento by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confirmarSenha by remember { mutableStateOf("") }
    var isSenhaVisible by remember { mutableStateOf(false) }
    var isConfirmarSenhaVisible by remember { mutableStateOf(false) }
    var selectedToggle by remember { mutableStateOf(ToggleState.CADASTRO) }
    var isLoading by remember { mutableStateOf(false) }

    var isGoogleLoading by remember { mutableStateOf(false) }


    // --- LÓGICA DO GOOGLE ADICIONADA AQUI ---

    // Pegamos a webClientId dos recursos (gerado pelo google-services.json)
    val webClientId = LocalContext.current.getString(R.string.default_web_client_id)

    val gso = remember(webClientId) {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId).requestEmail().build()
    }
    val googleSignInClient = remember(context, gso) { GoogleSignIn.getClient(context, gso) }

    val googleAuthLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val email = account.email!!

                // --- MUDANÇA PRINCIPAL AQUI ---
                // 1. Verifica se o e-mail já existe
                auth.fetchSignInMethodsForEmail(email)
                    .addOnCompleteListener(activity!!) { fetchTask ->
                        if (fetchTask.isSuccessful) {
                            val signInMethods = fetchTask.result?.signInMethods
                            if (signInMethods.isNullOrEmpty()) {
                                // O usuário NÃO existe. Prossegue com o cadastro.
                                val idToken = account.idToken!!
                                val credential = GoogleAuthProvider.getCredential(idToken, null)
                                auth.signInWithCredential(credential)
                                    .addOnCompleteListener { firebaseTask ->
                                        isLoading = false
                                        if (firebaseTask.isSuccessful) {
                                            // Cadastro com Google bem-sucedido!
                                            // Leva para a tela de completar o perfil.
                                            val user = firebaseTask.result?.user
                                            val intent = Intent(context, CompletarCadastroActivity::class.java)
                                            intent.putExtra("USER_UID", user?.uid)
                                            intent.putExtra("USER_NAME", user?.displayName ?: "Usuário do Google")
                                            intent.putExtra("USER_EMAIL", user?.email ?: "")
                                            context.startActivity(intent)
                                        } else {
                                            Toast.makeText(context, firebaseTask.exception?.message ?: "Falha no cadastro com Firebase.", Toast.LENGTH_LONG).show()
                                        }
                                    }
                            } else {
                                // O usuário JÁ EXISTE.
                                Toast.makeText(context, "Este e-mail já está em uso. Por favor, faça login.", Toast.LENGTH_LONG).show()
                                isGoogleLoading = false
                                googleSignInClient.signOut()
                            }
                        } else {
                            isGoogleLoading = false
                            Toast.makeText(context, "Erro ao verificar e-mail.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } catch (e: ApiException) {
                isGoogleLoading = false
                Toast.makeText(context, "Falha no cadastro com Google.", Toast.LENGTH_LONG).show()
            }
        } else {
            isGoogleLoading = false
        }
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(50.dp))
            AnimatedToggle(
                selectedState = selectedToggle,
                onStateChange = { newState ->
                    selectedToggle = newState
                    if (newState == ToggleState.LOGIN) {
                        activity?.finish()
                    }
                }
            )
            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FormField(label = "Nome Completo", value = nome, onValueChange = { nome = it })
                    FormField(label = "Data de Nascimento", value = dataNascimento, onValueChange = { dataNascimento = it }, keyboardType = KeyboardType.Text)
                    FormField(label = "Telefone", value = telefone, onValueChange = { telefone = it }, keyboardType = KeyboardType.Phone)
                    FormField(label = "E-mail", value = email, onValueChange = { email = it }, keyboardType = KeyboardType.Email)
                    PasswordField(label = "Senha", value = senha, onValueChange = { senha = it }, isVisible = isSenhaVisible, onVisibilityChange = { isSenhaVisible = !isSenhaVisible })
                    PasswordField(label = "Confirmar Senha", value = confirmarSenha, onValueChange = { confirmarSenha = it }, isVisible = isConfirmarSenhaVisible, onVisibilityChange = { isConfirmarSenhaVisible = !isConfirmarSenhaVisible })

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (senha != confirmarSenha) {
                                Toast.makeText(context, "As senhas não coincidem!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (nome.isBlank() || email.isBlank() || senha.isBlank()) {
                                Toast.makeText(context, "Preencha os campos obrigatórios!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isLoading = true
                            auth.createUserWithEmailAndPassword(email.trim(), senha)
                                .addOnCompleteListener(activity!!) { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        val user = auth.currentUser
                                        val userId = user?.uid

                                        if (userId != null) {
                                            val userProfile = UserProfile(
                                                uid = userId,
                                                name = nome.trim(),
                                                email = email.trim(),
                                                phone = telefone.trim(),
                                                dob = dataNascimento.trim()
                                            )
                                            val db = Firebase.firestore
                                            db.collection("users").document(userId).set(userProfile)
                                                .addOnSuccessListener {
                                                    isLoading = false
                                                    Toast.makeText(context, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show()
                                                    activity.finish()
                                                }
                                                .addOnFailureListener {
                                                    isLoading = false
                                                    Toast.makeText(context, "Erro ao criar conta: ${it.message}", Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                    } else {
                                        // FALHA na autenticação
                                        isLoading = false
                                        val exception = task.exception?.message ?: "Erro desconhecido."
                                        Toast.makeText(context, "Falha no cadastro: $exception", Toast.LENGTH_LONG).show()
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
                            Text("Cadastrar-se", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    OrDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            isGoogleLoading = true
                            googleAuthLauncher.launch(googleSignInClient.signInIntent)
                        },
                        enabled = !isGoogleLoading,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                    ) {
                        if (isGoogleLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(28.dp))
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_google),
                                contentDescription = "Google Icon",
                                modifier = Modifier.size(24.dp),
                                tint = Color.Unspecified
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Cadastre-se com o Google",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
fun CadastroScreenPreview() {

}
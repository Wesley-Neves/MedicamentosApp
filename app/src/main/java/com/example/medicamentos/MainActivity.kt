package com.example.medicamentos

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.medicamentos.ui.theme.MedicamentosTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import androidx.activity.compose.rememberLauncherForActivityResult
import com.google.firebase.firestore.ktx.firestore

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val isInCaregiverMode = sharedPreferences.getBoolean("KEY_IS_CAREGIVER_MODE", false)

        if (isInCaregiverMode) {
            // Se está no modo cuidador, busca os dados salvos e pula para a tela do cuidador
            val patientUid = sharedPreferences.getString("KEY_PATIENT_UID", null)
            val patientName = sharedPreferences.getString("KEY_PATIENT_NAME", "Paciente")

            if (patientUid != null) {
                val intent = Intent(this, CaregiverHomeActivity::class.java).apply {
                    putExtra("PATIENT_UID", patientUid)
                    putExtra("PATIENT_NAME", patientName)
                }
                startActivity(intent)
                finish() // Finaliza a MainActivity para que ela não fique na pilha
                return   // Impede que o resto do código (setContent) seja executado
            }
        }

        // Pega a web client ID do arquivo de recursos gerado pelo google-services.json
        val webClientId = getString(R.string.default_web_client_id)

        setContent {
            MedicamentosTheme {
                LoginScreen(auth = auth, webClientId = webClientId)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser

        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val isInCaregiverMode = sharedPreferences.getBoolean("KEY_IS_CAREGIVER_MODE", false)

        // Só redireciona para a HomeActivity se NÃO estiver no modo cuidador
        if (currentUser != null && !isInCaregiverMode) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }
}

@Composable
fun LoginScreen(auth: FirebaseAuth, webClientId: String) {
    val context = LocalContext.current
    val activity = LocalActivity.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var selectedToggle by remember { mutableStateOf(ToggleState.LOGIN) }
    var isLoading by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }

    var hasError by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                selectedToggle = ToggleState.LOGIN
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val gso = remember(webClientId) {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember(context, gso) { GoogleSignIn.getClient(context, gso) }

    val googleAuthLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isGoogleLoading = false
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val idToken = account.idToken!!
                val credential = GoogleAuthProvider.getCredential(idToken, null)

                auth.signInWithCredential(credential)
                    .addOnCompleteListener(activity!!) { authTask ->
                        if (authTask.isSuccessful) {
                            val user = authTask.result?.user!!
                            val db = Firebase.firestore

                            // PASSO 2: A verificação crucial - o perfil existe no Firestore?
                            db.collection("users").document(user.uid).get()
                                .addOnSuccessListener { document ->
                                    if (document != null && document.exists()) {
                                        // PERFIL EXISTE -> Login bem-sucedido, vai para a Home!
                                        Toast.makeText(context, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()
                                        context.startActivity(Intent(context, HomeActivity::class.java))
                                        activity.finish()
                                    } else {
                                        // PERFIL NÃO EXISTE -> Usuário é novo, vai para Completar Cadastro
                                        val intent = Intent(context, CompletarCadastroActivity::class.java).apply {
                                            putExtra("USER_UID", user.uid)
                                            putExtra("USER_NAME", user.displayName ?: "Usuário do Google")
                                            putExtra("USER_EMAIL", user.email ?: "")
                                        }
                                        context.startActivity(intent)
                                        activity.finish()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    // Erro ao se comunicar com o banco de dados
                                    Toast.makeText(context, "Erro ao verificar perfil: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        } else {
                            // Falha na autenticação com o Firebase (problema de rede, etc.)
                            Toast.makeText(context, authTask.exception?.message ?: "Falha no login com Firebase.", Toast.LENGTH_LONG).show()
                        }
                    }
            } catch (e: ApiException) {
                // Falha na comunicação com os serviços do Google
                Toast.makeText(context, "Falha na comunicação com o Google.", Toast.LENGTH_LONG).show()
            }
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
                    if (newState == ToggleState.CADASTRO) {
                        context.startActivity(Intent(context, CadastroActivity::class.java))
                    }
                }
            )
            Spacer(modifier = Modifier.height(48.dp))

            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FormField(
                    label = "E-mail",
                    value = email,
                    onValueChange = {
                        email = it
                        hasError = false // Limpa o erro assim que o usuário começa a corrigir
                    },
                    placeholder = "ex: seuemail@email.com",
                    keyboardType = KeyboardType.Email,
                    isError = hasError // Conecta o estado de erro ao componente
                )
                Spacer(modifier = Modifier.height(24.dp))
                PasswordField(
                    label = "Senha",
                    value = password,
                    onValueChange = {
                        password = it
                        hasError = false // Limpa o erro assim que o usuário começa a corrigir
                    },
                    isVisible = isPasswordVisible,
                    onVisibilityChange = { isPasswordVisible = !isPasswordVisible },
                    isError = hasError // Conecta o estado de erro ao componente
                )
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            Toast.makeText(context, "Preencha e-mail e senha.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isLoading = true
                        auth.signInWithEmailAndPassword(email.trim(), password)
                            .addOnCompleteListener(activity!!) { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    hasError = false
                                    Toast.makeText(context, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()
                                    context.startActivity(Intent(context, HomeActivity::class.java))
                                    activity.finish()
                                } else {
                                    hasError = true
                                    //val exception = task.exception?.message ?: "Falha na autenticação."
                                    //Toast.makeText(context, "Erro: $exception", Toast.LENGTH_LONG).show()
                                }
                            }
                    },
                    enabled = !isLoading && !isGoogleLoading,
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Entrar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }

                TextButton(onClick = { /* Lógica para esquecer senha */ }) {
                    Text("Esqueceu a Senha?", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
                OrDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        isGoogleLoading = true
                        googleAuthLauncher.launch(googleSignInClient.signInIntent)
                    },
                    enabled = !isLoading && !isGoogleLoading,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                ) {
                    if (isGoogleLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = "Google Icon",
                            modifier = Modifier.size(24.dp),
                            tint = Color.Unspecified
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Entrar com o Google", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { context.startActivity(Intent(context, CuidadorScanActivity::class.java)) },
                    modifier = Modifier.wrapContentWidth().height(50.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    Text("Entrar como cuidador")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MedicamentosTheme {
        // O Preview não consegue inicializar o Firebase, então a UI pode não renderizar
        // completamente, mas o código compilará.
        // LoginScreen(auth = null, webClientId = "")
    }
}
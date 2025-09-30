package com.example.medicamentos

import android.app.Activity
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

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

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
        if (currentUser != null) {
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
                val email = account.email!!

                auth.fetchSignInMethodsForEmail(email)
                    .addOnCompleteListener(activity!!) { fetchTask ->
                        if (fetchTask.isSuccessful) {
                            val signInMethods = fetchTask.result?.signInMethods
                            if (signInMethods.isNullOrEmpty()) {
                                // Se a lista estiver vazia, o usuário NÃO existe.
                                Toast.makeText(
                                    context,
                                    "Nenhuma conta encontrada com este e-mail. Por favor, cadastre-se.",
                                    Toast.LENGTH_LONG
                                ).show()
                                isGoogleLoading = false
                                googleSignInClient.signOut() // Desconecta para permitir nova tentativa
                            } else {
                                // O usuário EXISTE. Prossegue com o login.
                                val idToken = account.idToken!!
                                val credential = GoogleAuthProvider.getCredential(idToken, null)
                                auth.signInWithCredential(credential)
                                    .addOnCompleteListener { firebaseTask ->
                                        isGoogleLoading = false
                                        if (firebaseTask.isSuccessful) {
                                            Toast.makeText(
                                                context,
                                                "Login com Google bem-sucedido!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            context.startActivity(
                                                Intent(
                                                    context,
                                                    HomeActivity::class.java
                                                )
                                            )
                                            activity.finish()
                                        } else {
                                            Toast.makeText(
                                                context,
                                                firebaseTask.exception?.message
                                                    ?: "Falha no login com Firebase.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                            }
                        } else {
                            isGoogleLoading = false
                            Toast.makeText(context, "Erro ao verificar e-mail.", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
            } catch (e: ApiException) {
                isGoogleLoading = false
                Toast.makeText(
                    context,
                    "Falha no login com Google. Verifique sua conexão.",
                    Toast.LENGTH_LONG
                ).show()
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
                FormField(label = "E-mail", value = email, onValueChange = { email = it }, placeholder = "ex: seuemail@email.com", keyboardType = KeyboardType.Email)
                Spacer(modifier = Modifier.height(24.dp))
                PasswordField(label = "Senha", value = password, onValueChange = { password = it }, isVisible = isPasswordVisible, onVisibilityChange = { isPasswordVisible = !isPasswordVisible })

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
                                    Toast.makeText(context, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()
                                    context.startActivity(Intent(context, HomeActivity::class.java))
                                    activity.finish()
                                } else {
                                    val exception = task.exception?.message ?: "Falha na autenticação."
                                    Toast.makeText(context, "Erro: $exception", Toast.LENGTH_LONG).show()
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
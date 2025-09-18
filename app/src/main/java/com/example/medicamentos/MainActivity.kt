package com.example.medicamentos

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MedicamentosTheme {
                LoginScreen()
            }
        }
    }
}

@Composable
fun LoginScreen() {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var selectedToggle by remember { mutableStateOf(ToggleState.LOGIN) }

    // Efeito para resetar o estado do toggle sempre que a tela de Login fica visível
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

    //função para ir para a tela de cadastro
    val goToCadastro: () -> Unit = {
        context.startActivity(Intent(context, CadastroActivity::class.java))
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // Coluna principal NÃO rolável para o cabeçalho fixo
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(50.dp))
            // Item 1: O Toggle (Fixo no topo)
            AnimatedToggle(
                selectedState = selectedToggle,
                onStateChange = { newState ->
                    selectedToggle = newState
                    if (newState == ToggleState.CADASTRO) {
                        goToCadastro()
                    }
                }
            )
            Spacer(modifier = Modifier.height(48.dp))

            // Item 2: Coluna interna rolável para o formulário
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "E-mail",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("ex: seuemail@email.com") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "Senha",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Digite sua senha") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Mostrar/Ocultar senha"
                                )
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        Toast.makeText(context, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()
                        context.startActivity(Intent(context, HomeActivity::class.java))
                        (context as? Activity)?.finish()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Entrar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                TextButton(onClick = { /* Lógica para esquecer senha */ }) {
                    Text("Esqueceu a Senha?", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
                OrDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { /* Lógica de login com Google */ },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = "Google Icon",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Entrar com o Google", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- LÓGICA DO BOTÃO CUIDADOR ---
                Button(
                    onClick = {
                        // Inicia a CuidadorScanActivity
                        context.startActivity(Intent(context, CuidadorScanActivity::class.java))
                    },
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
        LoginScreen()
    }
}
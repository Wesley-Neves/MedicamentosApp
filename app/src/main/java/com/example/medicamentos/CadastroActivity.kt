package com.example.medicamentos

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.unit.dp
import com.example.medicamentos.ui.theme.MedicamentosTheme

class CadastroActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MedicamentosTheme {
                CadastroScreen()
            }
        }
    }
}

@Composable
fun CadastroScreen() {
    val context = LocalContext.current

    var nome by remember { mutableStateOf("") }
    var dataNascimento by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confirmarSenha by remember { mutableStateOf("") }
    var isSenhaVisible by remember { mutableStateOf(false) }
    var isConfirmarSenhaVisible by remember { mutableStateOf(false) }
    var selectedToggle by remember { mutableStateOf(ToggleState.CADASTRO) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // Coluna principal NÃO rolável
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Item 1: O Toggle (Fixo no topo)
            Spacer(modifier = Modifier.height(50.dp))
            AnimatedToggle(
                selectedState = selectedToggle,
                onStateChange = { newState ->
                    selectedToggle = newState
                    if (newState == ToggleState.LOGIN) {
                        (context as? Activity)?.finish()
                    }
                }
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Item 2: Coluna interna rolável
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FormField(
                        label = "Nome Completo",
                        value = nome,
                        onValueChange = { nome = it },
                        placeholder = "Digite seu nome completo"
                    )
                    FormField(
                        label = "Data de Nascimento",
                        value = dataNascimento,
                        onValueChange = { dataNascimento = it },
                        placeholder = "DD/MM/AAAA",
                        keyboardType = KeyboardType.Text
                    )
                    FormField(
                        label = "Telefone",
                        value = telefone,
                        onValueChange = { telefone = it },
                        placeholder = "(XX) XXXXX-XXXX",
                        keyboardType = KeyboardType.Phone
                    )
                    FormField(
                        label = "E-mail",
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "seuemail@email.com",
                        keyboardType = KeyboardType.Email
                    )
                    PasswordField(
                        label = "Senha",
                        value = senha,
                        onValueChange = { senha = it },
                        isVisible = isSenhaVisible,
                        onVisibilityChange = { isSenhaVisible = !isSenhaVisible }
                    )
                    PasswordField(
                        label = "Confirmar Senha",
                        value = confirmarSenha,
                        onValueChange = { confirmarSenha = it },
                        isVisible = isConfirmarSenhaVisible,
                        onVisibilityChange = { isConfirmarSenhaVisible = !isConfirmarSenhaVisible }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (senha != confirmarSenha) {
                                Toast.makeText(context, "As senhas não coincidem!", Toast.LENGTH_SHORT).show()
                            } else if (nome.isBlank() || email.isBlank() || senha.isBlank()) {
                                Toast.makeText(context, "Preencha os campos obrigatórios!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                                (context as? Activity)?.finish()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("Cadastrar-se", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(5.dp))
                    OrDivider()
                    Spacer(modifier = Modifier.height(5.dp))

                    // Abre a tela de completar cadastro com dados do Google
                    Button(
                        onClick = {
                            val intent = Intent(context, CompletarCadastroActivity::class.java)
                            intent.putExtra("USER_NAME", "Usuário do Google")
                            intent.putExtra("USER_EMAIL", "usuario.google@exemplo.com")
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
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
                        Text("Cadastre-se com o Google", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    MedicamentosTheme {
        CadastroScreen()
    }
}
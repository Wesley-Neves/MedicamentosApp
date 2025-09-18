package com.example.medicamentos

import android.app.Activity
import android.os.Bundle
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

class CompletarCadastroActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Pega os dados enviados pela tela anterior
        val nomeUsuario = intent.getStringExtra("USER_NAME") ?: "Nome não encontrado"
        val emailUsuario = intent.getStringExtra("USER_EMAIL") ?: "Email não encontrado"

        setContent {
            MedicamentosTheme {
                CompletarCadastroScreen(
                    prefilledName = nomeUsuario,
                    prefilledEmail = emailUsuario
                )
            }
        }
    }
}

@Composable
fun CompletarCadastroScreen(
    prefilledName: String,
    prefilledEmail: String
) {
    val context = LocalContext.current

    // Estados para os novos campos
    var telefone by remember { mutableStateOf("") }
    var dataNascimento by remember { mutableStateOf("") }
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
                onStateChange = { newState ->
                    selectedToggle = newState
                    if (newState == ToggleState.LOGIN) {
                        (context as? Activity)?.finish()
                    }
                }
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Coluna interna rolável para o formulário
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
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
                                Toast.makeText(context, "Cadastro finalizado com sucesso!", Toast.LENGTH_SHORT).show()
                                // Lógica futura: navegar para a HomeActivity e finalizar o fluxo
                                (context as? Activity)?.finish()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("Prosseguir", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
    MedicamentosTheme {
        CompletarCadastroScreen(prefilledName = "Carla Alves", prefilledEmail = "carla.alves@email.com")
    }
}
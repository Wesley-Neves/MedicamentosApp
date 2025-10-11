package com.example.medicamentos

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.height
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.colorResource
import com.example.medicamentos.data.MedicationDose
import com.example.medicamentos.data.MedicationStatus
import com.example.medicamentos.data.Treatment


/**
 * Barra de navegação reutilizável para todo o app.
 */
@Composable
fun AppBottomNavigationBar(
    currentScreen: String,
    onNavigateToHome: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .navigationBarsPadding()
    ) {

        val iconSize = 32.dp
        val labelSize = 16.sp

        // Item "Hoje"
        NavigationBarItem(
            modifier = Modifier.padding(vertical = 8.dp),
            selected = currentScreen == "Hoje",
            onClick = onNavigateToHome,
            icon = { Icon(Icons.Default.Today, contentDescription = "Hoje", modifier = Modifier.size(iconSize)) },
            label = { Text("Hoje", fontSize = labelSize, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary
            )
        )
        // Item "Cronograma"
        NavigationBarItem(
            modifier = Modifier.padding(vertical = 8.dp),
            selected = currentScreen == "Cronograma",
            onClick = onNavigateToSchedule,
            icon = { Icon(Icons.Default.DateRange, contentDescription = "Cronograma", modifier = Modifier.size(iconSize)) },
            label = { Text("Cronograma", fontSize = labelSize) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary
            )
        )
        // Item "Perfil"
        NavigationBarItem(
            modifier = Modifier.padding(vertical = 8.dp),
            selected = currentScreen == "Perfil",
            onClick = { onNavigateToProfile() },
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Perfil",
                    modifier = Modifier.size(iconSize)
                )
            },
            label = { Text("Perfil") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

// Enum para o nosso toggle
enum class ToggleState {
    LOGIN, CADASTRO
}

/**
 * Toggle animado e reutilizável para Login/Cadastro.
 */
@Composable
fun AnimatedToggle(
    selectedState: ToggleState,
    onStateChange: (ToggleState) -> Unit
){
    val offset by animateDpAsState(targetValue = if (selectedState == ToggleState.LOGIN) 0.dp else 160.dp, label = "offset")

    Box(
        modifier = Modifier
            .width(320.dp)
            .height(60.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .width(160.dp)
                .fillMaxHeight()
                .offset(x = offset)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(28.dp)
                )
        )
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onStateChange(ToggleState.LOGIN) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Login",
                    color = if (selectedState == ToggleState.LOGIN) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onStateChange(ToggleState.CADASTRO) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Cadastrar",
                    color = if (selectedState == ToggleState.CADASTRO) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }
    }
}


/**
 * Componente reutilizável para campos de texto padrão do formulário.
 */
@Composable
fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            enabled = enabled,
            isError = isError,
            visualTransformation = visualTransformation,
            supportingText = {
                if (isError && errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )
    }
}

/**
 * Componente reutilizável para campos de senha.
 */
@Composable
fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isVisible: Boolean,
    onVisibilityChange: () -> Unit,
    isError: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("Digite sua senha") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = isError,
            supportingText = {
                if (isError) {
                    Text(
                        text = "Email ou senha incorretos",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            trailingIcon = {
                IconButton(onClick = onVisibilityChange) {
                    Icon(
                        imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Mostrar/Ocultar senha"
                    )
                }
            }
        )
    }
}
// ----- ou -----
@Composable
fun OrDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Linha da esquerda
        Divider(
            modifier = Modifier.weight(1f),
            color = Color.Gray.copy(alpha = 0.5f),
            thickness = 1.dp
        )
        // Texto "ou"
        Text(
            text = "ou",
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        // Linha da direita
        Divider(
            modifier = Modifier.weight(1f),
            color = Color.Gray.copy(alpha = 0.5f),
            thickness = 1.dp
        )
    }
}

//tratamento
@Composable
fun TreatmentCard(
    treatment: Treatment,
    onDelete: ((Treatment) -> Unit)? = null
    ) {
    val progress = (treatment.daysCompleted.toFloat() / treatment.durationInDays).coerceIn(0f, 1f)
    val daysRemaining = (treatment.durationInDays - treatment.daysCompleted).coerceAtLeast(0)
    val isCompleted = daysRemaining == 0 || treatment.daysCompleted >= treatment.durationInDays

    val cardColor = if (isCompleted) {
        colorResource(id = R.color.status_green_container)
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }

    val contentColor = if (isCompleted) {
        colorResource(id = R.color.status_green_on_container)
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    var showOptionsDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showOptionsDialog && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showOptionsDialog = false },
            title = { Text(treatment.medicationName) },
            text = { Text("O que você gostaria de fazer?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(treatment)
                        showOptionsDialog = false
                    }
                ) { Text("Excluir", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = {
                    val intent = Intent(context, AddMedicationActivity::class.java)
                    intent.putExtra("TREATMENT_ID", treatment.id) // Envia o ID
                    context.startActivity(intent)
                    showOptionsDialog = false
                }) { Text("Editar") }
            }
        )
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("Detalhes do Tratamento") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Medicamento: ${treatment.medicationName}")
                    Text("Dosagem: ${treatment.dosage}")
                    Text("Duração: ${treatment.durationInDays} dias")
                    Text("Frequência: ${treatment.frequencyPerDay} vezes ao dia")
                }
            },
            confirmButton = { TextButton(onClick = { showInfoDialog = false }) { Text("OK") } }
        )
    }


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .then(
                if (onDelete != null) {
                    Modifier.clickable { showOptionsDialog = true }
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
        // A borda não é mais necessária, pois o contraste virá das cores do container
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = treatment.medicationName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = contentColor // Usa a cor de conteúdo adaptável
                    )
                    if (isCompleted) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Tratamento Finalizado",
                            tint = contentColor // Usa a cor de conteúdo adaptável
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Tratamento de ${treatment.durationInDays} dias",
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.7f) // Usa a cor de conteúdo com transparência
                )
                Spacer(modifier = Modifier.height(16.dp))

                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = contentColor, // Usa a cor de conteúdo adaptável
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.height(8.dp))

                val statusText =
                    if (isCompleted) "Tratamento finalizado!" else "Faltam $daysRemaining dias"
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor, // Usa a cor de conteúdo adaptável
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(
                onClick = { showInfoDialog = true },
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = "Informações do tratamento",
                    tint = contentColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun CaregiverMedicationCard(medication: MedicationDose) { // ✅ Usa MedicationDose
    val isTaken = medication.status == MedicationStatus.TAKEN
    val cardColor = if (isTaken) colorResource(id = R.color.status_green_container) else MaterialTheme.colorScheme.surfaceVariant
    val iconBackgroundColor = if (isTaken) colorResource(id = R.color.status_green_on_container) else MaterialTheme.colorScheme.primary
    val iconContentColor = if (isTaken) colorResource(id = R.color.status_green_container) else MaterialTheme.colorScheme.onPrimary
    val statusIconColor = if (isTaken) colorResource(id = R.color.status_green_on_container) else Color.Gray

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(50.dp).clip(CircleShape).background(iconBackgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Medication, contentDescription = "Ícone de medicamento", modifier = Modifier.size(28.dp), tint = iconContentColor)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    // ✅ Usa medication.medicationName, o campo correto da entidade
                    Text(text = medication.medicationName, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(text = medication.dosage, fontSize = 16.sp, color = Color.Gray)
                }
            }
            val statusIcon = if (isTaken) Icons.Default.CheckCircle else Icons.Default.Schedule
            Icon(imageVector = statusIcon, contentDescription = if (isTaken) "Tomado" else "Pendente", modifier = Modifier.size(36.dp), tint = statusIconColor)
        }
    }
}
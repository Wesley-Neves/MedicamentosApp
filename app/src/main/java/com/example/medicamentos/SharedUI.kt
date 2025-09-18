package com.example.medicamentos

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    NavigationBar(modifier = Modifier.height(96.dp)) {

        val iconSize = 40.dp
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
            label = { Text("Cronograma", fontSize = labelSize) }
        )
        // Item "Perfil"
        NavigationBarItem(
            modifier = Modifier.padding(vertical = 8.dp),
            selected = currentScreen == "Perfil",
            onClick = onNavigateToProfile,
            icon = { Icon(Icons.Default.Person, contentDescription = "Perfil", modifier = Modifier.size(iconSize)) },
            label = { Text("Perfil", fontSize = labelSize) }
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
    enabled: Boolean = true
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
            enabled = enabled
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
    onVisibilityChange: () -> Unit
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


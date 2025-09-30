package com.example.medicamentos

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medicamentos.data.UserProfile
import com.example.medicamentos.data.ProfileViewModel
import com.example.medicamentos.data.ProfileViewModelFactory
import com.example.medicamentos.ui.theme.MedicamentosTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.journeyapps.barcodescanner.BarcodeEncoder

class ProfileActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

        val currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(this, "Nenhum usuário logado.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        setContent {
            MedicamentosTheme {
                val viewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory())
                ProfileScreen(
                    auth = auth,
                    viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    auth: FirebaseAuth,
    viewModel: ProfileViewModel
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.fetchUserProfile()
    }

    val userProfile by viewModel.userProfile.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Meu Perfil",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        bottomBar = {
            AppBottomNavigationBar(
                currentScreen = "Perfil",
                onNavigateToHome = {
                    val intent = Intent(context, HomeActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    context.startActivity(intent)
                },
                onNavigateToSchedule = {
                    val intent = Intent(context, ScheduleActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    context.startActivity(intent)
                },
                onNavigateToProfile = { /* Já estamos aqui */ }
            )
        }
    ) { innerPadding ->
        if (userProfile == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            ProfileContent(
                modifier = Modifier.padding(innerPadding),
                auth = auth,
                profile = userProfile!!
            )
        }
    }
}
@Composable
fun ProfileContent(modifier: Modifier = Modifier, auth: FirebaseAuth, profile: UserProfile) {
    val context = LocalContext.current

    // O QR Code é gerado com o ID único do usuário (profile.uid)
    val qrCodeBitmap = remember(profile.uid) {
        generateQrCodeBitmap("medlembrete://vincular?uid=${profile.uid}")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier.size(120.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = "Foto de Perfil", modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = profile.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(text = profile.email, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                auth.signOut()
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = "Sair")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sair", color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold)
        }

        Divider(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), color = Color.Gray.copy(alpha = 0.2f))

        ProfileInfoRow(label = "Telefone:", value = profile.phone)
        ProfileInfoRow(label = "Nascimento:", value = profile.dob)

        Spacer(modifier = Modifier.height(32.dp))
        Text("Compartilhar com Cuidador", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

        if (qrCodeBitmap != null) {
            Image(
                bitmap = qrCodeBitmap.asImageBitmap(),
                contentDescription = "QR Code para Cuidador",
                modifier = Modifier.size(220.dp).clip(RoundedCornerShape(16.dp)).background(Color.White).padding(10.dp)
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

private fun generateQrCodeBitmap(text: String): Bitmap? {
    return try {
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 400, 400)
        val barcodeEncoder = BarcodeEncoder()
        barcodeEncoder.createBitmap(bitMatrix)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true, device = "id:pixel_6")
@Composable
fun ProfileScreenPreview() {
    MedicamentosTheme {

    }
}
package com.example.medicamentos

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.medicamentos.ui.theme.MedicamentosTheme
// ADICIONE ESTAS IMPORTAÇÕES
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView

class CuidadorScanActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                recreate()
            } else {
                Toast.makeText(this, "Permissão da câmera é necessária para escanear.", Toast.LENGTH_LONG).show()
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MedicamentosTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    when (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)) {
                        PackageManager.PERMISSION_GRANTED -> {
                            CuidadorScanScreen(onCodeScanned = { result ->
                                val prefix = "medlembrete://vincular?uid="
                                if (result.startsWith(prefix)) {
                                    val patientUid = result.substringAfter(prefix)
                                    // ✨ MUDANÇA PRINCIPAL: Chamamos a função para buscar dados no Firebase
                                    fetchPatientNameAndNavigate(patientUid)
                                } else {
                                    Toast.makeText(this, "Código QR inválido ou não compatível.", Toast.LENGTH_LONG).show()
                                    // O ideal seria reiniciar o scanner aqui, mas por enquanto um aviso é suficiente.
                                }
                            })
                        }
                        else -> {
                            SideEffect {
                                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Pedindo permissão da câmera...")
                            }
                        }
                    }
                }
            }
        }
    }

    // ✨ NOVA FUNÇÃO: Lógica para buscar os dados no Firestore
    private fun fetchPatientNameAndNavigate(patientUid: String) {
        Toast.makeText(this, "Verificando paciente...", Toast.LENGTH_SHORT).show()

        val db = Firebase.firestore
        db.collection("users").document(patientUid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Sucesso! O paciente foi encontrado.
                    val patientName = document.getString("name") ?: "Paciente"

                    // ✨ SALVANDO O ESTADO DO MODO CUIDADOR ✨
                    val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
                    with(sharedPreferences.edit()) {
                        putBoolean("KEY_IS_CAREGIVER_MODE", true)
                        putString("KEY_PATIENT_UID", patientUid)
                        putString("KEY_PATIENT_NAME", patientName)
                        apply() // Salva as alterações
                    }

                    // Navega para a tela do cuidador com os dados corretos
                    val intent = Intent(this, CaregiverHomeActivity::class.java).apply {
                        putExtra("PATIENT_UID", patientUid)
                        putExtra("PATIENT_NAME", patientName)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()

                } else {
                    // Erro: O UID do QR Code não existe no banco de dados.
                    Toast.makeText(this, "Paciente não encontrado!", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
            .addOnFailureListener { exception ->
                // Erro de conexão ou outro problema com o Firebase.
                Toast.makeText(this, "Erro ao buscar dados: ${exception.message}", Toast.LENGTH_LONG).show()
                finish()
            }
    }
}


// O Composable CuidadorScanScreen não precisa de nenhuma alteração.
@Composable
fun CuidadorScanScreen(onCodeScanned: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val barcodeView = remember {
        DecoratedBarcodeView(context)
    }

    DisposableEffect(lifecycleOwner) {
        val activity = context as ComponentActivity
        val captureManager = CaptureManager(activity, barcodeView)
        captureManager.initializeFromIntent(activity.intent, null)

        barcodeView.decodeContinuous { result ->
            result.text?.let { barCodeOrQr ->
                if (barCodeOrQr.startsWith("medlembrete://vincular?uid=")){
                    barcodeView.pause()
                }
                onCodeScanned(barCodeOrQr)
            }
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> captureManager.onResume()
                Lifecycle.Event.ON_PAUSE -> captureManager.onPause()
                Lifecycle.Event.ON_DESTROY -> captureManager.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            captureManager.onDestroy()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(64.dp))
        Text(
            text = "Escanear QR Code",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Aponte a câmera para o QR Code do usuário que você deseja Vincular",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 64.dp)
        ) {
            AndroidView(factory = { barcodeView })
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CuidadorScanScreenPreview() {
    MedicamentosTheme {
        CuidadorScanScreen(onCodeScanned = {})
    }
}
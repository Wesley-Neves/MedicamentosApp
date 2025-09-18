package com.example.medicamentos

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView

class CuidadorScanActivity : ComponentActivity() {

    // Lógica para pedir permissão de câmera
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permissão concedida, o compose tentará recriar o scanner
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
                // Verifica a permissão antes de exibir a UI
                when (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)) {
                    PackageManager.PERMISSION_GRANTED -> {
                        // Se já temo a permissão, mostra a tela de scan
                        CuidadorScanScreen(onCodeScanned = { result ->
                            Toast.makeText(this, "Código escaneado: $result. Vinculando...", Toast.LENGTH_LONG).show()
                            finish()
                        })
                    }
                    else -> {
                        // Se não tem, lança o pedido de permissão
                        SideEffect {
                            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        }
                        // Mostra uma tela de carregamento ou informativa enquanto pede permissão
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Pedindo permissão da câmera...")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CuidadorScanScreen(onCodeScanned: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // O CaptureManager controla o ciclo de vida do scanner
    var captureManager: CaptureManager? = null

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

        // O AndroidView é a ponte entre o Compose e o sistema de Views antigo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Ocupa o espaço restante
                .padding(bottom = 64.dp)
        ) {
            AndroidView(
                factory = {
                    // 1. Cria a View do Scanner
                    DecoratedBarcodeView(context).apply {
                        // Configura o que acontece quando um código é lido
                        decodeContinuous { result ->
                            result.text?.let {
                                onCodeScanned(it)
                            }
                        }
                    }
                },
                update = { view ->
                    // Pega a activity a partir do contexto
                    val activity = context as ComponentActivity

                    if (captureManager == null) {
                        captureManager = CaptureManager(activity, view)
                    }
                    // Usamos o intent da activity
                    captureManager?.initializeFromIntent(activity.intent, null)
                    captureManager?.decode()
                }
            )

            // 3. Gerencia o ciclo de vida (onResume, onPause, etc.)
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> captureManager?.onResume()
                        Lifecycle.Event.ON_PAUSE -> captureManager?.onPause()
                        Lifecycle.Event.ON_DESTROY -> captureManager?.onDestroy()
                        else -> {}
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun CuidadorScanScreenPreview() {
    MedicamentosTheme {
        // No preview, não pode mostrar a câmera, então é passado uma função vazia
        CuidadorScanScreen(onCodeScanned = {})
    }
}
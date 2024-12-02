package pl.smolisoft.mapka.ui

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.osmdroid.views.MapView
import pl.smolisoft.mapka.GpxUtils.drawGpxPath
import pl.smolisoft.mapka.services.PermissionHandler

@Composable
fun MenuContent(
    permissionHandler: PermissionHandler,
    context: Context,
    mapView: MapView?,
    onDismiss: () -> Unit,
    onAnotherOptionSelected: () -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                val inputStream = context.contentResolver.openInputStream(it)
                inputStream?.let { stream ->
                    drawGpxPath(stream, mapView)
                }
            }
        }
    )

    val requestPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted: Boolean ->
                if (isGranted) {
                    // Jeśli przyznano uprawnienia, uruchom selektor plików
                    launcher.launch(arrayOf("*/*"))
                } else {
                    // Jeśli odmówiono uprawnień, poinformuj użytkownika
                    Toast.makeText(context, "Brak uprawnień do odczytu plików", Toast.LENGTH_SHORT).show()
                }
            }
        )

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text("Menu", style = MaterialTheme.typography.titleMedium)

        Button(
            onClick = {
                permissionHandler.checkStoragePermission(
                    activity = context as ComponentActivity, // Upewnij się, że `context` to `ComponentActivity`
                    requestPermissionLauncher = requestPermissionLauncher
                ) {
                    launcher.launch(arrayOf("*/*")) // Otwórz selektor plików, jeśli uprawnienia są przyznane
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Wczytaj GPX")
        }

        Button(
            onClick = {
                onAnotherOptionSelected()
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Inna opcja")
        }
    }
}
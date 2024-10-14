package pl.smolisoft.mapka.ui

import android.content.Context
import android.net.Uri
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
import pl.smolisoft.mapka.services.SharedViewModel

@Composable
fun MenuContent(
    context: Context,
    mapView: MapView,
    viewModel: SharedViewModel,
    onDismiss: () -> Unit,
    onSettingsSelected: () -> Unit,
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

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text("Menu", style = MaterialTheme.typography.titleMedium)

        Button(
            onClick = {
                launcher.launch(arrayOf("*/*"))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Wczytaj GPX")
        }

        Button(
            onClick = {
                viewModel.startRecording(mapView)
//                onAnotherOptionSelected()
//                onDismiss()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Nagrywaj")
        }
    }
}
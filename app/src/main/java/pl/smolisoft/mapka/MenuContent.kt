package pl.smolisoft.mapka

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MenuContent(
    onDismiss: () -> Unit,
    onSettingsSelected: () -> Unit,
    onAnotherOptionSelected: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text("Menu", style = MaterialTheme.typography.titleMedium)

        Button(
            onClick = {
                onSettingsSelected()
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ustawienia")
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
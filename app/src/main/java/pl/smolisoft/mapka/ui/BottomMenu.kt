package pl.smolisoft.mapka.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetMenu(
    onDismiss: () -> Unit,
    onSettingsSelected: () -> Unit,
    onAnotherOptionSelected: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Menu", style = MaterialTheme.typography.titleMedium)
        Divider(modifier = Modifier.padding(vertical = 8.dp))
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
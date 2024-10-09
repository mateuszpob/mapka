package pl.smolisoft.mapka.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import pl.smolisoft.mapka.services.SharedViewModel

@Composable
fun BottomBar(
    viewModel: SharedViewModel,
    toggleLocationService: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = Modifier.fillMaxWidth()
        .background(Color.LightGray)) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.Start // Ustawia przyciski na lewo
        ) {
            Button(
                onClick = {
                    viewModel.isTracking = !viewModel.isTracking // Przełączenie trybu śledzenia
                    if (viewModel.isTracking) {
                        viewModel.isLocationUpdate = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.isTracking) Color.Green else Color.Gray // Zmiana koloru na zielony, gdy włączone śledzenie
                )
            ) {
                Text(if (viewModel.isTracking) "Tracking" else "Tracking")
            }

            Spacer(modifier = Modifier.width(16.dp)) // Dodaje przestrzeń między przyciskami

            Button(
                onClick = {
                    viewModel.isLocationUpdate = !viewModel.isLocationUpdate
                    toggleLocationService(viewModel.isLocationUpdate)

                    if (!viewModel.isLocationUpdate) {
                        viewModel.isTracking = false
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.isLocationUpdate) Color.Green else Color.Gray // Zmiana koloru na zielony, gdy włączone śledzenie
                )
            ) {
                Text(if (viewModel.isLocationUpdate) "Location" else "Location")
            }

            Spacer(modifier = Modifier.weight(1f)) // Dodaje elastyczną przestrzeń między przyciskami a trzecim przyciskiem

            Button(onClick = {
                viewModel.isMenuOpened = !viewModel.isMenuOpened
            }) {
                Text("M")
            }
        }
    }
}

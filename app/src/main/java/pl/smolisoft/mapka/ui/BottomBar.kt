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

@Composable
fun BottomBar(
    toggleTracking: (Boolean) -> Unit,
    toggleLocationService: (Boolean) -> Unit,
    toggleMenu: (Boolean) -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    var isTracking by remember { mutableStateOf(false) }
    var isLocationUpdate by remember { mutableStateOf(false) }
    var isMenuOened by remember { mutableStateOf(false) }
    
//    var isLocationUpdate by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()
        .background(Color.LightGray)) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.Start // Ustawia przyciski na lewo
        ) {
            Button(
                onClick = {
                    isTracking = !isTracking // Przełączenie trybu śledzenia
                    toggleTracking(isTracking)
                    if (isTracking) {
                        isLocationUpdate = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTracking) Color.Green else Color.Gray // Zmiana koloru na zielony, gdy włączone śledzenie
                )
            ) {
                Text(if (isTracking) "Tracking" else "Tracking")
            }

            Spacer(modifier = Modifier.width(16.dp)) // Dodaje przestrzeń między przyciskami

            Button(
                onClick = {
                    isLocationUpdate = !isLocationUpdate
                    toggleLocationService(isLocationUpdate)

                    if (!isLocationUpdate) {
                        isTracking = false
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLocationUpdate) Color.Green else Color.Gray // Zmiana koloru na zielony, gdy włączone śledzenie
                )
            ) {
                Text(if (isLocationUpdate) "Location" else "Location")
            }

            Spacer(modifier = Modifier.weight(1f)) // Dodaje elastyczną przestrzeń między przyciskami a trzecim przyciskiem

            Button(onClick = {
                isMenuOened = !isMenuOened
                toggleMenu(isMenuOened)
            }) {
                Text("M")
            }
        }
    }


//    Row(
//        modifier = modifier
//            .fillMaxWidth()
//            .background(Color.White)
//            .padding(16.dp),
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//        Text("Mapa", style = MaterialTheme.typography.titleMedium)
//
//        Button(onClick = onMenuClick) {
//            Text("Menu")
//        }
//    }
}

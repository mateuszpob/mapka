package pl.smolisoft.mapka.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import pl.smolisoft.mapka.R
import pl.smolisoft.mapka.services.SharedViewModel

@Composable
fun BottomBar(
    viewModel: SharedViewModel,
    toggleLocationService: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = Modifier.fillMaxWidth()
        .background(Color(0xFFFFFFFF))) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.Start // Ustawia przyciski na lewo
        ) {
//            Button(
//                onClick = {
//                    viewModel.isTracking = !viewModel.isTracking // Przełączenie trybu śledzenia
//                    if (viewModel.isTracking) {
//                        viewModel.isLocationUpdate = true
//                        toggleLocationService(viewModel.isLocationUpdate)
//                    }
//                },
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = if (viewModel.isTracking) Color.Green else Color.Gray // Zmiana koloru na zielony, gdy włączone śledzenie
//                )
//            ) {
//                Text(if (viewModel.isTracking) "Tracking" else "Tracking")
//            }

            Spacer(modifier = Modifier.width(16.dp)) // Dodaje przestrzeń między przyciskami

            IconButton(
                onClick = {
                    viewModel.isLocationUpdate = !viewModel.isLocationUpdate
                    toggleLocationService(viewModel.isLocationUpdate)
                },
                modifier = Modifier.size(48.dp) // Możesz dostosować rozmiar ikony
            ) {
                Icon(
                    painter = painterResource(
                        id = if (viewModel.isLocationUpdate) R.drawable.target_btn_b else R.drawable.target_btn
                    ),
                    contentDescription = null, // Opcjonalnie dodaj opis dla dostępności
                    tint = Color.Unspecified // Wyłącza automatyczne kolorowanie ikony
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(
                onClick = {
                    viewModel.isRecording = !viewModel.isRecording
                },
                modifier = Modifier.size(48.dp) // Możesz dostosować rozmiar ikony
            ) {
                Icon(
                    painter = painterResource(
                        id = if (viewModel.isRecording) R.drawable.rec_btn_r else R.drawable.rec_btn_g
                    ),
                    contentDescription = null, // Opcjonalnie dodaj opis dla dostępności
                    tint = Color.Unspecified // Wyłącza automatyczne kolorowanie ikony
                )
            }

            Spacer(modifier = Modifier.weight(1f))



            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = {
                    viewModel.isMenuOpened = !viewModel.isMenuOpened
                },
                modifier = Modifier.size(40.dp).padding(1.dp)
            ) {
                Icon(
                    painter = painterResource(
                        id = R.drawable.menu_b
                    ),
                    contentDescription = null, // Opcjonalnie dodaj opis dla dostępności
                    tint = Color.Unspecified // Wyłącza automatyczne kolorowanie ikony
                )
            }

        }
    }
}

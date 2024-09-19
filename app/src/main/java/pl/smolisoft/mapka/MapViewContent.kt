import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import android.net.Uri
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun MapViewContent(
    mapView: MapView?,
    currentLocation: GeoPoint?,
    onMapViewInitialized: (MapView) -> Unit,
    onGpxFileSelected: (Uri, MapView) -> Unit,
    onRequestLocationUpdate: () -> Unit // Funkcja do ponownej aktualizacji lokalizacji
) {
    var isTracking by remember { mutableStateOf(false) }

    // Layout główny ekranu
    Column(modifier = Modifier.fillMaxSize()) {
        // AndroidView dla MapView
        AndroidView(
            factory = { ctx ->
                MapView(ctx).also { map ->
                    map.setBuiltInZoomControls(true)
                    map.setMultiTouchControls(true)
                    map.controller.setZoom(15.0)
                    onMapViewInitialized(map)
                }
            },
            modifier = Modifier.weight(1f)
        )

        // Przyciski kontrolne
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            // Przycisk do centrowania mapy na lokalizacji użytkownika
            Button(onClick = {
                onRequestLocationUpdate()
            }) {
                Text("Center")
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Przycisk do wczytywania pliku GPX
            Button(onClick = {
                // Otwórz okno wyboru plików
            }) {
                Text("Load GPX")
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Przycisk toggle dla trybu śledzenia pozycji
            Button(
                onClick = {
                    isTracking = !isTracking // Przełączenie trybu śledzenia
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTracking) Color.Green else Color.Gray // Zmiana koloru na zielony, gdy włączone śledzenie
                )
            ) {
                Text(if (isTracking) "Stop Tracking" else "Start Tracking")
            }
        }
    }

    // Śledzenie pozycji w trybie "tracking" co 2 sekundy
    LaunchedEffect(isTracking) {
        while (isTracking) {
            onRequestLocationUpdate() // Zaktualizuj lokalizację
            mapView?.controller?.setCenter(currentLocation) // Ustawienie na nową lokalizację
            kotlinx.coroutines.delay(2000) // Opóźnienie 2 sekundy
        }
    }
}

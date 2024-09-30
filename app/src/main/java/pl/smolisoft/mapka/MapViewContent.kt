import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import pl.smolisoft.mapka.GpxUtils.drawGpxPath
import pl.smolisoft.mapka.R
@Composable
fun MapViewContent(
    context: Context,
    mapView: MapView?,
    currentLocation: GeoPoint?,
    onMapViewInitialized: (MapView) -> Unit,
    onGpxFileSelected: (Uri, MapView) -> Unit,
    onRequestLocationUpdate: () -> Unit,
    startLocationService: (Boolean) -> Unit
) {
    var isTracking by remember { mutableStateOf(false) }
    var isLocationUpdate by remember { mutableStateOf(false) }
    var userMarker by remember { mutableStateOf<Marker?>(null) } // Marker zapamiętany w stanie Compose

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

    Column(modifier = Modifier.fillMaxSize()) {
        // AndroidView dla MapView
        AndroidView(
            factory = { ctx ->
                MapView(ctx).also { map ->
                    map.setBuiltInZoomControls(true)
                    map.setMultiTouchControls(true)
                    map.controller.setZoom(15.0)

                    onMapViewInitialized(map)

                    // Inicjalizacja markera
                    userMarker = Marker(map).apply {
                        position = currentLocation ?: GeoPoint(52.0, 21.0) // Default location
                        setAnchor(0.2f, 0.2f)

                        // Ustawienie niestandardowej ikony
                        val iconDrawable = ContextCompat.getDrawable(ctx, R.drawable.ic_location)
                        icon = iconDrawable
                    }
                    map.overlays.add(userMarker)
                }
            },
            modifier = Modifier.weight(1f) // Umożliwienie mapie zajęcia dostępnej przestrzeni
        )

        // Przyciski kontrolne
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.DarkGray) // Ustawienie ciemnego tła dla przycisków
                .padding(8.dp) // Opcjonalnie: dodaj padding
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                // Przycisk do wczytywania pliku GPX
                Button(onClick = {
                    // Otwórz okno wyboru plików
                    launcher.launch(arrayOf("*/*"))
                }) {
                    Text("GPX")
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
                    Text(if (isTracking) "Tracking" else "Tracking")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = {
                        isLocationUpdate = !isLocationUpdate
                        startLocationService(isLocationUpdate)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLocationUpdate) Color.Green else Color.Gray // Zmiana koloru na zielony, gdy włączone śledzenie
                    )
                ) {
                    Text(if (isLocationUpdate) "SL" else "SL")
                }
            }
        }
    }

    // LaunchedEffect śledzący aktualizację lokalizacji
    LaunchedEffect(isTracking) {
        while (isTracking) {
            onRequestLocationUpdate() // Zaktualizuj lokalizację

            mapView?.let { map ->
                currentLocation?.let { location ->
                    Log.d("MainActivity", "Received location --------1: ${location.latitude}, ${location.longitude}")
                    Log.d("MainActivity", "Received location --------2: ${userMarker?.position?.latitude}, ${userMarker?.position?.longitude}")

                    // Zaktualizuj marker i centrum mapy
                    userMarker?.position = location
                    map.controller.setCenter(location)
                    map.invalidate()
                }
            }

            kotlinx.coroutines.delay(300)
        }
    }
}
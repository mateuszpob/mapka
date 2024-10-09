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
import androidx.compose.foundation.layout.fillMaxHeight
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
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import pl.smolisoft.mapka.GpxUtils.drawGpxPath
import pl.smolisoft.mapka.R
import androidx.compose.material3.AlertDialog


import androidx.compose.ui.Alignment
import androidx.compose.ui.zIndex
import pl.smolisoft.mapka.ui.MenuContent
import pl.smolisoft.mapka.ui.BottomBar

//@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapViewContent(
    context: Context,
    mapView: MapView?,
    currentLocation: GeoPoint?,
    onMapViewInitialized: (MapView) -> Unit,
    onGpxFileSelected: (Uri, MapView) -> Unit,
    onRequestLocationUpdate: () -> Unit,
    startLocationService: (Boolean) -> Unit,
    onMenuClick: () -> Unit,
) {
    var isTracking by remember { mutableStateOf(false) }
    var isLocationUpdate by remember { mutableStateOf(false) }
    var userMarker by remember { mutableStateOf<Marker?>(null) } // Marker zapamiętany w stanie Compose
    val topBarState = remember { mutableStateOf(true) }
    var isMenuOened by remember { mutableStateOf(false) }


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

        // Tutaj możesz dodać kod do wyświetlenia menu, np. AlertDialog
        if (isMenuOened) {
            AlertDialog(
                onDismissRequest = { isMenuOened = false },
                title = { Text("Menu") },
                text = {
                    MenuContent(
                        onDismiss = { isMenuOened = false },
                        onSettingsSelected = {
                            // Logika po wybraniu ustawień
                        },
                        onAnotherOptionSelected = {
                            // Logika po wybraniu innej opcji
                        }
                    )
                },
                confirmButton = {
                    Button(onClick = { isMenuOened = false }) {
                        Text("Zamknij")
                    }
                }
            )
        }





        // AndroidView dla MapView
        AndroidView(
            factory = { ctx ->
                MapView(ctx).also { map ->
                    map.setBuiltInZoomControls(true)
                    map.setMultiTouchControls(true)
                    map.controller.setZoom(15.0)

                    // Dodajemy listener mapy, aby wykryć ruch
                    map.setMapListener(object : MapListener {
                        override fun onScroll(event: ScrollEvent?): Boolean {
                            // Gdy użytkownik poruszy mapę, wyłącz tracking
                            isTracking = false
                            Log.d("MapListener", "Map moved, tracking disabled")
                            return true
                        }

                        override fun onZoom(event: ZoomEvent?): Boolean {
                            isTracking = false
                            return true
                        }
                    })

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
            update = { mapViewLocal ->
                // Aktualizuj pozycję markera
                userMarker?.position = currentLocation
                if (isLocationUpdate) {
                    if (!mapViewLocal.overlays.contains(userMarker)) {
                        mapViewLocal.overlays.add(userMarker) // Dodaj, jeśli nie ma
                    }
                } else {
                    mapViewLocal.overlays.remove(userMarker) // Usuń, jeśli jest niewidoczny
                }
                mapViewLocal.invalidate() // Odśwież mapę
            },
            modifier = Modifier
                .weight(1f) // Umożliwienie mapie zajęcia dostępnej przestrzeni
        )

        BottomBar(
            toggleTracking = { isTrackingRun ->
                isTracking = isTrackingRun
            },
            toggleLocationService = { locationUpdate ->
                startLocationService(locationUpdate)
                isLocationUpdate = locationUpdate
            },
            onMenuClick = { isMenuOened = true },
            toggleMenu = { isMenuOened1 ->
                isMenuOened = isMenuOened1
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
//                .background(Color.Red)
        )

    }

    // LaunchedEffect śledzący aktualizację lokalizacji
    LaunchedEffect(isTracking) {
        while (isTracking) {
            onRequestLocationUpdate() // Zaktualizuj lokalizację

            mapView?.let { map ->
                currentLocation?.let { location ->
                    Log.d("MainActivity", "Received location: ${location.latitude}, ${location.longitude}")

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
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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



import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import androidx.compose.ui.zIndex
import pl.smolisoft.mapka.MenuContent
import pl.smolisoft.mapka.TopBar

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
    var isMenuVisible by remember { mutableStateOf(false) }


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

        // Pasek nawigacyjny (TopAppBar)
//        TopAppBar(
//            title = { Text("Mapka") },
//            navigationIcon = {
//                IconButton(onClick = onMenuClick) { // Otwieranie menu
//                    Icon(Icons.Filled.Menu, contentDescription = "Menu")
//                }
//            }
//        )

        TopBar(
            onMenuClick = { isMenuVisible = true },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .zIndex(1f) // Ustaw zIndex na wyższy niż dla mapy
        )

        // Tutaj możesz dodać kod do wyświetlenia menu, np. AlertDialog
        if (isMenuVisible) {
            AlertDialog(
                onDismissRequest = { isMenuVisible = false },
                title = { Text("Menu") },
                text = {
                    MenuContent(
                        onDismiss = { isMenuVisible = false },
                        onSettingsSelected = {
                            // Logika po wybraniu ustawień
                        },
                        onAnotherOptionSelected = {
                            // Logika po wybraniu innej opcji
                        }
                    )
                },
                confirmButton = {
                    Button(onClick = { isMenuVisible = false }) {
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
            modifier = Modifier.weight(1f) // Umożliwienie mapie zajęcia dostępnej przestrzeni
        )

        // Przyciski kontrolne
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.7f)) // Ustawienie ciemnego tła dla przycisków
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

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = {
                        isLocationUpdate = !isLocationUpdate
                        startLocationService(isLocationUpdate)

                        if (!isLocationUpdate) {
                            isTracking = false
                        }
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
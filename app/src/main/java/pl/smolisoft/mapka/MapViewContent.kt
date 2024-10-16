import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.MotionEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import pl.smolisoft.mapka.services.SharedViewModel
import pl.smolisoft.mapka.ui.BottomBar
import pl.smolisoft.mapka.ui.MenuContent

@SuppressLint("ClickableViewAccessibility")
@Composable
fun MapViewContent(
    viewModel: SharedViewModel,
    context: Context,
    mapView: MapView?,
    onMapViewInitialized: (MapView) -> Unit,
    onGpxFileSelected: (Uri, MapView) -> Unit,
    onRequestLocationUpdate: () -> Unit,
    startLocationService: (Boolean) -> Unit,
    onMenuClick: () -> Unit,
) {
    var userMarker by remember { mutableStateOf<Marker?>(null) }



    Column(modifier = Modifier.fillMaxSize()) {
        // Tutaj możesz dodać kod do wyświetlenia menu, np. AlertDialog
        if (mapView !== null && viewModel.isMenuOpened) {
            AlertDialog(
                onDismissRequest = { viewModel.isMenuOpened = false },
                title = { Text("Menu") },
                text = {
                    MenuContent(
                        context = context,
                        mapView = mapView,
                        viewModel = viewModel,
                        onDismiss = { viewModel.isMenuOpened = false },
                        onSettingsSelected = {
                            // Logika po wybraniu ustawień
                        },
                        onAnotherOptionSelected = {
                            // Logika po wybraniu innej opcji
                        }
                    )
                },
                confirmButton = {
                    Button(onClick = { viewModel.isMenuOpened = false }) {
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

                    // Ustawienie nasłuchiwania na dotyk mapy
                    map.setOnTouchListener { _, motionEvent ->
                        when (motionEvent.action) {
                            MotionEvent.ACTION_MOVE -> {
                                // Gdy użytkownik przesuwa mapę
                                viewModel.isTracking = false
                                Log.d("MapListener", "Map moved, tracking disabled")
                            }
                            MotionEvent.ACTION_UP -> {
                                // Gdy użytkownik przestaje przesuwać
                                viewModel.isTracking = false
                                Log.d("MapListener", "Touch released, tracking disabled")
                            }
                        }
                        false
                    }

                    onMapViewInitialized(map)

                    // Inicjalizacja markera
                    userMarker = Marker(map).apply {
                        position = viewModel.currentLocation
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
                userMarker?.position = viewModel.currentLocation

                if (viewModel.isLocationUpdate) {
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
            viewModel= viewModel,
            toggleLocationService = { locationUpdate ->
                startLocationService(locationUpdate)
                viewModel.isLocationUpdate = locationUpdate
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )
    }

    // LaunchedEffect śledzący aktualizację lokalizacji
    LaunchedEffect(viewModel.isTracking) {
        while (viewModel.isTracking) {
            onRequestLocationUpdate() // Zaktualizuj lokalizację

            mapView?.let { map ->
                viewModel.currentLocation?.let { location ->
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
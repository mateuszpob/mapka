package pl.smolisoft.mapka

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.MotionEvent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import pl.smolisoft.mapka.services.SharedViewModel
import pl.smolisoft.mapka.ui.BottomBar
import pl.smolisoft.mapka.ui.MenuContent

@Composable
fun MapViewContent(
    viewModel: SharedViewModel,
    context: Context,
    mapView: MapView?,
    onMapViewInitialized: (MapView) -> Unit,
    startLocationService: (Boolean) -> Unit,
) {
    var userMarker by remember { mutableStateOf<Marker?>(null) }
    val currentLocation by viewModel.currentLocation.collectAsState()

    Log.d("MapViewContent", "MapViewContent is active")

    Column(modifier = Modifier.fillMaxSize()) {
        if (viewModel.isMenuOpened) {
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

        // Ustawienie MapView tylko raz
        AndroidView(
            factory = { ctx ->
                MapView(ctx).also { map ->
                    map.setMultiTouchControls(true)
                    map.controller.setZoom(15.0)

                    onMapViewInitialized(map)

                    // Inicjalizacja markera
                    userMarker = Marker(map).apply {
                        setAnchor(0.2f, 0.2f)
                        icon = ContextCompat.getDrawable(ctx, R.drawable.ic_location)
                        map.overlays.add(this)
                    }
                }
            },
            modifier = Modifier.weight(1f)
        )

        BottomBar(
            viewModel = viewModel,
            toggleLocationService = { locationUpdate ->
                startLocationService(locationUpdate)
                viewModel.isLocationUpdate = locationUpdate
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }

    // LaunchedEffect monitorujący aktualizacje lokalizacji i ustawiający pozycję na mapie
    LaunchedEffect(currentLocation) {
        currentLocation?.let { location ->
            userMarker?.apply {
                position = GeoPoint(location.latitude, location.longitude)
            }
            mapView?.controller?.animateTo(GeoPoint(location.latitude, location.longitude))
        }
    }
}
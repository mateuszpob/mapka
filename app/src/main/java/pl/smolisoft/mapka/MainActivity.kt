package pl.smolisoft.mapka

import MapViewContent
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLocation: GeoPoint? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            // Obsługa przypadku, gdy uprawnienia nie zostały przyznane
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Konfiguracja osmdroid
        Configuration.getInstance().load(applicationContext, getPreferences(MODE_PRIVATE))

        // Inicjalizacja klienta lokalizacji
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Ustawiamy zawartość ekranu przy pomocy Jetpack Compose
        setContent {
            val context = LocalContext.current
            var mapView by remember { mutableStateOf<MapView?>(null) } // MapView do kontroli mapy

            MapViewContent(
                mapView = mapView,
                currentLocation = userLocation,
                onMapViewInitialized = { initializedMapView ->
                    mapView = initializedMapView // Inicjalizacja mapView
                    checkLocationPermission(mapView)
                },
                onGpxFileSelected = { uri, mapView ->
                    val inputStream = context.contentResolver.openInputStream(uri)
                    inputStream?.let { stream ->
                        GpxUtils.drawGpxPath(stream, mapView)
                    }
                },
                onRequestLocationUpdate = {
                    // Aktualizacja lokalizacji
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            userLocation = GeoPoint(it.latitude, it.longitude)
                            Log.d("MainActivity", "New location: $userLocation, set center")
                            mapView?.controller?.setCenter(userLocation)
                        } ?: run {
                            Log.d("MainActivity", "Failed to retrieve location")
                        }
                    }
                }
            )
        }
    }

    private fun checkLocationPermission(mapView: MapView?) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        } else {
            // Pobierz aktualną lokalizację
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    userLocation = GeoPoint(it.latitude, it.longitude)
                    mapView?.let { map ->
                        val marker = Marker(map).apply {
                            position = userLocation!!
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            title = "Jesteś tutaj"
                        }
                        map.overlays.add(marker)
                        map.controller.setCenter(userLocation)
                        map.invalidate()
                    }
                }
            }
        }
    }

    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Poproś o uprawnienia do odczytu plików
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
}
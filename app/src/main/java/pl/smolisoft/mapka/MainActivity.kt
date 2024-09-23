package pl.smolisoft.mapka

import MapViewContent
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLocation: GeoPoint? = null
    private var userMarker: Marker? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            // Obsługa przypadku, gdy uprawnienia nie zostały przyznane
        }
    }

    private fun initializeMarker(mapView: MapView) {
        userMarker = Marker(mapView).apply {
            position = userLocation ?: GeoPoint(0.0, 0.0)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "Jesteś tutaj"
        }
        mapView.overlays.add(userMarker!!)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Konfiguracja osmdroid
        Configuration.getInstance().load(applicationContext, getPreferences(MODE_PRIVATE))

        // Inicjalizacja klienta lokalizacji
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val locationRequest = LocationRequest.Builder(
                100
            ).build()

        // Ustawiamy zawartość ekranu przy pomocy Jetpack Compose
        setContent {
            val context = LocalContext.current
            var mapView by remember { mutableStateOf<MapView?>(null) } // MapView do kontroli mapy

            // Callback do przetwarzania lokalizacji, z dostępem do mapView
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    for (location in locationResult.locations) {
                        location?.let {
                            userLocation = GeoPoint(it.latitude, it.longitude)
                            Log.d("MainActivity", "New GPS location: $userLocation, set center")
                            userMarker?.position = userLocation
                            mapView?.controller?.setCenter(userLocation)
                            mapView?.invalidate()
                        }
                    }
                }
            }

            // Uruchomienie selektora pliku
            MapViewContent(
                context = context,
                mapView = mapView,
                currentLocation = userLocation,
                onMapViewInitialized = { initializedMapView ->
                    mapView = initializedMapView // Inicjalizacja mapView
                    checkLocationPermission(mapView)
                    initializeMarker(initializedMapView)

                    try {
                        // Start GPS updates with mapView in scope
                        fusedLocationClient.requestLocationUpdates(
                            locationRequest,
                            locationCallback,
                            Looper.getMainLooper()
                        )
                    } catch (e: SecurityException) {
                        Log.e("MainActivity", "Location permission not granted: ${e.message}")
                    }
                },
                onGpxFileSelected = { uri, mapViewLocal ->
                    val inputStream = context.contentResolver.openInputStream(uri)
                    inputStream?.let { stream ->
                        GpxUtils.drawGpxPath(stream, mapViewLocal)
                    }
                },
                onRequestLocationUpdate = {
                    // Ręczne zapytanie o ostatnią lokalizację, na wypadek gdyby aktualizacje GPS nie działały
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            userLocation = GeoPoint(it.latitude, it.longitude)
                            Log.d("MainActivity", "Manual location update: $userLocation, set center")
                            userMarker?.position = userLocation
                            mapView?.controller?.setCenter(userLocation)
                            mapView?.invalidate()
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
//                        val marker = Marker(map).apply {
//                            position = userLocation!!
//                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
//                            title = "Jesteś tutaj"
//                        }
//                        map.overlays.add(marker)
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
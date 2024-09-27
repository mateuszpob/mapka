package pl.smolisoft.mapka

import MapViewContent
import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
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
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MainActivity : ComponentActivity() {

//    private lateinit var fusedLocationClient: FusedLocationProviderClient
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
            position = GeoPoint(0.0, 0.0) // userLocation ?: GeoPoint(0.0, 0.0)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "Jesteś tutaj"
        }
        mapView.overlays.add(userMarker!!)
    }

//    private val permissionRequestReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent?) {
//            ActivityCompat.requestPermissions(
//                this@MainActivity,
//                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                1
//            )
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ustawienie flagi, aby ekran nie gasł
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

//      // to moze byc potrzebne
//        val intentFilter = IntentFilter("pl.smolisoft.mapka.REQUEST_LOCATION_PERMISSION")
//        registerReceiver(permissionRequestReceiver, intentFilter, RECEIVER_NOT_EXPORTED)


        // Konfiguracja osmdroid
        Configuration.getInstance().load(applicationContext, getPreferences(MODE_PRIVATE))

        // Inicjalizacja klienta lokalizacji
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//
//        val locationRequest = LocationRequest.Builder(
//            100
//        ).build()

        // Ustawiamy zawartość ekranu przy pomocy Jetpack Compose
        setContent {
            val context = LocalContext.current
            var mapView by remember { mutableStateOf<MapView?>(null) } // MapView do kontroli mapy

            // Callback do przetwarzania lokalizacji, z dostępem do mapView
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
//                    for (location in locationResult.locations) {
//                        location?.let {
//                            userLocation = GeoPoint(it.latitude, it.longitude)
//                            Log.d("MainActivity", "New GPS location: $userLocation, set center")
//                            userMarker?.position = userLocation
//                            mapView?.controller?.setCenter(userLocation)
//                            mapView?.invalidate()
//                        }
//                    }
                }
            }

            // Uruchomienie selektora pliku
            MapViewContent(
                context = context,
                mapView = mapView,
                currentLocation = GeoPoint(0.0, 0.0),
                onMapViewInitialized = { initializedMapView ->
                    mapView = initializedMapView // Inicjalizacja mapView
                    checkLocationPermission(mapView)
                    initializeMarker(initializedMapView)

                    try {
                        // Start GPS updates with mapView in scope
//                        fusedLocationClient.requestLocationUpdates(
//                            locationRequest,
//                            locationCallback,
//                            Looper.getMainLooper()
//                        )
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
                    Log.d("MainActivity", "Start tracking")
                },
                startLocationService = { isLocationUpdate ->
                    if (isLocationUpdate) {
                        startLocationService()
                    } else {
                        stopLocationService()
                    }
                }

            )

            // Odbieranie lokalizacji z serwisu
            val locationReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    intent?.let {
                        val latitude = it.getDoubleExtra("latitude", 0.0)
                        val longitude = it.getDoubleExtra("longitude", 0.0)
                        userLocation = GeoPoint(latitude, longitude)
                        Log.d("MainActivity", "Received location: $latitude, $longitude")

                        // Zaktualizuj pozycję markera
                        userMarker?.position = userLocation
                        mapView?.controller?.setCenter(userLocation)
                        mapView?.invalidate()
                    }
                }
            }

            // Rejestracja BroadcastReceiver
            val intentFilter = IntentFilter("LOCATION_UPDATE")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(locationReceiver, intentFilter, RECEIVER_NOT_EXPORTED)
            }
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
            // Start LocationService
            // startLocationService()
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

    private fun startLocationService() {
        val intent = Intent(this, LocationService::class.java)
        startForegroundService(intent)
    }

    private fun stopLocationService() {
        val intent = Intent(this, LocationService::class.java)
        stopService(intent)
    }
}